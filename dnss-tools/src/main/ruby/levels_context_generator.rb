#!/bin/ruby
require 'json'
require 'nokogiri'
require_relative 'common'
require_relative 'dn-weapons'
require_relative 'dn-skills'

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
# generate the bean file
##############################################################################
builder = Nokogiri::XML::Builder.new do |xml|
  xml.beans(SPRING_BEAN_HEADER) do
    xml['util'].list('id' => 'levels', 'value-type' => 'int') {sp_by_level.each {|sp| xml.value_ sp}}
  end
end

BEAN_DIRECTORY.gsub!(/[\/\\]/, "/")
mkdir_p(BEAN_DIRECTORY)

##############################################################################
# WRITE: Bean
##############################################################################
path = '%s/%s.xml' % [BEAN_DIRECTORY, 'levels-context']
stream = open(path, 'w')
stream.write(builder.to_xml)
stream.close()

puts "%s has been created." % path

conn.close()

