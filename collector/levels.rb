#!/bin/ruby
require_relative 'common'

conn = createPGConn()

##############################################################################
# Hopefully only thing you have to edit
##############################################################################
LEVEL_CAP = 90

##############################################################################
# get sp required of all classes [write]
##############################################################################
sp_by_level = Array.new
sp_by_level << 0
query = <<sql_query
  SELECT _id, _skillpoint
  FROM playerleveltable
  WHERE _id <= %d
  ORDER BY _id ASC
sql_query

conn.exec(query % LEVEL_CAP).each_dnt do |level|
  sp_by_level << sp_by_level[level['id'] - 1] + level['skillpoint']
end

sp_by_level.shift

##############################################################################
# generate the bean file
##############################################################################
builder = Nokogiri::XML::Builder.new do |xml|
  xml.beans(SPRING_BEAN_HEADER) do
    xml.bean('id' => 'sp', 'class' => 'dnss.model.SP', 'scope' => 'singleton') do
      xml.property('name' => 'sp') do
        xml.list('value-type' => 'int') do
          sp_by_level.each do |sp|
            xml.value_ sp
          end
        end
      end
    end
  end
end

mkdir_p(BEAN.gsub(/[\/\\]/, "/"))

##############################################################################
# WRITE: Bean
##############################################################################
path = '%s/%s.xml' % [BEAN, 'levels-context']
stream = open(path, 'w')
stream.write(builder.to_xml)
stream.close()

puts "%s has been created." % path

conn.close()

