function Skill(id, s, e) {
  var t = this;
  var level = 0;

  this.setLevel = function(lvl) {
    level = lvl;
  };

  this.getLevel = function() {
    return level+isDefault()
  };

  this.getTotalSPUsage = function() {
    if (s.levels[0].totalspcost === undefined) {
      s.levels[0].totalspcost = s.levels[0].spcost;
      for (var i = 1; i < s.levels.length; i++) {
        s.levels[i].totalspcost = s.levels[i - 1].totalspcost;
      }
    }

    if (this.getLevel()) {
      return s.levels[this.getLevel() - 1].totalspcost;
    } else {
      return 0;
    }
  };


  this.getPosition = function() {
    return e.data("pos");
  };

  this.getMaxLevel = function() {
    for (var i = s.levels.length - 1; -1 < i; i--) {
      if (s.levels[i].required_level <= dnss.getMaxRequiredLevel(this.getAdvancement())) {
        return i+1;
      }
    }
  };

  this.getMinLevel = function() {
    return isDefault();
  };

  this.getAdvancement = function() {
    return e.data("adv")
  };

  this.set = function() {
    if (e.css("background-image").indexOf(getSpriteURL()) == -1) {
      e.css("background-image", "url("+getSpriteURL()+")");
    }

    e.find(".lvl").html(this.getLevel() + "/" + this.getMaxLevel());
  };

  function getSpriteURL() {
    return "/skillicons/" + version.skillicon + "_skillicon" + s.image + (t.getLevel() ? "" : "_b") + ".png"
  }

  function getSpriteXY() {
    var x = (s.icon % 10) * -50;
    var y = Math.floor(s.icon / 10) * -50;
    return x+"px "+y+"px";
  }

  function isDefault() {
    return s.levels[0].required_level == 1;
  }

  /* List of getters for current + next description */

  /* List of getters for navigating levels */

  // things to do at end
  e.css("background-position", getSpriteXY());6
}