function DNSS() {
  /* Private Fields */
  var t = this;
  var skills = {};
  var positions = [];
  var common = {};

  this.changeLevelCap = function(lvl) {
    // can be but will not be updated
  };

  /* starts the idempotent JSON stuff */
  function start() {
    // event binding
    $(".skill-tree,#job-sp").on("contextmenu", function(){return false});
    $("#job-sp li[id]").each(function() {
      var advancement = $(this).attr("id").substr(-1);
      $(this).click(function() {
        $("#job-sp .active").removeClass("active");
        $(this).addClass("active");
        $(".skill-tree:visible").hide();
        $("#skill-tree-"+advancement).show();
      });
    });

    for (var i = 0; i < properties.jobs.length; i++) {
      $.getJSON("/json/" + properties.version.json + "-" + properties.jobs[i].id + "-skills.json", addSkills);
    }

    for (var i = 0; i < properties.jobs.length; i++) {
      $.getJSON("/json/" + properties.version.json + "-" + properties.jobs[i].id + "-messages.json", function(json) {
        $.each(json, function(j, message) { messages.put(j, message); });
      });
    }
  };

  this.getSkillType = function(id) { return common.types.skills[id]; };
  this.getWeaponType = function(id) { return common.types.weapons[id]; };

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
    $("#job-sp li:last .sp").html(new_total + "/" + properties.max.sp[properties.max.sp.length - 1]);

    build.notify();
  };

  function addSkills(json) {
    var someSkill;
    $.each(json, function(id, data) {
      var skill = new Skill(id, data, $("#skill-" + id));
      var pos = skill.getPosition();
      skills[id] = skill;
      positions[pos] = skill;
      skill.setLevel(iBuildMap[build.get(pos)]);
      skill.commit();
      someSkill = skill;
    });
    t.commit(someSkill.getAdvancement());
  }

  start();
}