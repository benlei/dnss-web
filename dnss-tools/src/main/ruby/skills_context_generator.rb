#!/bin/ruby
require 'json'
require 'nokogiri'
require_relative 'common'

##############################################################################
# Hopefully only thing you have to edit
##############################################################################
BEAN_DIRECTORY = ROOT+'/'+DNSS['web.webinf_path']

conn = createPGConn()
# what's needed:
# skill levels
# skill sprite
# skill icon position

##############################################################################
# gets all the jobs [write]
# notes:
#   jobnumber => 0 = base, 1 = first advancement, etc.
##############################################################################
jobs = Hash.new
query = <<sql_query
  SELECT j._id,
         LOWER(_englishname) as englishname,
         _jobnumber as advancement
  FROM jobs j
  WHERE _service is TRUE
sql_query

conn.exec(query).each_dnt do |job|
  job['skills'] = Hash.new
  job['skilltree'] = Array.new
  jobs[job['id']] = job
  jobs[job['id']].delete('id')
end


##############################################################################
# get all the skills of all classes
##############################################################################
query = <<sql_query
  SELECT _id,
         _iconimageindex,
         _needjob
  FROM skills
  WHERE _id IN (
      SELECT _skilltableid
      FROM skill_tree
  )
  ORDER BY _needjob ASC
sql_query

conn.exec(query).each_dnt do |skill|
  s = Hash.new
  s['levels'] = Array.new
  s['image'] = '%02d' % ((skill['iconimageindex'] / 200) + 1)
  s['icon'] = skill['iconimageindex'] % 200
  jobs[skill['needjob']]['skills'][skill['id']] = s
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
           _needskillpoint as spcost
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

  conn.exec(query % [job['englishname'], 'pve']).each_dnt do |skill|
    jobs[skill['needjob']]['skills'][skill['id']]['levels'] << skill
    ['id', 'skilllevel', 'needjob'].each {|a| skill.delete(a)}
  end
end

##############################################################################
# generate the bean file
##############################################################################
builder = Nokogiri::XML::Builder.new do |xml|
  xml.beans(SPRING_BEAN_HEADER) do
    # First create a bean for each skill
    jobs.each_value do |job|
      job['skills'].each do |id, skill|
        xml.bean('id' => "skill_#{id}", 'class' => 'dnss.model.Skill', 'scope' => 'prototype') do
          xml.property('name' => 'id', 'value' => id)
          xml.property('name' => 'sprite', 'value' => skill['image'])
          xml.property('name' => 'icon', 'value' => skill['icon'])
          total = 0
          xml.property('name' => 'levels') do
            xml.list do
              skill['levels'].each_with_index do |level, i|
                total += level['spcost']
                xml.bean('class' => 'dnss.model.Level', 'scope' => 'prototype') do
                  xml.property('name' => 'level', 'value' => i+1)
                  xml.property('name' => 'requiredJobLevel', 'value' => level['required_level'])
                  xml.property('name' => 'spCost', 'value' => level['spcost'])
                  xml.property('name' => 'totalSPCost', 'value' => total)
                end
              end
            end
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
path = '%s/%s.xml' % [BEAN_DIRECTORY, 'skills-context']
stream = open(path, 'w')
stream.write(builder.to_xml)
stream.close()

puts "%s has been created." % path
conn.close()