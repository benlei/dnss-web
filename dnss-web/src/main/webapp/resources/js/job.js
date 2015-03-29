function Job(adv, json) {
  this.advancement = adv;
  this.icon = json.jobicon;
  this.skills = new Skills(json.skills, json.default_skills, json.message);
}