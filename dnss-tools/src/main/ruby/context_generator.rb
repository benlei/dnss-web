#!/bin/ruby
require 'json'
require 'nokogiri'
require_relative 'common'
require_relative 'dn-weapons'
require_relative 'dn-skills'

if ARGV[0] == "-fix"
  fixTables()
end

conn = createPGConn()

##############################################################################
# Hopefully only thing you have to edit
##############################################################################
BEAN_DIRECTORY = ROOT+'/'+DNSS['web.webinf_path']
LEVEL_CAP = DNSS['dn.level_cap']

##############################################################################
# get sp required of all classes [write]
##############################################################################
sp_by_level = Array.new
sp_by_level << 0
query = <<sql_query
  SELECT _id, _skillpoint
  FROM player_level
  WHERE _id <= %d
  ORDER BY _id ASC
sql_query

conn.exec(query % LEVEL_CAP).each_dnt do |level|
  sp_by_level << sp_by_level[level['id'] - 1] + level['skillpoint']
end

##############################################################################
# gets all the jobs [write]
# notes:
#   jobnumber => 0 = base, 1 = first advancement, etc.
#   maxspjobx => # ratio to max SP (floored); at the moment 3 and 4 are unused
##############################################################################
jobs = Hash.new
query = <<sql_query
  SELECT j._id,
         m._data as jobname,
         LOWER(_englishname) as identifier,
         _jobnumber as advancement,
         _parentjob,
         _maxspjob0, _maxspjob1, _maxspjob2
  FROM jobs j
  INNER JOIN messages m
    ON _jobname = m._id
  WHERE _service is TRUE
  ORDER BY _id ASC
sql_query

conn.exec(query).each_dnt do |job|
  job['skilltree'] = Array.new
  job['images'] = Array.new
  jobs[job['id']] = job
  job['spRatio'] = [job['maxspjob0'], job['maxspjob1'], job['maxspjob2']]
  ['maxspjob0', 'maxspjob1', 'maxspjob2'].each {|a| job.delete(a)}
  jobs[job['id']].delete('id')
end

##############################################################################
# get the skill tree for each class
##############################################################################
query = <<sql_query
  SELECT _needjob,
         _skilltableid as skillid,
         _treeslotindex
  FROM skill_tree
  INNER JOIN skills
    ON _skilltableid = skills._id
sql_query
conn.exec(query).each_dnt do |tree|
  job = jobs[tree['needjob']]
  job['skilltree'][tree['treeslotindex']] = tree['skillid']
end


##############################################################################
# generate the bean file
##############################################################################
beans = {'xmlns' => 'http://www.springframework.org/schema/beans',
         'xmlns:xsi' => 'http://www.w3.org/2001/XMLSchema-instance',
         'xmlns:util' => 'http://www.springframework.org/schema/util',
         'xsi:schemaLocation' => ['http://www.springframework.org/schema/beans',
                                  'http://www.springframework.org/schema/beans/spring-beans.xsd',
                                  'http://www.springframework.org/schema/util',
                                  'http://www.springframework.org/schema/util/spring-util.xsd'
                                 ].join(' ')}

builder = Nokogiri::XML::Builder.new do |xml|
  xml.beans(beans) do
    jobs.each_value do |job|
      job['skilltree'] = job['skilltree'].skilltree_partition()

      xml.bean('id' => 'job_' + job['identifier'], 'class' => 'dnss.model.Job') do
        xml.property('name' => 'name', 'value' => job['jobname'])
        xml.property('name' => 'identifier', 'value' => job['identifier'])
        xml.property('name' => 'advancement', 'value' => job['advancement'])
        xml.property('name' => 'parent', 'ref' => 'job_' + jobs[job['parentjob']]['identifier']) unless job['parentjob'] == 0
        xml.property('name' => 'spRatio') {xml.list {job['spRatio'].each {|spRatio| xml.value_ spRatio}}}
        xml.property('name' => 'skillTree') {xml.list {job['skilltree'].each {|skillblock| xml.list {skillblock.each {|skill| xml.value_ skill.to_i}}}}}
      end
    end

    jobs.select {|id, job| job['advancement'] == 2}.each_value do |job|
      xml.bean('id' => 'jobs_' + job['identifier'], 'class' => 'dnss.model.Jobs') do
        xml.property('name' => 'primary', 'ref' => 'job_' + jobs[jobs[job['parentjob']]['parentjob']]['identifier'])
        xml.property('name' => 'secondary', 'ref' => 'job_' + jobs[job['parentjob']]['identifier'])
        xml.property('name' => 'tertiary', 'ref' => 'job_' + job['identifier'])
      end
    end

    xml['util'].list('id' => 'levels', 'value-type' => 'int') {sp_by_level.each {|sp| xml.value_ sp}}
    adv = ['primary', 'secondary', 'tertiary']
    (0..2).each do |a|
      xml['util'].list('id' => 'all_jobs_%s' % adv[a], 'value-type' => 'dnss.model.Job') do
        jobs.select {|id, job| job['advancement'] == a}.each_value do |job|
          xml.ref('bean' => 'job_' + job['identifier'])
        end
      end
    end

    xml['util'].list('id' => 'skill_types', 'value-type' => 'java.lang.String') {DN_SKILL_TYPES.each {|t| xml.value_ t}}

    DN_WEAPON_TYPES.each do |base, weaps|
      xml.bean('id' => base+"_weapons", 'class' => 'java.util.HashMap') do
        xml.send('constructor-arg') do
          xml.map('key-type' => "java.lang.String", 'value-type' => 'java.lang.String') do
            weaps.each {|k,v| xml.entry('key' => k, 'value' => v)}
          end
        end
      end
    end
  end
end

BEAN_DIRECTORY.gsub!(/[\/\\]/, "/")
mkdir_p(BEAN_DIRECTORY)

##############################################################################
# WRITE: Bean
##############################################################################
path = '%s/%s.xml' % [BEAN_DIRECTORY, 'spring-context']
stream = open(path, 'w')
stream.write(builder.to_xml)
stream.close()

puts "%s has been created." % path

conn.close()

