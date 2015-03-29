var dnss = {
  skills: {},
  types: null,
  queue: [],
  messages: {},
  max_level: 0,
  max_skill_levels: null,
  sp: [0, 0, 0],
  max_sp: null,

  mode: 'pve',

  init: function(jobIdentifiers, max_level, max_skill_levels, max_sp) {
    this.max_level = max_level;
    this.max_skill_levels = max_skill_levels;
    this.max_sp = max_sp;

    this.assign_icon_positions();

    $.getJSON('/json/types.min.json', function(json) {
      dnss.types = json;
      while (dnss.queue.length) {
        var q = dnss.queue.shift();
        dnss.add_skills(q.adv, q.job);
      }
    });

    for (var i in jobIdentifiers) {
      $.getJSON(fmt('/json/$0.min.json', jobIdentifiers[i]), function(json) {
        for (var i in json.messages) { // I don't trust async $.extends()
          dnss.messages[i] = json.messages[i];
        }

        if (dnss.types) {
          dnss.add_skills(i, json);
        } else {
          dnss.queue.push({adv: i, job: json});
        }
      });
    }
  },

  assign_icon_positions: function() {
    $('.container').each(function(i) {
      var element = $(this).find('.skill');
      if (element.length) {
        element.data('position', i);
      }
    });
  },

  flip: function() {
    switch (this.mode) {
      default:
      case 'pve': this.mode = 'pvp'; break;
      case 'pvp': this.mode = 'pve'; break;
    }
  },

  add_skills: function(adv, job) {
    var skills = job.skills, messages = job.messages, default_skills = job.default_skills;
    for (i in skills) {
      var skill = skills[i];
      var levels = skill.levels;
      delete skill.levels; // don't want this lingering

      var l = 0;
      if (default_skills) {
        this.skills[i] = new Skill($.extend({}, skill, levels[0], {level: 1, advancement: adv}));
        l++;
      } else {
        this.skills[i] = new Skill($.extend({}, skill, {image: skill.image+'_b', level: 0, advancement: adv}));
      }

      var curr_skill = this.skills[i];
      for (; l < levels.length; l++) {
        curr_skill.next_level = new Skill($.extend({}, skill, levels[l], {level: l+1, advancement: adv}));
        curr_skill.next_level.prev_level = curr_skill;
        curr_skill = curr_skill.next_level;
      }
    }
  },

  get_skill: function(id, lvl) {
    var skill = this.skills[id];
    while (skill.get_level() != lvl) {
      skill = skill.next_level;
    }

    return skill;
  }
};