#!/bin/ruby
require 'java-properties'
properties_file = "C:\\Users\\Ben\\IdeaProjects\\dnss\\dnss-web\\src\\main\\resources\\dnss.properties"
properties = JavaProperties.load(properties_file)
property = ARGV[0] + '.version'
property = property.to_sym
properties[property] = properties[property].to_i + 1
JavaProperties.write(properties, properties_file)
puts properties[property]