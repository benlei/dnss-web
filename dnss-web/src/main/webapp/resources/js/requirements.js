var requirements = {
  $: $('#warnings'),
  $$: $('#warnings li').first(),
  advancements: [$('#job-list-sp li[data-job=0]').ownText(),$('#job-list-sp li[data-job=1]').ownText(),$('#job-list-sp li[data-job=2]').ownText()],

  check: function() {
    var sp = dnss.get_all_sp(), show = false;
    for (id in dnss.current) {
      var skill = dnss.current[id];

      for (var i = 0; i < skill.requires.length; i++) {
        if (skill.level && dnss.current[skill.requires[i].id].level < skill.requires[i].level) {
          this.add_skill(skill.nameid, dnss.current[skill.requires[i].id].nameid, skill.requires[i].level);
          show = true;
        } else {
          this.remove_skill(skill.nameid, dnss.current[skill.requires[i].id].nameid, skill.requires[i].level);
        }
      }

      for (var i = 0; i < skill.need_sp.length; i++) {
        if (skill.level && sp[i] < skill.need_sp[i]) {
          this.add_sp(skill.nameid, i, skill.need_sp[i]);
          show = true;
        } else {
          this.remove_sp(skill.nameid, i, skill.need_sp[i]);
        }
      }
    }

    if (show) {
      this.$.show();
    } else {
      this.$.hide();
    }
  },

  add_skill: function(nameid, req_nameid, level) {
    var id = fmt('warn-skill-$0-$1-$2', nameid, req_nameid, level);
    var $warning = $('#'+id);
    if ($warning.length) {
      $warning.show();
    } else {
      $(document.createElement('li')).attr('id', id).addClass('skill-warning').html(fmt('&raquo; $0 requires $1 level $2', dnss.messages[nameid], dnss.messages[req_nameid], level)).insertAfter(this.$$);
    }
  },

  remove_skill: function(nameid, req_nameid, level) {
    var $warning = $(fmt('#warn-skill-$0-$1-$2', nameid, req_nameid, level));
    if ($warning.length) {
      $warning.hide();
    }
  },

  add_sp: function(nameid, adv, sp) {
    var id = fmt('warn-sp-$0-$1-$2', nameid, adv, sp);
    var $warning = $('#' + id);
    if ($warning.length) {
      $warning.show();
    } else {
      $(document.createElement('li')).attr('id', id).addClass('sp-warning').html(fmt('&raquo; $0 requires $1 to have $2 SP', dnss.messages[nameid], this.advancements[adv], sp)).appendTo(this.$);
    }
  },

  remove_sp: function(nameid, adv, sp) {
    var $warning = $(fmt('#warn-sp-$0-$1-$2', nameid, adv, sp));
    if ($warning.length) {
      $warning.hide();
    }
  }
};