function DNSS(o) {
  /* Private Fields */
  var t = this;
  var build = Array(72 + 1 + 1).join("-").split("");
  var skills = {};
  var positions = [];
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

  this.updateBuild = function(position, level) { build[position] = buildMap[level] };
  function commitBuildURL() {
    $("#build").val(window.location.protocol + "//" + window.location.host + "/job/" + o.base + "?" + build.join(""));
  }

  this.commitJobSP = function(advancement) {
    var curr_sp = $("#job-sp-"+advancement+" .sp").html().split("/")[0] || 0;
    var total_sp = $("#job-sp li:last .sp").html().split("/")[0] || 0;
    var sum = 0, new_total;
    for (var i = 24*advancement; i < 24*(advancement+1); i++) {
      if (positions[i]) {
        sum += positions[i].getTotalSPUsage();
      }
    }

    new_total = total_sp - curr_sp + sum;
    $("#job-sp-"+advancement+" .sp").html(sum + "/" + o.max.sp[advancement]);
    $("#job-sp li:last .sp").html(new_total + "/" + o.max.sp[o.max.sp.length - 1]);

    commitBuildURL();
  };

  function addSkills(json) {
    var someSkill;
    $.each(json, function(id, data) {
      var skill = new Skill(id, data, $("#skill-" + id));
      var pos = skill.getPosition();
      skills[id] = skill;
      positions[pos] = skill;
      skill.setLevel(iBuildMap[build[pos]]);
      skill.commit();
      someSkill = skill;
    });
    t.commitJobSP(someSkill.getAdvancement());
  }


  // constructor stuff
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