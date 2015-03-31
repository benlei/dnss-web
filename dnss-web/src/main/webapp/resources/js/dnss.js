var dnss = {
  skills: {},
  types: null,
  queue: [],
  messages: {},
  max_levels: null,
  max_sp: null,
  build: Array(72 + 1 + 1).join('-').split(''),
  positions: [],
  current: {},
  total_skills: $('.skill[data-id]').length,
  parsed_total: 0,
  jobs: [],

  init: function(jobIdentifiers, max_levels, max_sp, start_build) {
    this.jobs = jobIdentifiers;
    this.max_levels = max_levels;
    this.max_sp = max_sp;

    // sets the starting build
    if (start_build) {
      var m = start_build.match(/^[0-9a-zA-Z-]+/g);
      var build = m.shift();
      if (build && build.length > 72) {
        build = build.split('');
        for (var i = 0; i < build.length - 1; i++) {
          this.build[i] = build[i];
        }
        this.build[this.build.length - 1] = build[build.length - 1];
      }
    }

    // gives position to every container
    $('.container').each(function(i) {
      var element = $(this).find('.skill');
      if (element.length) {
        element.data('position', i);
      }
    });

    // types
    $.getJSON('/json/types.min.json', function(json) {
      dnss.types = json;
      while (dnss.queue.length) {
        dnss.add_skills(dnss.queue.shift());
      }
    });

    // get json
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

    // mode bind
    $('#mode').bind('click', function() {
      switch($(this).val()) {
        case 'pve': $(this).val('pvp'); break;
        case 'pvp': $(this).val('pve'); break;
      }
     description.flip();
    });

    // job list bind
    $('#job-list-sp li[data-job]').each(function() {
      var idx = $(this).data('job');
      $(this).click(function() {
        $('#job-list-sp li[data-job][class="active"]').removeClass('active');
        $(this).addClass('active');
        $('.skill-tree').hide();
        $('#skill-tree-' + idx).show();
      })
    });
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

      var curr = this.skills[i], max = curr, end, sp_sum = 0;
      max = curr;

      for (; l < levels.length; l++) {
        curr.next = new Skill($.extend({}, skill, levels[l], {level: l+1, advancement: adv, id: i}));
        curr.next.prev = curr;
        curr = curr.next;

        sp_sum += curr.get_spcost();
        curr.total_sp_cost = sp_sum;


        if (curr.required_level <= this.max_levels[adv] ) {
          max = curr;
        }
      }
      end = curr;

      curr = this.skills[i];
      while (curr) {
        curr.start = this.skills[i];
        curr.max = max;
        curr.end = end;
        curr = curr.next;
      }

      this.parsed_total++;
      this.set_starting_skill(i);
    }
  },

  get_skill: function(id, lvl) {
    var skill = this.skills[id];
    if (skill.level == 1 && lvl == 0) {
      return skill;
    }

    while (skill.level != lvl) {
      skill = skill.next;
    }

    return skill;
  },

  $: function(id) {
    return $('.skill[data-id=' + id + ']');
  },

  set_starting_skill: function(id) {
    var $skill = this.$(id), position = $skill.data('position');
    var skill = this.get_skill(id, inv_build_map[this.build[position]]);
    skill.bind($skill);

    $skill.bind({
      mousedown: function(e) {
        var skill = $(this).data('skill'), change;
        var extreme = e.shiftKey || e.ctrlKey;
        if (e.button == 0) { // left click
          if (extreme) {
            if (skill.level < skill.max.level) {
              change = skill.max;
            }
          } else {
            if (skill.next) {
              change = skill.next;
            }
          }
        } else if(e.button == 2) { // right click
          if (extreme) {
            if (skill.level != skill.start.level) {
              change = skill.start;
            }
          } else {
            if (skill.prev) {
              change = skill.prev;
            }
          }
        }

        if (change) {
            description.use(change);
            change.bind($(this));
            dnss.update();
            requirements.check();
        }
      },
      mouseenter: function() {
        description.use($(this).data('skill'));
      }
    });

    $skill.on('contextmenu', function(){ // disable menu
      return false;
    });

    if (this.parsed_total == this.total_skills) {
      this.update();
      if (inv_build_map[this.build[this.build.length - 1]] & 1) {
        description.flip();
      }
    }
  },

  get_all_sp: function() {
    var sp = [0,0,0];
    for (var i = 0; i < 72; i++) {
      if (this.positions[i]) {
        sp[Math.floor(i/24)] += this.positions[i].total_sp_cost;
      }
    }

    return sp;
  },

  update: function() {
    var sp = this.get_all_sp();
    for (i in sp) {
      $('#job-list-sp li[data-job=' + i + '] .sp').html(sp[i] + '/' + this.max_sp[i]);
    }

    $('#job-list-sp li:last .sp').html(sp[0]+sp[1]+sp[2] + '/' + this.max_sp[3]);
    this.update_build();
  },

  update_build: function() {
    var $build = $('#build');
    $build.val(fmt($build.data('base'), window.location) + '/?' + this.build.join(''));
  }
};