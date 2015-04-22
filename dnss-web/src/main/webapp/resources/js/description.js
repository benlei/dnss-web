var description = new (function Description() {
  var t = this;
  var MODE_FLAG = 1;
  var separator = "  &rarr; ";
  var skill = null;

  var $ndesc = $("#next-description");

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

  this.getSeparator = function() {
    return separator;
  };

  this.use = function(s) {
    $("#sidebar-2").show();
    skill = s;

    this.update();
  };

  this.update = function() {
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

  function setName() {
    name.html(skill.getName());
  }

  function setSP() {
    sp.html(skill.getNextSPUsage());
  }

  function setTotalSP() {
    total_sp.html(skill.getTotalSPUsage());
  }

  function setRequiredWeapons() {
    required_weaps.html(skill.getRequiredWeapons());
  }

  function setType() {
    type.html(skill.getType());
  }

  function setDescriptions() {
    var d = skill.getDescription(t.getMode()), n = skill.getNextDescription(t.getMode());
    desc.html(format(d == -1 ? n : d));

    ndesc.html(format(n));
    d == -1  || n == -1 ? $ndesc.hide() : $ndesc.show();
  }

  function setRequiredSP() {
    sp_required.html(format(skill.getSPRequirements()));
  }

  function setRequiredSkills() {
    skills_required.html(format(skill.getSkillRequirements()));
  }

  function html(e, curr, next) {
    if (curr == -1) {
      e.html(next);
    } else if (next == -1) {
      e.html(curr);
    } else {
      e.html(curr + separator + next);
    }
  }

  function setLevel() {
    html(level, skill.getLevel() ? skill.getLevel() : -1, skill.getNextLevel());
  }

  function setMP() {
    html(mp, skill.getMPUsage(t.getMode()) == skill.getNextMPUsage(t.getMode()) ? -1 : skill.getMPUsage(t.getMode()),
      skill.getNextMPUsage(t.getMode()));
  }

  function setCD() {
    html(cd, skill.getCD(t.getMode()) == skill.getNextCD(t.getMode()) ? -1 : skill.getCD(t.getMode()),
      skill.getNextCD(t.getMode()));
  }

  function setRequiredLevel() {
    html(required_level, skill.getRequiredLevel(), skill.getNextRequiredLevel());
  }

  function format(str) {
    if (str == -1) {
      return "";
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