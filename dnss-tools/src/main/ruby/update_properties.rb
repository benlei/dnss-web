#!/bin/ruby
require 'java-properties'
require_relative 'common'

properties_file = ROOT+"/"+DNSS['web.resource_path']+"/dnss.properties"
properties_file.gsub!(/[\/\\]/, "/")
properties = JavaProperties.load(properties_file)
property = ARGV[0] + '.version'
property = property.to_sym
properties[property] = properties[property].to_i + 1
JavaProperties.write(properties, properties_file)
puts properties[property]