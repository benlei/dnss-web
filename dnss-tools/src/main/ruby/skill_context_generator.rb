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
# what's needed:
# skill levels
# skill sprite
# skill icon position
conn.close()