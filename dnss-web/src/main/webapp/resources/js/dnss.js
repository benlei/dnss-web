function DNSS(o) {
  /* Private */
  var jobs = o.jobs;
  var json = o.json;
  var skillicon = o.skillicon;
  var max = o.max;
  var build = Array(72 + 1 + 1).join('-').split('');
  var readyCount = jobs.length+1;
  var skills = {};
  var common = {};

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
      $.getJSON('/json/' + json.version + '-' + jobs[i].id + '-skills.json', addSkills).always(ready);
    }

    $.getJSON('/json/' + json.version + '-common.json', function(json) { common = json; }).always(ready);

    for (var i = 0; i < jobs.length; i++) {
      $.getJSON('/json/' + json.version + '-' + jobs[i].id + '-messages.json', function(json) {
        $.each(json, function(j, message) { messages.put(j, message); });
      });
    }
  };

  this.getSkillType = function(id) { return common.types.skills[id]; };
  this.getWeaponType = function(id) { return common.types.weapons[id]; };

  function addSkills(json) {
    $.each(json, function(id, data) {
      var $skill = $('.skill[data-id=' + id + ']');
      var skill = new Skill(data);
      var pos = $skill.data('pos');
      skills[id] = skill;

      skill.bindTo($skill); // set all bind events too
      skill.setLevel(iBuildMap[pos]);
      skill.trigger();
    });
  };

  function ready() {
    if (--readyCount != 0) {
      return;
    }

    var defaults = common.default_skills;
    for (var i = 0; i < defaults.length; i++) {
      if (skills[defaults[i]]) {
        skills[defaults[i]].setDefault(true);
        skills[defaults[i]].trigger(); // bump it up a level
      }
    }
  };
}