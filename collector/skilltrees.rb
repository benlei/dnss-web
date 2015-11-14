#!/bin/ruby
require 'json'
require_relative 'common'

conn = createPGConn()

##############################################################################
# get all messages
##############################################################################
uistring = load_uistring()

# untranslated messages


# var declarations
tables = Array.new
jobs = Hash.new

# get all skill tables
rs = conn.exec("SELECT table_name FROM information_schema.tables WHERE table_name LIKE 'skillleveltable\\_character%'")
rs.each do |r|
  if not r["table_name"].end_with?("characteretc")
    tables << r["table_name"]
  end
end

##############################################################################
# gets all the jobs [write]
# notes:
#   jobnumber => 0 = base, 1 = first advancement, etc.
##############################################################################
query = <<QUERY
SELECT _primaryid as _id,
       _jobname,
       LOWER(_englishname) as englishname,
       _parentjob,
       _jobnumber as advancement
FROM jobtable
WHERE _service is TRUE
QUERY

character_union = []
#character_union = ["SELECT * FROM skilltable_character90passive"]
conn.exec(query).each_dnt do |job|
  job['jobname'] = uistring[job['jobname']]
  job['skills'] = Hash.new
  job['messages'] = Hash.new    # prepare to store messages needed for skill descriptions
  jobs[job['id']] = job
  jobs[job['id']].delete('id')
  if job['advancement'] == 0
    character_union << "SELECT * FROM skilltable_character#{job['englishname']}"
  end
end

character_union = character_union.join(" UNION ")

##############################################################################
# get all the skills of all classes
##############################################################################
query = <<sql_query
SELECT s._primaryid as id,
       _nameid,
       _needjob,
       _skilltype as type,
       _needweapontype1, _needweapontype2,
       _parentskillid1, _parentskillid2,
       _needparentskilllevel1, _needparentskilllevel2,
       _needbasicsp1, _needfirstsp1,
       _spmaxlevel
FROM (#{character_union}) s
INNER JOIN skilltreetable
ON s._primaryid = _skilltableid
ORDER BY _needjob ASC
sql_query

conn.exec(query).each_dnt do |skill|
  jobs[skill['needjob']]['skills'][skill['id']] = skill
  jobs[skill['needjob']]['messages'][skill['nameid']] = uistring[skill['nameid']]

  skill['levels'] = Array.new

  skill['needweapon'] = Array.new
  skill['needweapon'] << skill['needweapontype1'] unless skill['needweapontype1'] == -1
  skill['needweapon'] << skill['needweapontype2'] unless skill['needweapontype2'] == -1

  skill['requires'] = Hash.new
  skill['requires'][skill['parentskillid1']] = skill['needparentskilllevel1'] unless skill['parentskillid1'] == 0
  skill['requires'][skill['parentskillid2']] = skill['needparentskilllevel2'] unless skill['parentskillid2'] == 0

  skill['need_sp'] = Hash.new
  skill['need_sp'][0] = skill['needbasicsp1'] unless skill['needbasicsp1'] == 0
  skill['need_sp'][1] = skill['needfirstsp1'] unless skill['needfirstsp1'] == 0
  
  # delete these
  ['id', 'needjob',
    'needweapontype1', 'needweapontype2',
    'needbasicsp1', 'needfirstsp1',
    'needparentskilllevel1', 'needparentskilllevel2',
    'parentskillid1', 'parentskillid2'].each {|a| skill.delete(a)}
end

query = <<QUERY
SELECT _needjob,
       _skillindex as id,
       _skilllevel,
       _levellimit as required_level,
       _needskillpoint as spcost,
       _skillexplanationid,
       _skillexplanationidparam,
       _decreasesp,
       _delaytime
FROM %s c
INNER JOIN (#{character_union}) s
  ON _skillindex = s._primaryid
INNER JOIN jobtable j
  ON j._primaryid = _needjob
INNER JOIN skilltreetable
  ON _skillindex = _skilltableid
WHERE _service IS TRUE
  AND _applytype = %d
  AND _skilllevel <= _maxlevel
ORDER BY _skillindex, _skilllevel ASC
QUERY

def add_skill(jobs, uistring, skill, mode)
  if jobs[skill['needjob']]['skills'][skill['id']]['levels'][skill["skilllevel"] - 1].nil?
    jobs[skill['needjob']]['skills'][skill['id']]['levels'][skill["skilllevel"] - 1] = skill
    
    skill["cd"] = Hash.new
    skill["mpcost"] = Hash.new
    skill["explanationid"] = Hash.new
    skill["explanationparams"] = Hash.new
  end

  s = jobs[skill['needjob']]['skills'][skill['id']]['levels'][skill["skilllevel"] - 1]

  if mode == "pve"
    s["spcost"] = skill["spcost"]
  end

  cd = skill['delaytime'] / 1000.0
  cd = cd.to_i if cd == cd.to_i
  
  s["cd"][mode] = cd
  s['mpcost'][mode] = skill['decreasesp']
  
  s['explanationparams'][mode] = skill['skillexplanationidparam'].to_s.split(',').map {|str| str.strip.message_format(uistring)}
  s['explanationid'][mode] = skill['skillexplanationid']
  
  # add to messages
  jobs[skill['needjob']]['messages'][skill['skillexplanationid']] = uistring[skill['skillexplanationid']]

  ['id', 'skilllevel', 'needjob',
    'skillexplanationid', 'skillexplanationidparam', 'decreasesp', 'delaytime'].each {|a| skill.delete(a)}
end

tables.each do |table|
  if not table.end_with?("pvp") # is pve!
    conn.exec(query % [table, 0]).each_dnt do |skill|
      add_skill(jobs, uistring, skill, "pve")
    end
  end
  
  if not table.end_with?("pve") # is pvp!
    conn.exec(query % [table, 1]).each_dnt do |skill|
      add_skill(jobs, uistring, skill, "pvp")
    end
  end
end

mkdir_p(JSON_PATH.gsub(/[\/\\]/, File::SEPARATOR))

##############################################################################
# WRITE: all jobs jobs
##############################################################################
jobs.each do |id, job|
  # create the json files
  create_json_file(JSON_PATH + "/%s.json" % (job['englishname']),
    {'skills' => job['skills'], 'messages' => job['messages']}.to_json)
end

conn.close()
