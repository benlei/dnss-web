#!/bin/ruby
require 'json'
require 'pg'
require_relative 'common'
require_relative 'dn-weapons'
require_relative 'dn-skills'

##############################################################################
# Hopefully only thing you have to edit
##############################################################################
JSON_DIRECTORY = 'C:\\Users\\Ben\\IdeaProjects\\dn-skill-sim\\dnss-web\\src\\main\\webapp\\resources\\json'
MIN_JSON_DIRECTORY = 'C:\\Users\\Ben\\IdeaProjects\\dn-skill-sim\\dnss-web\\src\\main\\webapp\\resources\\json\\min'
# JSON_DIRECTORY = 'E:\\json'

##############################################################################
# get all messages
##############################################################################
messages = Hash.new
@conn.exec('SELECT * FROM messages').each_dnt {|message| messages[message['id']] = message['data']}

# untranslated messages
messages[1000085115] = messages[1000007433] # academic tumble

##############################################################################
# gets all the jobs [write]
# notes:
#   jobnumber => 0 = base, 1 = first advancement, etc.
##############################################################################
jobs = Hash.new
query = <<sql_query
  SELECT j._id,
          m._data as jobname,
         LOWER(_englishname) as englishname,
         _parentjob,
         _jobnumber as advancement
  FROM jobs j
  INNER JOIN messages m
    ON _jobname = m._id
  WHERE _service is TRUE
sql_query
@conn.exec(query).each_dnt do |job|
  job['skills'] = Hash.new
  job['messages'] = Hash.new    # prepare to store messages needed for skill descriptions
  jobs[job['id']] = job
  jobs[job['id']].delete('id')
end

# puts JSON.pretty_generate jobs

##############################################################################
# get all the skills of all classes
##############################################################################
query = <<sql_query
  SELECT _id,
         _nameid,
         _iconimageindex,
         _needjob,
         _skilltype as type,
         _needweapontype1, _needweapontype2
  FROM skills
  WHERE _id IN (
      SELECT _skilltableid
      FROM skill_tree
  )
  ORDER BY _needjob ASC
sql_query
@conn.exec(query).each_dnt do |skill|
  jobs[skill['needjob']]['skills'][skill['id']] = skill
  jobs[skill['needjob']]['messages'][skill['nameid']] = messages[skill['nameid']]

  skill['levels'] = Array.new
  skill['image'] = '%02d' % ((skill['iconimageindex'] / 200) + 1)
  skill['icon'] = skill['iconimageindex'] % 200

  skill['needweapon'] = Array.new
  skill['needweapon'] << skill['needweapontype1'] unless skill['needweapontype1'] == -1
  skill['needweapon'] << skill['needweapontype2'] unless skill['needweapontype2'] == -1

  # delete these
  ['id', 'needjob', 'iconimageindex', 'needweapontype1', 'needweapontype2'].each {|a| skill.delete(a)}
end

##############################################################################
# get the skill tree for each class
##############################################################################
query = <<sql_query
  SELECT _needjob,
         _skilltableid as skillid,
         _parentskillid1, _parentskillid2,
         _needparentskilllevel1, _needparentskilllevel2,
         _needbasicsp1, _needfirstsp1
  FROM skill_tree
  INNER JOIN skills
    ON _skilltableid = skills._id
sql_query

@conn.exec(query).each_dnt do |tree|
  job = jobs[tree['needjob']]
  skills = job['skills']
  skill = skills[tree['skillid']]
  skill['requires'] = Array.new
  skill['requires'] << {'id' => tree['parentskillid1'], :level => tree['needparentskilllevel1']} unless tree['parentskillid1'] == 0
  skill['requires'] << {'id' => tree['parentskillid2'], :level => tree['needparentskilllevel2']} unless tree['parentskillid2'] == 0
  skill['need_sp'] = [tree['needbasicsp1'], tree['needfirstsp1']]
end

##############################################################################
# get default skills
##############################################################################
queries = Array.new
base_query = <<sql_query
  SELECT _defaultskill%1$d as id, _needjob
  FROM default_create
  INNER JOIN skills s
    ON s._id = _defaultskill%1$d
  INNER JOIN jobs j
    ON j._id = _needjob
  WHERE _jobnumber = 0
sql_query
(1..10).each {|i| queries << base_query % i}
query = queries.join("UNION\n")
@conn.exec(query).each_dnt do |skill|
  job = jobs[skill['needjob']]
  if job['default_skills'].nil?
    job['default_skills'] = Array.new
  end

  job['default_skills'] << skill['id']
end


##############################################################################
# get all base jobs, and then get all tables of the base classes
# Notes:
#   _decreasehp, at this point of time, is always 0
##############################################################################
jobs.select {|id, job| job['advancement'] == 0}.each_value do |job|
  query = <<-sql_query
    SELECT  _needjob,
           _skillindex as id,
           _skilllevel,
           _levellimit as required_level,
           _decreasesp as mpcost,
           _skillexplanationid as explanationid, _skillexplanationidparam,
           _needskillpoint as spcost,
           _delaytime as cd
    FROM skills_%s_%s s
    INNER JOIN skills
      ON _skillindex = skills._id
    WHERE _needjob > 0
      AND _nameid > 0
      AND _skillindex IN (
        SELECT _skilltableid
        FROM skill_tree
      )
    ORDER BY _skillindex, _skilllevel ASC
  sql_query

  @conn.exec(query % [job['englishname'], 'pve']).each_dnt do |skill|
    jobs[skill['needjob']]['skills'][skill['id']]['levels'] << skill

    cd = skill['cd'] / 1000.0
    cd = cd.to_i if cd == cd.to_i

    jobs[skill['needjob']]['messages'][skill['explanationid']] = messages[skill['explanationid']]
    skill['cd'] = {'pve' => cd}
    skill['mpcost'] = {'pve' => skill['mpcost'] / 10.0}
    skill['explanationparams'] = {'pve'=> skill['skillexplanationidparam'].to_s.split(',').map {|str| str.strip.message_format(messages)}}
    skill['explanationid'] = {'pve' => skill['explanationid']}

    ['id', 'skilllevel', 'needjob', 'skillexplanationidparam'].each {|a| skill.delete(a)}
  end

  @conn.exec(query % [job['englishname'], 'pvp']).each_dnt do |skill|
    level = jobs[skill['needjob']]['skills'][skill['id']]['levels'][skill['skilllevel'].to_i - 1]
    next if level.nil?

    cd = skill['cd'] / 1000.0
    cd = cd.to_i if cd == cd.to_i

    jobs[skill['needjob']]['messages'][skill['explanationid']] = messages[skill['explanationid']]
    level['cd']['pvp'] = cd
    level['mpcost']['pvp'] = skill['mpcost'] / 10.0
    level['explanationparams']['pvp'] = skill['skillexplanationidparam'].to_s.split(',').map {|str| str.strip.message_format(messages)}
    level['explanationid']['pvp'] = skill['explanationid']
  end
end


JSON_DIRECTORY.gsub!(/[\/\\]/, File::SEPARATOR)
MIN_JSON_DIRECTORY.gsub!(/[\/\\]/, File::SEPARATOR)
mkdir_p(JSON_DIRECTORY)
mkdir_p(MIN_JSON_DIRECTORY)

##############################################################################
# WRITE: all jobs tertiary jobs
##############################################################################
jobs.select {|id, job| job['advancement'] == 2}.each_value do |tertiary|
  englishname = tertiary['englishname']

  secondary = jobs[tertiary['parentjob']]
  primary = jobs[secondary['parentjob']]

  messages = primary['messages'].merge(secondary['messages']).merge(tertiary['messages'])
  skills = [primary['skills'], secondary['skills'], tertiary['skills']]
  json = {'messages' => messages, 'skills' => skills, 'types' => {'weapons' => DN_WEAPON_TYPES, 'skills' => DN_SKILL_TYPES}}

  create_json_file('%s%s%s.json' % [JSON_DIRECTORY, File::SEPARATOR, englishname], JSON.pretty_generate(json))
  create_json_file('%s%s%s.json' % [MIN_JSON_DIRECTORY, File::SEPARATOR, englishname], json.to_json)
end

@conn.close()