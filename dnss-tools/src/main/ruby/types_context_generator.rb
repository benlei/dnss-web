#!/bin/ruby
require 'json'
require 'nokogiri'
require_relative 'common'
require_relative 'dn-weapons'
require_relative 'dn-skills'

##############################################################################
# Hopefully only thing you have to edit
##############################################################################
BEAN_DIRECTORY = ROOT+'/'+DNSS['web.webinf_path']

##############################################################################
# generate the bean file
##############################################################################
builder = Nokogiri::XML::Builder.new do |xml|
  xml.beans(SPRING_BEAN_HEADER) do
    xml['util'].list('id' => 'skill_types', 'value-type' => 'java.lang.String', 'scope' => 'singleton') {DN_SKILL_TYPES.each {|t| xml.value_ t}}

    DN_WEAPON_TYPES.each do |base, weaps|
      xml.bean('id' => base+"_weapons", 'class' => 'java.util.HashMap', 'scope' => 'singleton') do
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
path = '%s/%s.xml' % [BEAN_DIRECTORY, 'types-context']
stream = open(path, 'w')
stream.write(builder.to_xml)
stream.close()

puts "%s has been created." % path