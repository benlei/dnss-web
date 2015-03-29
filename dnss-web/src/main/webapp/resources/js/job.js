function Job(adv, job) {
  this.advancement = adv;
  this.icon = job.jobicon;
  this.skills = {};
  this.default_skills = []

  for (var i in job.skills) {
    this.skills[i] = new Skill(job.skills[i], job.default_skills && job.default_skills.contains(i), job.messages);
    if (job.default_skills) {
      this.default_skills.push(this.skills[i]);
    }
  }


  this.is_primary = this.advancement == 0;
  this.is_secondary = this.advancement == 1;
  this.is_tertiary = this.advancement == 2;
}