var search = new (function Search() {
  function clear() {
    var skills = dnss.getSkills();
    for (id in skills) {
      skills[id].setOpacity(1);
    }
  }

  function nameAndDescription(pattern) {
    var re = new RegExp(pattern, "igm");
    var skills = dnss.getSkills();
    for (id in skills) {
      var skill = skills[id];
      var haystacks = [skill.getName(),
                       skill.getRequiredWeapons(),
                       skill.getType(),
                       skill.getDescription(),
                       skill.getNextDescription()].filter(function(v) { return v != -1 });

      var matched = false;
      for (var i = 0 ; i < haystacks.length && ! matched; i++) {
        matched = re.test(haystacks[i]) || re.test(haystacks[i]); // need to test twice because regexp can have false negatives
      }

      skill.setOpacity(matched ? 1 : .1);
    }
  }

  this.hook = function() {
    var str = $("#search").val();
    if (str.length > 1) {
      nameAndDescription(str);
    } else {
      clear();
    }
  };

  $("#search").val("").on("input", this.hook);
})();