function Skill(id, s, e) {
  var t = this;
  var level = 0;
  var pos = -1;
  var advancement = -1;
  var def = s.levels[0].required_level == 1 ? 1 : 0;
  var started = false;
  var hooks = [];

  this.setLevel = function(lvl) {
    level = lvl;
  };

  this.getLevel = function() {
    return def && !level ? 1 : level;
  };

  this.getTotalSPUsage = function() {
    if (s.levels[0].totalspcost === undefined) {
      s.levels[0].totalspcost = s.levels[0].spcost;
      var total = s.levels[0].spcost;
      for (var i = 1; i < s.levels.length; i++) {
        if (i < this.getMaxLevel()) {
          total = total + s.levels[i].spcost;
        }

        s.levels[i].totalspcost = total;
      }
    }

    return this.getLevel() ? s.levels[this.getLevel()-1].totalspcost : 0;
  };


  this.getPosition = function() {
    if (pos == -1) {
      pos = $(".container").index(e.parent());
    }
    return pos;
  };

  this.getMaxLevel = function() {
    if (s.max_level === undefined) {
      for (var i = s.levels.length - 1; -1 < i; i--) {
        if (s.levels[i].required_level <= properties.max.required_level[this.getAdvancement()]) {
          s.max_level = i+1;
          break;
        }
      }
    }

    return s.max_level;
  };

  this.getNextLevel = function() {
    return this.getLevel() < s.levels.length ? this.getLevel() + 1 : -1;
  };

  this.getMinLevel = function() {
    return def;
  };

  this.getPrevLevel = function() {
    return this.getMinLevel() < this.getLevel() ? level - 1 : -1;
  };

  this.getAdvancement = function() {
    if (advancement == -1) {
      advancement = parseInt(e.parentsUntil("table").parent().attr("id").substr(-1));
    }
    return advancement;
  };

  this.commit = function() {
    if (e.css("background-image").indexOf(getSpriteURL()) == -1) {
      e.css("background-image", "url("+getSpriteURL()+")");
    }

    e.find(".lvl").html(this.getLevel() + "/" + this.getMaxLevel());

    build.put(this.getPosition(), this.getLevel() == 1 && def ? 0 : this.getLevel());
    if (started) {
      dnss.commit(t.getAdvancement());
      description.update();
    }


    for (var i = 0; i < hooks.length; i++) {
      hooks[i].notifySkill(t);
    }

    started = true;
  };

  // this skill is being notified
  this.notifySP = function(adv, sp) {
  };

  this.isSPRequired = function() {

  };

  this.getRequiredSPs = function() {
  };

  // this skill is being notified from input skill
  this.notifySkill = function(skill) {

  };

  this.isSkillRequired = function() {
  };

  this.getRequiredSkills = function() {
  };

  this.addHook = function(skill) {
    hooks.push(skill);
  };

  function getSpriteURL() {
    return "/skillicons/" + properties.version.skillicon + "_skillicon" + s.image + (t.getLevel() ? "" : "_b") + ".png"
  }

  function getSpriteXY() {
    var x = (s.icon % 10) * -50;
    var y = Math.floor(s.icon / 10) * -50;
    return x+"px "+y+"px";
  }


  // things for skill descriptions
  this.getName = function() {
    return messages.get(s.nameid);
  };

  this.getNextSPUsage = function() {
    return this.getLevel() < this.getMaxLevel() ? s.levels[this.getLevel()].spcost : 0;
  };

  this.getMPUsage = function(mode) {
    return this.getLevel() ? s.levels[this.getLevel()-1].mpcost[mode]+"%" : -1;
  };

  this.getNextMPUsage = function(mode) {
    return this.getLevel() < s.levels.length ? s.levels[this.getLevel()].mpcost[mode]+"%" : -1;
  };

  this.getCD = function(mode) {
    return this.getLevel() ? s.levels[this.getLevel()-1].cd[mode] : -1;
  };

  this.getNextCD = function(mode) {
    return this.getLevel() < s.levels.length ? s.levels[this.getLevel()].cd[mode] : -1;
  };

  this.getRequiredLevel = function() {
    return this.getLevel() ? s.levels[this.getLevel()-1].required_level : -1;
  };

  this.getNextRequiredLevel =  function() {
    return this.getLevel() < s.levels.length ? s.levels[this.getLevel()].required_level : -1;
  };

  this.getRequiredWeapons = function() {
    if (!s.needweapon.length) {
      return "Any";
    }

    weapons = [];
    for (var i = 0; i < s.needweapon.length; i++) {
      weapons[i] = dnss.getWeaponType(s.needweapon[i]);
    }

    return weapons.join(", ");
  };

  this.getType = function() {
    return dnss.getSkillType(s.type);
  };

  this.getDescription = function(mode) {
    return this.getLevel() ? messages.get(s.levels[this.getLevel()-1].explanationid[mode],
      s.levels[this.getLevel()-1].explanationparams[mode]) : -1;
  };

  this.getNextDescription = function(mode) {
    return this.getLevel() < s.levels.length ? messages.get(s.levels[this.getLevel()].explanationid[mode],
      s.levels[this.getLevel()].explanationparams[mode]) : -1;
  };


  // Set the background position
  e.css("background-position", getSpriteXY());

  // bind click events
  e.bind({
    mousedown: function(b) {
      var extreme = b.shiftKey || b.ctrlKey;
      var curr = t.getLevel();
      if (b.button == 0) { // left click
        if (extreme && curr < t.getMaxLevel()) {
          t.setLevel(t.getMaxLevel());
        } else if (t.getNextLevel() != -1) {
          t.setLevel(t.getNextLevel());
        }
      } else if(b.button == 2) { // right click
        if (extreme && curr != t.getMinLevel()) {
            t.setLevel(t.getMinLevel());
        } else if (t.getPrevLevel() != -1) {
            t.setLevel(t.getPrevLevel());
        }
      }

      curr != t.getLevel() && t.commit();
    },

    mouseenter: function() {
      description.use(t);
    }
  });

  for (var i = 0; i < s.need_sp.length; i++) {
    if (s.need_sp[i] > 0) {
      dnss.addSPHook(i, id);
    }

    // reformatting the array
    s.need_sp[i] = {req:s.need_sp[i]};
  }

  for (var i = 0; i < s.requires.length; i++) {
    dnss.addSkillHook();
  }
}