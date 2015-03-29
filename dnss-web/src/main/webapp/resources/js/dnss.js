var dnss = {
  skills: {},
  types: null,
  queue: [],
  messages: {},
  max_levels: null,
  max_sp: null,
  sp: [0, 0, 0],
  mode: 'pve',
  build: Array(72 + 1 + 1).join('-').split(''),

  init: function(jobIdentifiers, max_levels, max_sp, start_build) {
    this.max_levels = max_levels;
    this.max_sp = max_sp;

    if (start_build) {
      var m = this.start_build.match(/^[0-9a-zA-Z-]+/g);
      var build = m.shift();
      if (build && build.length > 72) {
        build = build.split('');
        for (var i = 0; i < build.length - 1; i++) {
          this.build[i] = build[i];
        }
        this.build[this.build.length - 1] = build[build.length - 1];

        if (inv_build_map[build[build.length - 1]] & 1) {
          this.flip();
        }
      }
    }

    this.assign_icon_positions();

    $.getJSON('/json/types.min.json', function(json) {
      dnss.types = json;
      while (dnss.queue.length) {
        dnss.add_skills(dnss.queue.shift());
      }
    });

    for (var i in jobIdentifiers) {
      $.getJSON(fmt('/json/$0.min.json', jobIdentifiers[i]), function(json) {
        for (var j in json.messages) { // I don't trust async $.extends()
          dnss.messages[j] = json.messages[j];
        }

        if (dnss.types) {
          dnss.add_skills(json);
        } else {
          dnss.queue.push(json);
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
      case 'pve': this.mode = 'pvp'; this.build[this.build.length - 1] = build_map[1]; break;
      case 'pvp': this.mode = 'pve'; this.build[this.build.length - 1] = build_map[0]; break;
    }
  },

  add_skills: function(job) {
    var skills = job.skills, default_skills = job.default_skills, adv = job.advancement;
    delete job.advancement;
    for (i in skills) {
      var skill = skills[i];
      var levels = skill.levels;
      delete skill.levels; // don't want this lingering

      var l = 0, is_default = false;
      if (default_skills) {
        for (var j = 0; j < default_skills.length; j++) {
          if (default_skills[j] == i) {
            is_default = true;
            break;
          }
        }
      }

      if (is_default) {
        this.skills[i] = new Skill($.extend({}, skill, levels[0], {level: 1, advancement: adv, id: i}));
        l++;
      } else {
        this.skills[i] = new Skill($.extend({}, skill, {image: skill.image+'_b', required_level: 0, level: 0, advancement: adv, id: i}));
      }

      var curr_skill = this.skills[i];
      for (; l < levels.length; l++) {
        curr_skill.next_level = new Skill($.extend({}, skill, levels[l], {level: l+1, advancement: adv}));
        curr_skill.next_level.prev_level = curr_skill;
        curr_skill = curr_skill.next_level;
      }
      
      this.set_starting_skill(i);
    }
  },

  get_skill: function(id, lvl) {
    var skill = this.skills[id];
    if (skill.level == 1 && lvl == 0) {
      return skill;
    }

    while (skill.level != lvl) {
      skill = skill.next_level;
    }

    return skill;
  },

  $: function(id) {
    return $(fmt('.skill[data-id=$0]', id));
  },

  set_starting_skill: function(id) {
    var position = this.$(id).data('position');
    var skill = this.get_skill(id, inv_build_map[this.build[position]]);
    skill.use();
  }
};