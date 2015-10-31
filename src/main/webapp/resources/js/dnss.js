var dnss = new (function DNSS() {
  /* Private Fields */
  var t = this;
  var skills = {};
  var positions = [];
  var started = false;
  var loaded = 0;
  var ultimates = [];
  var fullscreen = false;

  this.start = function(v) {
    if (started) { return; }
    started = true;

    window.location.search.substr(1).length > 48 && build.use(window.location.search.substr(1));
    if (build.get(build.FLAG_POS) & 2) {
      toggleFullScreen({which:45});
    }

    // event binding
    $(".skill-tree,#job-sp").on("contextmenu", function(){return false});
    $("#job-sp li").each(function() {
      var advancement = $(this).attr("id").substr(-1);
      $(this).click(function() {
        if (fullscreen) {
          return;
        }

        t.setActive(advancement);
        $(".skill-tree").hide();
        $("#skill-tree-"+advancement).show();
      });
    });

    // obtain json
    for (var i = 0; i < 3; i++) {
      $.getJSON("/json/" + properties.jobs[i].id + ".json?" + v, addSkills);
    }
  };

  function changeCapOrReset() {
    var newCap = parseInt($("#cap").val());
    if (newCap < 1 || newCap > properties.max_cap || newCap != newCap) {
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
      $("#capchanger").val("Reset");
      var date = new Date();
      date.setTime(date.getTime()+1800000);
      document.cookie = "mru_level=" + newCap + ";path=/;expires=" + date.toUTCString();

      properties.sp = json.sp;

      for (var id in skills) {
        skills[id].reset();
        skills[id].setLevel(0);
        skills[id].commit();
      }
    });
  }

  this.setActive = function(advancement) {
    var e = $("#job-sp-"+advancement);
    if (! e.hasClass("active")) {
      $("#job-sp li").removeClass("active");
      e.addClass("active");
    }
  };

  this.getSP = function(advancement) {
    return $("#job-sp-"+advancement+" .sp").html().split("/")[0] || 0;
  };

  this.commit = function(advancement) {
    var curr_sp = this.getSP(advancement);
    var total_sp = $("#total-sp .sp").html().split("/")[1] || 0;
    var sum = 0, new_total;
    for (var i = 24*advancement; i < 24*(advancement+1); i++) {
      if (positions[i]) {
        sum += positions[i].getTotalSPUsage();
      }
    }

    new_total = total_sp - curr_sp + sum;
    $("#job-sp-"+advancement+" .sp").html(sum + "/" + properties.sp[advancement]);
    if (sum > properties.sp[advancement]) {
      $("#job-sp-"+advancement+" .sp").addClass("r");
    } else {
      $("#job-sp-"+advancement+" .sp").removeClass("r");
    }

    var remaining = properties.sp[3] - new_total;
    $("#total-sp .sp").html(remaining + "/" + new_total + "/" + properties.sp[3]);
    if (remaining < 0) {
      $("#total-sp .sp").addClass("r");
    } else {
      $("#total-sp .sp").removeClass("r");
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

  this.getSkills = function() {
    return skills;
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
    positions[0] && description.use(positions[0]);
  }

  this.isFullScreen = function() {
    return fullscreen;
  };

  function toggleFullScreen(e) {
    if (e.which != 45) {
      return;
    }

    var f = build.get(build.FLAG_POS);
    fullscreen = ! fullscreen;
    if(fullscreen) {
      $(".skill-tree").show();
      build.put(build.FLAG_POS, f | 2);
    } else {
      $("#job-sp li.active").click();
      build.put(build.FLAG_POS, f & 0xFD);
    }
    build.commit();
    description.updateTop();
  }

  // "constructor"
  $("#capchanger").click(changeCapOrReset);
  $("#cap").on("input", function(){
    $("#capchanger").val($(this).val() == properties.cap ? "Reset" : "Change");
  });

  $(document).keydown(toggleFullScreen);

  (function(e) {
    var originalY = e.offset().top;
    $(window).scroll(function() {
      var scrollTop = $(window).scrollTop();
      e.css("top", scrollTop < originalY ? 0 : (scrollTop - originalY + 10)+"px");
    });
  })($("#sidebar-1"));
})();