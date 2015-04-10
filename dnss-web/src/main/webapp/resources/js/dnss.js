function DNSS(o) {
  /* Private Fields */
  var build = Array(72 + 1 + 1).join("-").split("");
  var skills = {};
  var common = {};

  this.changeLevelCap = function(lvl) {
    // can be but will not be updated
  };

  /* starts the idempotent JSON stuff */
  this.start = function() {
    for (var i = 0; i < o.jobs.length; i++) {
      $.getJSON("/json/" + version.json + "-" + o.jobs[i].id + "-skills.json", addSkills);
    }

    $.getJSON("/json/" + version.json + "-common.json", function(json) { common = json; });

    for (var i = 0; i < o.jobs.length; i++) {
      $.getJSON("/json/" + version.json + "-" + o.jobs[i].id + "-messages.json", function(json) {
        $.each(json, function(j, message) { messages.put(j, message); });
      });
    }
  };

  this.getSkillType = function(id) { return common.types.skills[id]; };
  this.getWeaponType = function(id) { return common.types.weapons[id]; };

  this.getMaxRequiredLevel = function(adv) { return o.max.required_level[adv] };
  this.getMaxSP = function(adv) { return o.max.sp[adv] };

  this.updateBuild = function(p, l) { build[p] = buildMap[l] };
  this.setBuild = function(a) {
    var e = $("#job-sp li[data-job="+a+"] .sp");
    var curr_sp = parseInt(e.html().split("/")[0]);
    for (var i = 0; i < )

  };

  function addSkills(json) {
    var someSkill;
    $.each(json, function(id, data) {
      var skill = new Skill(id, data, $(".skill[data-id=" + id + "]"));
      skills[id] = skill;
      skill.setLevel(iBuildMap[build[skill.getPosition()]]);
      skill.set();
      someSkill = skill;
    });


  }



  if (o.build.length > 32) {
    var b = o.build.match(/^[0-9a-zA-Z-]+/g);
    if (b) {
      b = b.shift().split("");
      build[build.length - 1] = b[b.length - 1];
      for (var i = 0; i < b.length - 1; i++) {
        build[i] = b[i];
      }
    }
  }
}