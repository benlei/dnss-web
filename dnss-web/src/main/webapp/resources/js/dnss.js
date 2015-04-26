var dnss = new (function DNSS() {
  /* Private Fields */
  var t = this;
  var skills = {};
  var positions = [];
  var started = false;
  var loaded = 0;
  var ultimates = [];

  function changeLevelCapOrReset() {
    var newCap = parseInt($("#cap").val());
    if (newCap < 1 || newCap > 80 || newCap != newCap) {
      alert($("#cap").val() + " is not a valid level cap.");
      $("#cap").val(properties.cap);
      return;
    }


    if (newCap == properties.cap) {
      for (var id in skills) {
        skills[id].setLevel(0);
        skills[id].commit();
      }
      return;
    }

    $.getJSON("/api/level/" + newCap, function(json) {
      properties.cap = newCap;

      // reset max required level
      for (var i = 0; i < 3; i++) {
        properties.max.required_level[i] = i == 1 ? Math.min(70, newCap): newCap;
      }

      properties.max.sp = json.sp;

      for (var id in skills) {
        skills[id].reset();
        skills[id].setLevel(0);
        skills[id].commit();
      }
    });
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
    for (var i = 0; i < 3; i++) {
      $.getJSON("/json/" + properties.version.json + "-" + properties.jobs[i].id + ".json", addSkills);
    }
  };

  this.getSP = function(advancement) {
    return $("#job-sp-"+advancement+" .sp").html().split("/")[0] || 0;
  };

  this.commit = function(advancement) {
    var curr_sp = this.getSP(advancement);
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

    $("#job-sp li:last .sp").html(new_total + "/" + properties.max.sp[3]);
    if (new_total > properties.max.sp[3]) {
      $("#job-sp li:last .sp").addClass("r");
    } else {
      $("#job-sp li:last .sp").removeClass("r");
    }

    build.commit();
    notifier.notify(notifier.SP, advancement, sum);
  };

  this.getSkill = function(id) {
    return skills[id];
  };

  this.getUltimateSkills = function() {
    return ultimates;
  };

  function addSkills(json) {
    for (var mid in json.messages) { // should be pretty fast to do
      messages.put(mid, json.messages[mid]);
    }

    var someSkill;
    $.each(json.skills, function(id, data) {
      var skill = new Skill(id, data, $("#skill-" + id));
      var pos = skill.getPosition();
      skills[id] = skill;
      positions[pos] = skill;
      skill.setLevel(build.get(pos)+skill.getMinLevel());
      skill.ultimate && ultimates.push(skill);
      skill.commit();
      someSkill = skill;
    });
    t.commit(someSkill.getAdvancement());
  }

  $("#capbutton").click(changeLevelCapOrReset);
  $("#cap").keyup(function(){
    if ($(this).val() == properties.cap) {
      $("#capbutton").val("Reset");
      return;
    }

    $("#capbutton").val("Change");
  });
  $("form").submit(function(){ return false; });
})();