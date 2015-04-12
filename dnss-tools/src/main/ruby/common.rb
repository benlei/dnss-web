##############################################################################
# connect to postgresql server
##############################################################################
@conn = PG.connect(:host => 'localhost', :port => 5432,
                  :user => 'dnss', :password => 'dnss',
                  :dbname => 'dnss')
@conn.type_map_for_results = PG::BasicTypeMapForResults.new @conn

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

class Array
  def skilltree_partition()
    slices = self.each_slice(4).to_a
    (1..(4 - slices.last.length)).each {slices.last << 0}
    (slices.length..6-1).each { slices << [0,0,0,0] }
    slices
  end
end

class NilClass
  def to_bean
    '<null />'
  end
end

class Integer
  def to_bean
    return self.to_s
  end
end



##############################################################################
# func: creates directories
##############################################################################
def mkdir_p(directory)
  tokens = directory.split(/[\/\\]/)

  1.upto(tokens.size) do |n|
    dir = tokens[0..n].join(File::SEPARATOR)
    Dir.mkdir(dir) unless Dir.exist?(dir)
  end
end

def create_json_file(path, json)
  stream = open(path, 'w')
  stream.write(json)
  stream.close()
  puts '%s created' % path
end


##############################################################################
# Combine assassin tables
##############################################################################
query = <<sql_query
  INSERT INTO skills_assassin_%1$s
    SELECT *
    FROM skills_assassin_bringer_%1$s
    WHERE _id NOT IN (
      SELECT _id
      FROM skills_assassin_%1$s
    )
sql_query
@conn.exec(query % 'pve')
@conn.exec(query % 'pvp')
