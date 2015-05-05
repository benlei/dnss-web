var description = new (function Description() {
  var t = this;
  var MODE_FLAG = 1;
  var skill = null;

  var name = $("#skill-name"),
    level = $("#skill-level .w"),
    sp = $("#skill-sp .w"),
    total_sp = $("#skill-total-sp .w"),
    cd = $("#skill-cd .w"),
    mp = $("#skill-mp .w"),
    required_level = $("#skill-required-level .w"),
    required_weaps = $("#skill-required-weapon .w"),
    type = $("#skill-type .w"),
    desc = $("#skill-description .d"),
    ndesc = $("#next-description .d"),
    sp_required = $("#sp-required .w"),
    skills_required = $("#skills-required .w");

  this.use = function(s) {
    $("#sidebar-2").show();
    skill = s;

    this.update();
  };

  this.update = function() {
    if (!skill) {
      return;
    }

    setName();
    setLevel();
    setSP();
    setTotalSP();
    setMP();
    setCD();
    setRequiredLevel();
    setRequiredWeapons();
    setType();
    setDescriptions();
    setRequiredSP();
    setRequiredSkills();
  };

  this.hook = function() {
    $("#mode").val(description.getMode());

    if (! skill) {
      return;
    }

    setMP();
    setCD();
    setDescriptions();
  };

  this.getMode = function() {
    return build.get(build.FLAG_POS) & MODE_FLAG ? "pvp" : "pve";
  };

  function currOrNext(e, curr, next) {
    e.html(curr == -1 ? next : curr);
  }

  function showOrHide(e, _e, text) {
    if (text == -1) {
      e.hide();
      return;
    }

    _e.html(text);
    e.show();
  }

  // Name
  function setName() {
    name.html(skill.getName());
  }

  // main block
  function setLevel() {
    currOrNext(level, skill.getLevel() ? skill.getLevel() : -1, skill.getNextLevel());
  }

  function setMP() {
    currOrNext(mp, skill.getLevel() ? skill.getMPUsage(t.getMode()) : -1,  skill.getNextMPUsage(t.getMode()));
  }

  function setRequiredWeapons() {
    showOrHide(required_weaps.parent(), required_weaps, skill.getRequiredWeapons());
  }

  function setType() {
    type.html(skill.getType());
  }

  function setTotalSP() {
    total_sp.html(skill.getTotalSPUsage());
  }

  function setCD() {
    currOrNext(cd, skill.getLevel() ? skill.getCD(t.getMode()) : -1, skill.getNextCD(t.getMode()));
  }

  // requirement block
  function setRequiredLevel() {
    showOrHide(required_level.parent(), required_level, skill.getNextRequiredLevel())
  }

  function setRequiredSkills() {
    showOrHide(skills_required.parent(), skills_required, format(skill.getSkillRequirements()));
  }

  function setRequiredSP() {
    showOrHide(sp_required.parent(), sp_required, format(skill.getSPRequirements()));
  }

  function setSP() {
    sp.html(skill.getNextSPUsage());
  }

  // description block
  function setDescriptions() {
    var d = skill.getDescription(t.getMode()), n = skill.getNextDescription(t.getMode());
    desc.html(format(d == -1 ? n : d));

    ndesc.html(format(n));
    d == -1  || n == -1 ? ndesc.parent().hide() : ndesc.parent().show();
  }


  function format(str) {
    if (str == -1) {
      return -1;
    }

    var c = 0, w = 0, p = 0, newStr = "", startPos = 0;
    for (var i = 0; i < str.length - 1; i++) {
      switch (str.substr(i, 2)) {
        case "#y": case "#p": case "#r":
          if (c - w == 1) { // needed a closing </span>
            newStr += str.substring(startPos, i) + "</span><span class=\"" + str.substr(i+1,1) + "\">";
          } else {
            newStr += str.substring(startPos, i) + "<span class=\"" + str.substr(i+1,1) + "\">";
            c++;
          }

          startPos = i + 2;
          ++i;
          break;
        case "#w":
          if (w == c) { // early #w
            newStr +=  str.substring(startPos, i);
          } else {
            newStr += str.substring(startPos, i) + "</span>";
            w++;
          }

          startPos = i + 2;
          ++i;
        default:
          break;
      }
    }

    newStr = newStr + str.substr(startPos);

    if (c != w) {
      newStr = newStr + "</span>";
    }

    return newStr.replace(/\\n/g, "<br />");
  }

  $("#mode").click(function() {
    var mode = build.get(build.FLAG_POS) & MODE_FLAG;
    var flag  = (build.get(build.FLAG_POS) >> 1) << 1;
    build.put(build.FLAG_POS, flag | (mode?0:1));
    t.updateModeHook();
  });
})();