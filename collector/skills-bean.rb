#!/bin/ruby
require "json"
require "nokogiri"
require_relative "common"

# Gets each skill and its levels
# Example:
#  <bean id="skill_7204" class="dnss.model.Skill" scope="prototype">
#    <property name="id" value="7204"/>
#    <property name="sprite" value="11"/>
#    <property name="icon" value="40"/>
#    <property name="levels">
#      <list>
#        <bean class="dnss.model.Level" scope="prototype">
#          <property name="level" value="1"/>
#          <property name="requiredJobLevel" value="55"/>
#          <property name="spCost" value="0"/>
#          <property name="totalSPCost" value="0"/>
#        </bean>
#      </list>
#    </property>
#  </bean>

conn = createPGConn()

# var declarations
tables = Array.new
skills = Hash.new

# get all skill tables
rs = conn.exec("SELECT table_name FROM information_schema.tables WHERE table_name LIKE 'skillleveltable\\_character%'")
rs.each do |r|
  if not r["table_name"].end_with?("characteretc")
    tables << r["table_name"]
  end
end

query = <<QUERY
SELECT _needjob,
       _skillindex as id,
       _skilllevel,
       _levellimit as required_level,
       _needskillpoint as spcost,
       _iconimageindex
FROM %s
INNER JOIN skilltable_character skills
  ON _skillindex = skills._id
INNER JOIN skilltreetable t
  ON _skillindex = _skilltableid
INNER JOIN jobtable j
  ON _needjob = j._id
WHERE _applytype = %d
  AND _service IS TRUE
  AND _skilllevel <= _maxlevel
ORDER BY _skillindex, _skilllevel ASC
QUERY

tables.each do |table|
  if table.end_with?("pvp")
    next
  end
  
  rs = conn.exec(query % [table, 0])
  rs.each_dnt do |r|
    if not skills.has_key?(r["id"])
      skills[r["id"]] = Hash.new
      skills[r["id"]]["id"] = r["id"]
      skills[r["id"]]["image"] = "%02d" % ((r["iconimageindex"] / 200) + 1)
      skills[r["id"]]["icon"] = r["iconimageindex"] % 200
      skills[r["id"]]["levels"] = Array.new
    end

    levels = skills[r["id"]]["levels"]
    levels[r["skilllevel"] - 1] = {
      "spcost"         => r["spcost"],
      "required_level" => r["required_level"]
    }
  end
end

##############################################################################
# generate the bean file
##############################################################################
builder = Nokogiri::XML::Builder.new do |xml|
  xml.beans(SPRING_BEAN_HEADER) do
    skills.each do |id, skill|
      xml.bean("id" => "skill_#{id}", "class" => "dnss.model.Skill", "scope" => "prototype") do
        xml.property("name" => "id", "value" => id)
        xml.property("name" => "sprite", "value" => skill["image"])
        xml.property("name" => "icon", "value" => skill["icon"])
        total = 0
        xml.property("name" => "levels") do
          xml.list do
            skill["levels"].each_with_index do |level, i|
              total += level["spcost"]
              xml.bean("class" => "dnss.model.Level", "scope" => "prototype") do
                xml.property("name" => "level", "value" => i+1)
                xml.property("name" => "requiredJobLevel", "value" => level["required_level"])
                xml.property("name" => "spCost", "value" => level["spcost"])
                xml.property("name" => "totalSPCost", "value" => total)
              end
            end
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
path = "%s/%s.xml" % [BEAN, "skills-context"]
stream = open(path, "w")
stream.write(builder.to_xml)
stream.close()

puts "%s has been created." % path
conn.close()