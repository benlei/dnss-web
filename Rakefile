task :collect => "collect:all"
namespace :collect do
  task :beans do
    ruby "collector/jobs.rb"
    ruby "collector/skills.rb"
    ruby "collector/levels.rb"
  end
  
  task :json do
    ruby "collector/skilltrees.rb"
  end
  
  task :all => [:beans, :json]
end


task :default do
  sh "mvn"
end

task :clean do
  sh "mvn clean"
end

task :install do
  sh "mvn install"
end

task :package do
  sh "mvn package"
end

task :start do
  sh "foreman start"
end
