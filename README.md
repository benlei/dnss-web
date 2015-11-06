# dnss-web
Dragon Nest Skill Simulator

This project was made as a quick a dirty solution for having a Dragon Nest Skill Simulator.
It works, but it could be A LOT better. Unfortunately it relies on quite a few software in
order to get working, and even then that may not be enough -- Eyedentity/Nexon tends to change/add
to the existing structure of when this project first started, making such a project non-maintainable
unless a person can understand what was changed and work around it as such.

# Gather data

For this project, and this project in particular, I would recommend you also make use of the toolset I made:

  <https://github.com/ben-lei/dnss-tools>

Just make relevant changes to the dnss.ini, build, and check the dnt/pak directories for a target/appassembler/bin directory.
For Windows users you should be running the .bat files. For Unix, use the shell ones (no extension).

Here is my recommendation about how to handle data extraction/updating your own branch for Windows users:

**DISCLAIMER: Some paths are hard coded everywhere; please adjust as needed**

- Install DragonNest if you haven't already.
- Create/use a VM and mount the DragonNest directory onto the VM (let's call it /mnt/dragonnest).
   
   Everything from here on out is for the VM
- Some software of mention to install would be:
   - Java 8
   - Maven (technically need to download the binaries from website, add to PATH, etc.)
   - Ruby 2.2 (maybe higher is OK)
   - PostgreSQL - make sure to set the pg_hba.conf to trust method; setup user/etc. (I think you can use the default postgresql user if you wanted to)
   - Ruby Nokogiri Gem
   - Ruby pg Gem
   - Ruby inifile Gem
   - ImageMagick
   - pngcrush

## dnss-tools
1. Modify the dnss.ini to point to relevant location (/mnt/dragonnest/...)
1. Run the pak extractor.
1. Run the dnt processor (known bug: some DNTs can't be extracted; I know why but it's not a big deal and in general those that can't be extracted aren't relevant for this either).

  The dnt processor basically processes DNT files and creates identical SQL tables for you/the collector to use later. The collector was something I made when I was rethinking of how to gather data, but in the end it wasn't really needed (different structure than what is necessary for DNSS; more free to change stuff in Ruby).
   
  You can install pgAdmin and query the tables if you're curious as to what's what. It really helps if you have visuals to start connecting things together.

*Note that in the dnss-tools project, the rakefile has an update task (`rake update`) to attempt to update the resource
paks without needing to actually install DragonNest (if you already have the base files).*

## dnss-web again
Still under the VM, but in the dnss-web project instead.

  1. Modify the Rakefile and change the resource variable. Also you have to copy over your mounted DragonNest's Version.cfg file to the root of your output directory.
  1. Modify the collector/common.rb and change the PostgreSQL info / uistring path.
  1. Modify the LEVEL_CAP in the collector/levels.rb if necessary.
  1. You may have to manually add relevant entries if a new class is added to the game.
  1. Run `rake collect`
  
     First the beans (XML) related files are generated + the version.cfg file is copied over, then the JSON files for each class is gnerated, and finally imagemagick + pngcrush are used to convert + compress the DDS icons. In an ideal situation where everything is configured correctly and Eyedentity/Nexon didn't ninja something in that breaks everything, this process should only take a few minutes (ideally under 1 minute).
     
In an ideal scenario where everything is right, all that needs to be done is:
  1. Extract pak (in the dnss-tools project)
  2. Process DNT (in the dnss-tools project)
  3. rake all (in the dnss-web project)

# Future
At some point I do want to decomission work on this particular repo. The future I am envisioning is:
 - <https://github.com/ben-lei/dncli> - a single toolset that uses JavaScript to gather and compile data.
 - <https://github.com/ben-lei/dnss-nodejs> - a NodeJS implementation for DNSS. DNSS isn't computation hungry is actually a really simple app. Writing a Java webapp was overkill for this project. But it's already here, so I'll leave it here.

