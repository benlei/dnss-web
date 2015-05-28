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
