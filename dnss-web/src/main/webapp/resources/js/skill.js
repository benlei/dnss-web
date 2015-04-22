function Skill(id, s, e) {
  var el = e.find(".lvl");
  var t = this;
  var level = 0;
  var pos = -1;
  var advancement = -1;
  var def = s.levels[0].required_level == 1 ? 1 : 0;
  var started = false;
  var nSP = {}; // notifiable sp
  var nSkills = {}; // notifiable skills

  this.ultimate = s.levels.length == 2 && s.levels[0].required_level == 40 && s.levels[1].required_level == 60;

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

    el.html(this.getLevel() + "/" + this.getMaxLevel());

    build.put(this.getPosition(), this.getLevel() - def);
    if (started) {
      dnss.commit(t.getAdvancement());
      description.update();
    }

    started = true;

    notifier.notify(notifier.SKILL, id, this.getLevel());
    this.checkAndSetBalance(); // check if this skill is now balanced
    this.checkAndSetUltimates(); // check if ults are both set
  };

  this.notifySP = function(a, b) {
    nSP[a] = b;
    this.checkAndSetBalance();
  };

  this.notifySkill = function(id, b) {
    nSkills[id] = b;
    this.checkAndSetBalance();
  };

  this.setBalanced = function(b) {
    b ? el.removeClass("r") : el.addClass("r");
  };

  this.checkAndSetUltimates = function() {
    var ults = dnss.getUltimateSkills();

    if (ults[0] && ults[1]) {
      if (ults[0].getLevel() > 0 && ults[1].getLevel() > 0) {
        ults[0].setBalanced(true);
        ults[1].setBalanced(true);
      } else if (ults[0].getLevel() > 0 && ! ults[1].getLevel()) {
        ults[0].checkAndSetBalance();
      } else if (ults[1].getLevel() > 0 && ! ults[0].getLevel()) {
        ults[1].checkAndSetBalance();
      }
    }
  };

  this.checkAndSetBalance = function() {
    this.setBalanced(this.isBalanced() || !this.getLevel());
  };

  this.isBalancedSP = function() {
    for (var i in nSP) {
      if (! nSP[i]) {
        return false;
      }
    }
    return true;
  };

  this.isBalancedRequirements = function() {
    for (var i in nSkills) {
      if (! nSkills[i]) {
        return false;
      }
    }
    return true;
  }

  this.isBalanced = function() {
    return this.isBalancedSP() && this.isBalancedRequirements();
  };

  this.getSPRequirements = function() {
    var list = [];

    for (var i = 0; i < s.need_sp.length; i++) {
      if (s.need_sp[i] > 0) {
        var str = properties.jobs[i].name + description.getSeparator() + s.need_sp[i] + " SP";
        if (nSP[i]) {
          list.push(str);
        } else {
          list.push("#r" + str + "#w");
        }
      }
    }
    return list.length ? list.join(", ") : "None";
  };

  this.getSkillRequirements = function() {
    var list = [];

    for (var i = 0; i < s.requires.length; i++) {
      var other = dnss.getSkill(s.requires[i].id);
      var str = properties.jobs[other.getAdvancement()].name + description.getSeparator() + other.getName() + " Lv. " + s.requires[i].level;
      if (nSkills[s.requires[i].id]) {
        list.push(str);
      } else {
        list.push("#r" + str + "#w");
      }
    }

    return list.length ? list.join(", ") : "None";
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
      this.notifySP(i, dnss.getSP(i) >= s.need_sp[i]);
      notifier.addNotifier(notifier.SP, id, i, s.need_sp[i]);
    }
  }

  for (var i = 0; i < s.requires.length; i++) {
    var other = dnss.getSkill(s.requires[i].id);
    nSkills[s.requires[i].id] = false;
    if (other) {
      this.notifySkill(s.requires[i].id, other.getLevel() >= s.requires[i].level);
    }
    notifier.addNotifier(notifier.SKILL, id, s.requires[i].id, s.requires[i].level);
  }
}