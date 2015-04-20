var dnss = new (function DNSS() {
  /* Private Fields */
  var t = this;
  var skills = {};
  var positions = [];
  var common = {};
  var started = false;
  var loaded = 0;
  var spHooks = [];
  var skillHooks = {};

  this.changeLevelCap = function(lvl) {
    // can be but will not be updated
  };

  this.start = function() {
    if (started) { return; }
    started = true;

    window.location.search.substr(1).length > 48 && build.use(window.location.search.substr(1));

    // event binding
    $(".skill-tree,#job-sp").on("contextmenu", function(){return false});
    $("#job-sp li[id]").each(function() {
      var advancement = $(this).attr("id").substr(-1);
      $(this).click(function() {
        $("#job-sp li").removeClass("active");
        $(this).addClass("active");
        $(".skill-tree").hide();
        $("#skill-tree-"+advancement).show();
      });
    });

    // obtain json
    for (var i = 0; i < properties.jobs.length; i++) {
      spHooks[i] = [];
      $.getJSON("/json/" + properties.version.json + "-" + properties.jobs[i].id + "-skills.json", addSkills);
    }

    $.getJSON("/json/" + properties.version.json + "-common.json", function(json) {
      common = json;
    });

    for (var i = 0; i < properties.jobs.length; i++) {
      $.getJSON("/json/" + properties.version.json + "-" + properties.jobs[i].id + "-messages.json", function(json) {
        for (var j in json) {
          messages.put(j, json[j]);
        }
      });
    }
  };

  this.getSkillType = function(id) {
    return common.types.skills[id];
  };

  this.getWeaponType = function(id) {
    return common.types.weapons[id];
  };

  this.commit = function(advancement) {
    var curr_sp = $("#job-sp-"+advancement+" .sp").html().split("/")[0] || 0;
    var total_sp = $("#job-sp li:last .sp").html().split("/")[0] || 0;
    var sum = 0, new_total;
    for (var i = 24*advancement; i < 24*(advancement+1); i++) {
      if (positions[i]) {
        sum += positions[i].getTotalSPUsage();
      }
    }

    new_total = total_sp - curr_sp + sum;
    $("#job-sp-"+advancement+" .sp").html(sum + "/" + properties.max.sp[advancement]);
    if (sum > properties.max.sp[advancement]) {
      $("#job-sp-"+advancement+" .sp").addClass("r");
    } else {
      $("#job-sp-"+advancement+" .sp").removeClass("r");
    }

    $("#job-sp li:last .sp").html(new_total + "/" + properties.max.sp[properties.max.sp.length - 1]);
    if (new_total > properties.max.sp[properties.max.sp.length - 1]) {
      $("#job-sp li:last .sp").addClass("r");
    } else {
      $("#job-sp li:last .sp").removeClass("r");
    }

    build.notify();

    for (var i = 0; i < spHooks[advancement].length; i++) {
      skills[spHooks[advancement][i]].notifySP(advancement, sum);
    }
  };


  this.addSPHook = function(adv, skillId) {
    spHooks[adv].push(skillId);
  };

  this.addSkillHook = function(id, req) {
    if (skills[req]) {
      skills[req].addHook(skills[id]);
      return;
    }

    if (!skillHooks[req]) {
      skillHooks[req] = [];
    }

    skillHooks[req].push(id);
  };

  function addSkills(json) {
    var someSkill;
    $.each(json, function(id, data) {
      var skill = new Skill(id, data, $("#skill-" + id));
      var pos = skill.getPosition();
      skills[id] = skill;
      positions[pos] = skill;
      skill.setLevel(build.get(pos));
      skill.commit();

      // add lingering hooks
      if (skillHooks[id]) {
        while (skillHooks[id].length) {
          skill.addHook(skills[skillHooks[id].shift()]);
        }
      }

      someSkill = skill;
    });
    t.commit(someSkill.getAdvancement());
  }
})();