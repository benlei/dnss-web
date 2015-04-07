var dnss = {
  jobs: [],
  skillicon: {},
  max: {},
  common: {},
  skills = {},
  messages: {},
  build: Array(72 + 1 + 1).join('-').split(''), // the current build
  readyCount: 0,

  init: function(options) {
    this.jobs = options.jobs;
    this.skillicon = options.skillicon;
    this.max = options.max;

   // sets the starting build
    if (options.build.length > 24) {
      var build = options.build.match(/^[0-9a-zA-Z-]+/g);
      if (build) {
        build = build.shift().split('');
        for (var i = 0; i < build.length - 1; i++) {
          this.build[i] = build[i];
        }
        this.build[this.build.length - 1] = build[build.length - 1];
      }
    }

    this.readyCount = options.jobs.length+1;
    for (var i = 0; i < options.jobs.length; i++) {
      $.getJSON('/json/' + options.json.version + '-' + options.jobs[i].id + '-skills.json', this.addSkill).always(this.ready);
    }

    $.getJSON('/json/' + options.json.version + '-common.json', this.setCommon).always(this.ready);

    for (var i = 0; i < options.jobs.length; i++) {
      $.getJSON('/json/' + options.json.version + '-' + options.jobs[i].id + '-messages.json', function(json) {
        $.each(json, function(j, message) {
          messages[j] = message;
        });
      });
    }
  },

  addSkill: function(json) {
    $.each(json, function(id, skill) {
      this.skills[id] = new Skill(skill);
      var $skill = $('.skill[data-id=' + id + ']');
      var pos = $skill.data('pos');
      this.skills[id].bindTo($skill); // set all bind events too
      this.skills[id].setLevel(invertedBuildMap[pos]);
      this.skills[id].trigger();
    });
  },

  setCommon: function(json) {
    this.common = json;
  },

  ready: function() {
    if (--this.readyCount != 0) {
      return;
    }

    var default_skills = this.common.default_skills;
    for (var i = 0; i < default_skills.length; i++) {
      if (this.skills[default_skills[i]]) {
        this.skills[default_skills[i]].setDefault(true);
        this.skills[default_skills[i]].trigger();
      }
    }
  }
};