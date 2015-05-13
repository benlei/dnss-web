var search = new (function Search() {
  function clear() {
    var skills = dnss.getSkills();
    for (id in skills) {
      skills[id].setOpacity(1);
    }
  }

  function nameAndDescription(pattern) {
    var re = new RegExp(pattern, "igm"), skills = dnss.getSkills();
    for (id in skills) {
      var skill = skills[id];
      var haystacks = [skill.getName(), skill.getRequiredWeapons(), skill.getType(),
        skill.getDescription(), skill.getNextDescription()];

      var matched = false;
      for (var i = 0 ; i < haystacks.length; i++) {
        if (haystacks[i] != -1 && re.test(haystacks[i])) {
          matched = true;
          break;
        }
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

  $("#search").on("input", this.hook);
})();