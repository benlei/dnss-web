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
  var balanced = -1;
  var opacity = 1;

  this.ultimate = s.levels.length == 2 && s.levels[0].required_level == 40 && s.levels[1].required_level == 60;

  this.setLevel = function(lvl) {
    level = lvl;
  };

  this.getLevel = function() {
    return def && !level ? 1 : level;
  };

  this.reset = function() {
    s.levels[0].totalspcost = undefined;
    s.max_level = undefined;
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

    try {
        return this.getLevel() ? s.levels[this.getLevel()-1].totalspcost : 0;
    } catch(e) {
        dnss.t5Alert();
    }
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
        if (s.levels[i].required_level <= properties.cap) {
          s.max_level = i+1;
          break;
        }
      }
      if (s.max_level === undefined) { // it's still undefined
        s.max_level = 0;
      }
    }

    return Math.min(s.max_level, s.levels.length - s.spmaxlevel);
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
    var bg = e.css("background-image"), isB = bg.indexOf("_b.png");
    if (this.getLevel() == 0 && bg.indexOf("_b.png") == -1) {
      e.css("background-image", bg.replace(".png", "_b.png"));
    } else if (this.getLevel() > 0 && bg.indexOf("_b.png") != -1) {
      e.css("background-image", bg.replace("_b.png", ".png"));
    }

    build.put(this.getPosition(), this.getLevel() - def);
    el.html(this.getLevel() + "/" + this.getMaxLevel());
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
    // optimization: only check and set balance if the val will cahnge
    if (nSP[a] == b) {
      return;
    }

    nSP[a] = b;
    balanced = -1; // reset it
    this.checkAndSetBalance();
  };

  this.notifySkill = function(id, b) {
    // optimization: only check and set balance if the val will cahnge
    if (nSkills[id] == b) {
      return;
    }

    nSkills[id] = b;
    balanced = -1; // reset it
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
    if (balanced == -1) {
      for (var i in nSP) {
        if (! nSP[i]) {
          balanced = false;
          return balanced;
        }
      }
      balanced = true;
    }
    return balanced;
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

    for (var i in s.need_sp) {
      var str = properties.jobs[i].name + " SP Total " + s.need_sp[i] + " or above";
      if (this.getLevel()) {
        if (! nSP[i]) {
          list.push("#r" + str + "#w");
        }
      } else {
        if (nSP[i]) {
          list.push(str);
        } else {
          list.push("#r" + str + "#w");
        }
      }
    }
    return list.length ? list.join("\\n") : -1;
  };

  this.getSkillRequirements = function() {
    var list = [];

    for (var i in s.requires) {
      var other = dnss.getSkill(i);
      var str = other.getName() + " Lv. " + s.requires[i];
//      if (other.getAdvancement() != this.getAdvancement()) {
//        str = properties.jobs[other.getAdvancement()].name + " " + str;
//      }

      if (this.getLevel()) {
        if (!nSkills[i]) {
          list.push("#r" + str + "#w");
        }
      } else {
        if (nSkills[i]) {
          list.push(str);
        } else {
          list.push("#r" + str + "#w");
        }
      }
    }

    return list.length ? list.join("\\n") : -1;
  };

  // things for skill descriptions
  this.getName = function() {
    return messages.get(s.nameid);
  };

  this.getNextSPUsage = function() {
    return this.getLevel() < this.getMaxLevel() ? s.levels[this.getLevel()].spcost : 0;
  };

  this.getMPUsage = function() {
    return this.getLevel() ? s.levels[this.getLevel()-1].mpcost[description.getMode()]: -1;
  };

  this.getNextMPUsage = function() {
    return this.getLevel() < s.levels.length ? s.levels[this.getLevel()].mpcost[description.getMode()] : -1;
  };

  this.getCD = function() {
    return this.getLevel() ? s.levels[this.getLevel()-1].cd[description.getMode()] : -1;
  };

  this.getNextCD = function() {
    return this.getLevel() < s.levels.length ? s.levels[this.getLevel()].cd[description.getMode()] : -1;
  };

//  this.getRequiredLevel = function() {
//    return this.getLevel() ? s.levels[this.getLevel()-1].required_level : -1;
//  };

  this.getNextRequiredLevel =  function() {
    return this.getLevel() < s.levels.length ? s.levels[this.getLevel()].required_level : -1;
  };

  this.getRequiredWeapons = function() {
    if (! s.needweapon.length) {
      return -1;
    }

    weapons = [];
    for (var i = 0; i < s.needweapon.length; i++) {
      weapons[i] = properties.weapontypes[s.needweapon[i]];
    }

    return weapons.join(", ");
  };

  this.getType = function() {
    return properties.skilltypes[s.type];
  };

  this.getElement = function() {
    return s.element != -1 ? properties.skillelements[s.element] : -1;
  };

  this.getDescription = function() {
    return this.getLevel() ? messages.get(s.levels[this.getLevel()-1].explanationid[description.getMode()],
      s.levels[this.getLevel()-1].explanationparams[description.getMode()]) : -1;
  };

  this.getNextDescription = function() {
    return this.getLevel() < s.levels.length ? messages.get(s.levels[this.getLevel()].explanationid[description.getMode()],
      s.levels[this.getLevel()].explanationparams[description.getMode()]) : -1;
  };

  this.setOpacity = function(o) {
    if (o < 0 || o > 1 || o == opacity) {
      return;
    }

    opacity = o;
    e.css("opacity", o);
  };

  // bind click events
  e.bind({
    mousedown: function(b) {
      var extreme = b.shiftKey || b.ctrlKey;
      var curr = t.getLevel();
      switch(b.button) {
        case 0: // left click
          if (extreme && curr < t.getMaxLevel()) {
            t.setLevel(t.getMaxLevel());
          } else if (t.getNextLevel() != -1) {
            t.setLevel(t.getNextLevel());
          }
          break;
        case 2: // right click
          if (extreme && curr != t.getMinLevel()) {
              t.setLevel(t.getMinLevel());
          } else if (t.getPrevLevel() != -1) {
              t.setLevel(t.getPrevLevel());
          }
          break;
        case 1: // middle click
          download.skill(id, t.getName());
          return;
        default: return;
      }

      curr != t.getLevel() && t.commit();
    },

    mouseenter: function() {
      description.use(t);
      dnss.isFullScreen() && dnss.setActive(t.getAdvancement());
    }
  });

  for (var i in s.need_sp) {
    this.notifySP(i, dnss.getSP(i) >= s.need_sp[i]);
    notifier.addNotifier(notifier.SP, id, i, s.need_sp[i]);
  }

  for (var i in s.requires) {
    nSkills[i] = false;
    dnss.getSkill(i) && this.notifySkill(i, dnss.getSkill(i).getLevel() >= s.requires[i]);
    notifier.addNotifier(notifier.SKILL, id, i, s.requires[i]);
  }
}