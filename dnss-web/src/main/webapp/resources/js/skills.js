function Skills(skills, default_skills, messages) {
  this.list = {};
  this.defaults = []

  // setup skill list
  for (var id in skills) {
    this.list[id] = new Skill(skills[id], default_skills && default_skills.indexOf(id) != 1, messages);
  }

  if (default_skills) {
    for (var i = 0; i < default_skills.length; i++) {
      defaults.push(this.list[default_skills[i]]);
    }
  }
}