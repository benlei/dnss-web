#!/bin/ruby
require 'json'
require 'pg'

##############################################################################
# Hopefully only thing you have to edit
##############################################################################
JSON_DIRECTORY = 'E:\\json'

##############################################################################
# Custom things that I can't find in DNT files
##############################################################################
weapon_types = {
  0 => 'Greatsword',
  1 => 'Gauntlet',
  2 => 'Axe',
  3 => 'Hammer',
  4 => 'Shortbow',
  5 => 'Longbow',
  6 => 'Crossbow',
  7 => 'Quiver',
  8 => 'Book',
  9 => 'Orb',
  10 => 'Puppet',
  11 => 'Mace',
  12 => 'Flail',
  13 => 'Wand',
  14 => 'Shield',
  15 => 'Quiver',
  16 => 'Cannon',
  17 => 'Bubble Blaster',
  18 => 'Powerglove',
  19 => 'Fan',
  20 => 'Chakram',
  21 => 'Focus',
  22 => 'Scimtar',
  23 => 'Dagger',
  24 => 'Hook',
  25 => 'Spear',
  26 => 'Scythe', # ABSOLUTELY NO CLUE WHAT THIS IS, BUT IT'S THE OTHER SIDE OF LENCEA
  27 => 'Bracelet'
}


skill_types = {
  0 => 'Instant',
  1 => 'Passive',
  2 => 'Reactive Passive',
  3 => 'Passive Enhanced'
}

##############################################################################
# Extending base classes for own needs
##############################################################################
class String
  def message_format(hash)
    if self.empty?
      return self
    end
    
    message = String.new(self)
    while (m = message.match(/\{([0-9]+)\}/)).nil? == false
      message.gsub!(m[0], hash[m[1].to_i])
    end

    message
  end
end

class PG::Result
  def each_dnt()
    self.each do |row|
      row.keys.each do |key|
        if key[0, 1] == '_'
          row[key[1, key.length - 1]] = row[key]
          row.delete(key)
        end
      end
      
      yield row
    end
  end
end

##############################################################################
# connect to postgresql server
##############################################################################
conn = PG.connect(:host => 'localhost', :port => 5432, 
                  :user => 'dnss', :password => 'dnss',
                  :dbname => 'dnss')
conn.type_map_for_results = PG::BasicTypeMapForResults.new conn

##############################################################################
# get all messages
##############################################################################
messages = Hash.new
conn.exec('SELECT * FROM messages').each_dnt {|message| messages[message['id']] = message['data']}

##############################################################################
# Combine assassin tables
##############################################################################
query = <<sql_query
  INSERT INTO skills_assassin_%1$s
    SELECT *
    FROM skills_assassin_bringer_%1$s
    WHERE NOT EXISTS (
      SELECT *
      FROM skills_assassin_%1$s
    )
sql_query
conn.exec(query % 'pve')
conn.exec(query % 'pvp')

##############################################################################
# get sp required of all classes [write]
##############################################################################
sp_by_level = Hash.new
query = "SELECT _id, _skillpoint FROM player_level ORDER BY _id ASC LIMIT 100"
conn.exec(query).each_dnt {|row| sp_by_level[row['id']] = sp_by_level[row['id'] - 1].to_i + row['skillpoint']}

##############################################################################
# gets all the jobs [write]
# notes:
#   jobnumber => 0 = base, 1 = first advancement, etc.
#   jobicon => # position from top left to right
#   maxspjobx => # ratio to max SP (floored); at the moment 3 and 4 are unused
#   class => the base job (the class of a base job is the base job id)
##############################################################################
jobs = Hash.new
query = <<sql_query
  SELECT j._id,
         m1._data as jobname,
         LOWER(_englishname) as englishname,
         _jobnumber,
         _baseclass, _parentjob,
         _jobicon,
         _maxspjob0, _maxspjob1, _maxspjob2
  FROM jobs j
  INNER JOIN messages m1
    ON _jobname = m1._id
  WHERE _service is TRUE
  ORDER BY _id ASC
sql_query
conn.exec(query).each_dnt do |job|
  job['skills'] = Hash.new
  job['skilltree'] = Array.new
  job['messages'] = Hash.new    # prepare to store messages needed for skill descriptions
  jobs[job['id']] = job
  jobs[job['id']].delete('id')
end

##############################################################################
# get all the skills of all classes
# note:
#   _iconimageindex => # position from top left to right
#   needweapontype1,2 => must be wearing this weapon to use skill... need to self-make it
##############################################################################
query = <<sql_query
  SELECT s._id,
         nmessages._data as skillname,
         _iconimageindex,
         _needjob,
         _skilltype,
         _needweapontype1, _needweapontype2,
         _maxlevel
  FROM skills s
  INNER JOIN messages nmessages
    ON nmessages._id = _nameid
  WHERE _needjob > 0
  ORDER BY _needjob ASC
sql_query
conn.exec(query).each_dnt do |skill|
  jobs[skill['needjob']]['skills'][skill['id']] = skill
  skill['levels'] = Hash.new   # prepare to put skill levels into this
  skill['imagenum'] = (skill['iconimageindex'] / 200) + 1
  skill['position'] = skill['iconimageindex'] % 200
  skill['needweapon1'] = skill['needweapontype1'] == -1 ? nil : weapon_types[skill['needweapontype1']]
  skill['needweapon2'] = skill['needweapontype2'] == -1 ? nil : weapon_types[skill['needweapontype2']]
  skill['skilltype'] = skill_types[skill['skilltype']]

  # delete these
  skill.delete('id')
  skill.delete('needjob')
  skill.delete('iconimageindex')
  skill.delete('needweapontype1')
  skill.delete('needweapontype2')
end


##############################################################################
# get all base jobs, and then get all tables of the base classes
# Notes:
#   _decreasehp, at this point of time, is always 0
##############################################################################
jobs.select {|id, job| job['jobnumber'] == 0}.each_value do |job|
  query = <<-sql_query
    SELECT  _needjob,
           _skillindex as skillid,
           _skilllevel as level,
           _levellimit as reqlevel,
           _decreasesp,
           _skillexplanationid, _skillexplanationidparam,
           _needskillpoint
    FROM skills_%s_pve s
    INNER JOIN skills
      ON _skillindex = skills._id
    WHERE _needjob > 0
      AND _nameid > 0
    ORDER BY _skillindex, _skilllevel ASC
  sql_query

  conn.exec(query % job['englishname']).each_dnt do |skill|
    jobs[skill['needjob']]['skills'][skill['skillid']]['levels'][skill['level']] = skill

    skillparams = skill['skillexplanationidparam'].to_s
    skillparams = skillparams.split(',').map {|a| a.strip.message_format(messages)}
    jobs[skill['needjob']]['messages'][skill['skillexplanationid']] = messages[skill['skillexplanationid']]

    skill.delete('level')
    skill.delete('skillid')
    skill.delete('needjob')
    skill.delete('skillexplanationidparam')
  end
end


##############################################################################
# get the skill tree for each class
##############################################################################
query = <<sql_query
  SELECT _needjob,
         _skilltableid as skillid,
         _treeslotindex as treeposition,
         _parentskillid1, _parentskillid2,
         _needparentskilllevel1, _needparentskilllevel2,
         _needbasicsp1, _needfirstsp1,
         CASE WHEN _skilltableid IN (
                SELECT _defaultskill1 FROM default_create
          UNION SELECT _defaultskill2 FROM default_create
          UNION SELECT _defaultskill3 FROM default_create
          UNION SELECT _defaultskill4 FROM default_create
          UNION SELECT _defaultskill5 FROM default_create
          UNION SELECT _defaultskill6 FROM default_create
          UNION SELECT _defaultskill7 FROM default_create
          UNION SELECT _defaultskill8 FROM default_create
          UNION SELECT _defaultskill9 FROM default_create
          UNION SELECT _defaultskill10 FROM default_create
        ) THEN TRUE ELSE FALSE END as _default
  FROM skill_tree
  INNER JOIN skills
    ON _skilltableid = skills._id
sql_query

conn.exec(query).each_dnt do |tree|
  job = jobs[tree['needjob']]
  job['skilltree'] << tree
end



##############################################################################
# CREATE: directory
##############################################################################
tokens = JSON_DIRECTORY.split(/[\/\\]/) # don't forget the backslash for Windows!

1.upto(tokens.size) do |n|
  dir = tokens[0..n].join(File::SEPARATOR)
  Dir.mkdir(dir) unless Dir.exist?(dir)
end


##############################################################################
# WRITE: all jobs
##############################################################################
jobs.each_value do |job|
  path = '%s%s%s.json' % [JSON_DIRECTORY, File::SEPARATOR, job['englishname']]
  stream = open(path, 'w')
  #stream.write(JSON.pretty_generate(job))
  stream.write(job.to_json)
  stream.close()
end


##############################################################################
# WRITE: Level and SP for level
##############################################################################
path = '%s%s%s.json' % [JSON_DIRECTORY, File::SEPARATOR, 'sp_by_level']
stream = open(path, 'w')
# stream.write(JSON.pretty_generate(sp_by_level))
stream.write(sp_by_level.to_json)
stream.close()

conn.close()