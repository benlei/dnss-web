var description = new (function Description() {
  var t = this;
  var skill = null;
  var
    // name
    name = $("#skill-name"),

    // main block
    level = $("#skill-level .w"),
    mp = $("#skill-mp .w"),
    required_weaps = {main:$("#skill-required-weapon"), desc:$("#skill-required-weapon .w")},
    type = $("#skill-type .w"),
    cd = $("#skill-cd .w"),
    total_sp = $("#skill-total-sp .w"),

    // level up reqs
    required_level = {main:$("#skill-required-level"), desc:$("#skill-required-level .w")},
    skills_required = {main:$("#skills-required"), desc:$("#skills-required")},
    sp_required = {main:$("#sp-required"), desc:$("#sp-required")},
    sp = {main:$("#skill-sp"), desc:$("#skill-sp .w")},

    // descriptions
    desc = $("#skill-description .d"),
    ndesc = {main:$("#next-description"), desc:$("#next-description .d")};

  this.updateTop = function() {
    if (!skill) {
      return;
    }

    $("#sidebar-2").css("top", $("#skill-tree-" + skill.getAdvancement()).offset().top - $(".skill-tree:visible").first().offset().top);
  };

  this.use = function(s) {
    if (skill == s) { // no need to update more than once
      return;
    }

    skill = s;
    this.updateTop();
    this.update();
  };

  this.update = function() {
    if (!skill) {
      return;
    }

    // name
    setName();

    // main block
    setLevel();
    setMP();
    setRequiredWeapons();
    setType();
    setCD();
    setTotalSP();

    // level up reqs
    setRequiredLevel();
    setRequiredSkills();
    setRequiredSP();
    setSP();

    // descriptions
    setDescriptions();

    // if level up reqs are all hidden, just hide the level up req stuff
    var sep = $("#sidebar-2 .separator").first();
    if (required_level.main.is(":hidden") && skills_required.main.is(":hidden") && sp_required.main.is(":hidden") && sp.main.is(":hidden")) {
      sep.hide();
      sep.next().hide();
    } else {
      sep.show();
      sep.next().show();
    }
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
    return build.get(build.FLAG_POS) & 1 ? "pvp" : "pve";
  };

  function currOrNext(e, curr, next) {
    var after = e.data("after") ? " " + e.data("after") : "";
    e.html((curr == -1 ? next : curr) + after);
  }

  function showOrHide(e, text) {
    if (text == -1) {
      e.main.hide();
      return;
    }

    e.desc.html(text);
    e.main.show();
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
    showOrHide(required_weaps, skill.getRequiredWeapons());
  }

  function setType() {
    type.html(skill.getType());
  }

  function setCD() {
    currOrNext(cd, skill.getLevel() ? skill.getCD(t.getMode()) : -1, skill.getNextCD(t.getMode()));
  }

  function setTotalSP() {
    total_sp.html(skill.getTotalSPUsage());
  }

  // requirement block
  function setRequiredLevel() {
    showOrHide(required_level, skill.getNextRequiredLevel())
  }

  function setRequiredSkills() {
    showOrHide(skills_required, format(skill.getSkillRequirements()));
  }

  function setRequiredSP() {
    showOrHide(sp_required, format(skill.getSPRequirements()));
  }

  function setSP() {
    showOrHide(sp, skill.getLevel() < skill.getMaxLevel() ? skill.getNextSPUsage() : -1);
  }

  // description block
  function setDescriptions() {
    var d = skill.getDescription(t.getMode()), n = skill.getNextDescription(t.getMode());
    desc.html(format(d == -1 ? n : d));

    showOrHide(ndesc, d == -1  || n == -1 ? -1 : format(n));
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
    var mode = build.get(build.FLAG_POS) & 1;
    var flag  = (build.get(build.FLAG_POS) >> 1) << 1;
    build.put(build.FLAG_POS, flag | (mode?0:1));
    build.commit();

    t.hook();
  });
})();