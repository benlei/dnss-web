function Skill(id, s, e) {
  var t = this;
  var level = 0;
  var pos = -1;
  var advancement = -1;
  var def = s.levels[0].required_level == 1 ? 1 : 0;
  var init = false;

  this.setLevel = function(lvl) {
    level = lvl;
  };

  this.getLevel = function() {
    return def && !level ? 1 : level;
  };


  this.getTotalSPUsage = function() {
    if (s.levels[0].totalspcost === undefined) {
      s.levels[0].totalspcost = s.levels[0].spcost;
      for (var i = 1; i < s.levels.length; i++) {
        s.levels[i].totalspcost = s.levels[i - 1].totalspcost + s.levels[i].spcost;
      }
    }

    return level ? s.levels[level-1].totalspcost : 0;
  };


  this.getPosition = function() {
    if (pos == -1) {
      pos = $(".container").index(e.parent());
    }
    return pos;
  };

  function getMaxLevel() {
    for (var i = s.levels.length - 1; -1 < i; i--) {
      if (s.levels[i].required_level <= dnss.getMaxRequiredLevel(t.getAdvancement())) {
        return i+1;
      }
    }
  }

  function getNextLevel() {
    return level < s.levels.length ? level + 1 : -1;
  }

  function getMinLevel () {
    return def;
  }

  function getPrevLevel() {
    return getMinLevel() < t.getLevel() ? level - 1 : -1;
  }

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

    e.find(".lvl").html(this.getLevel() + "/" + getMaxLevel());

    if (init) {
      // update requirements
      // update descriptions
    }

    init = true;
  };

  function getSpriteURL() {
    return "/skillicons/" + version.skillicon + "_skillicon" + s.image + (t.getLevel() ? "" : "_b") + ".png"
  }

  function getSpriteXY() {
    var x = (s.icon % 10) * -50;
    var y = Math.floor(s.icon / 10) * -50;
    return x+"px "+y+"px";
  }

  /* List of getters for current + next description */

  /* List of getters for navigating levels */

  // Set the background position
  e.css("background-position", getSpriteXY());

  // bind click events
  e.bind({
    mousedown: function(b) {
      var extreme = b.shiftKey || b.ctrlKey;
      var curr = t.getLevel();
      if (b.button == 0) { // left click
        if (extreme && curr < getMaxLevel()) {
          t.setLevel(getMaxLevel());
        } else if (getNextLevel() != -1) {
          t.setLevel(getNextLevel());
        }
      } else if(b.button == 2) { // right click
        if (extreme && curr != getMinLevel()) {
            t.setLevel(getMinLevel());
        } else if (getPrevLevel() != -1) {
            t.setLevel(getPrevLevel());
        }
      }

      curr != t.getLevel() && t.commit();
    },

    mouseenter: function() {
      //description.use($(this).data('skill'));
    }
  });

}