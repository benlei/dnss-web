function DNSS(o) {
  /* Private Fields */
  var jobs = o.jobs;
  var build = Array(72 + 1 + 1).join('-').split('');
  var counter = jobs.length+1;
  var skills = {};
  var common = {};

  /* Public Fields */
  this.max = o.max;

  /* Constructor */
  (function() {
    if (o.build.length > 24) {
      var b = o.build.match(/^[0-9a-zA-Z-]+/g);
      if (b) {
        b = b.shift().split('');
        build[build.length - 1] = b[b.length - 1];
        for (var i = 0; i < b.length - 1; i++) {
          build[i] = b[i];
        }
      }
    }
  })();

  /* starts the JSON stuff */
  this.start = function() {
    for (var i = 0; i < jobs.length; i++) {
      $.getJSON('/json/' + version.json + '-' + jobs[i].id + '-skills.json', addSkills).always(postHook);
    }

    $.getJSON('/json/' + version.json + '-common.json', function(json) { common = json; }).always(postHook);
  };

  this.getSkillType = function(id) { return common.types.skills[id]; };
  this.getWeaponType = function(id) { return common.types.weapons[id]; };

  function addSkills(json) {
    $.each(json, function(id, data) {
      var skill = new Skill(id, data, $('.skill[data-id=' + id + ']'));
      skills[id] = skill;
      skill.setLevel(iBuildMap[skill.getPosition()]);
      skill.trigger();
    });
  }

  function postHook() {
    if (--counter != 0) {
      return;
    }

    var defaults = common.default_skills;
    for (var i = 0; i < defaults.length; i++) {
      if (skills[defaults[i]]) {
        skills[defaults[i]].setDefault(true);
        skills[defaults[i]].trigger(); // bump it up a level
      }
    }

    for (var i = 0; i < jobs.length; i++) {
      $.getJSON('/json/' + version.json + '-' + jobs[i].id + '-messages.json', function(json) {
        $.each(json, function(j, message) { messages.put(j, message); });
      });
    }
  }
}