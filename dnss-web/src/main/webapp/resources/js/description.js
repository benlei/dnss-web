var description = {
  separator: ' &rarr; ',
  skill: null,
  mode: 'pve',

  use: function(skill) {
    $('#sidebar-2').show();
    if (this.skill == skill) { // don't bother changing html
      return;
    }

    this.skill = skill;

    this.update_name();
    this.update_level();
    this.update_type();
    this.update_sp();
    this.update_mp();
    this.update_cd();
    this.update_required_level();
    this.update_required_weapons();
    this.update_descriptions();
  },

  flip: function() {
    switch (this.mode) {
      default:
      case 'pve': this.mode = 'pvp'; dnss.build[dnss.build.length - 1] = build_map[1]; break;
      case 'pvp': this.mode = 'pve'; dnss.build[dnss.build.length - 1] = build_map[0]; break;
    }

    this.update_mp();
    this.update_cd();
    this.update_descriptions();
  },

  update_name: function() {
    $('#skill-name').html(dnss.messages[this.skill.nameid]);
  },

  update_level: function () {
    if (! this.skill.prev && this.skill.level == 0) {
      $('#skill-level .w').html(this.skill.next.level);
    } else if (! this.skill.next) {
      $('#skill-level .w').html(this.skill.level);
    } else {
      $('#skill-level .w').html(this.skill.level+this.separator+this.skill.next.level);
    }
  },

  update_sp: function() {
    if (! this.skill.next) {
      $('#skill-sp .w').html(0);
    } else {
      $('#skill-sp .w').html(this.skill.next.get_spcost());
    }

    if (! this.skill.prev) {
      $('#skill-total-sp .w').html(0);
    } else {
      $('#skill-total-sp .w').html(this.skill.total_sp_cost);
    }
  },

  update_mp: function() {
    if (! this.skill.prev && ! this.skill.level) {
      $('#skill-mp .w').html(this.skill.next.mpcost[this.mode]);
    } else if (! this.skill.next) {
      $('#skill-mp .w').html(this.skill.mpcost[this.mode]);
    } else {
      $('#skill-mp .w').html(this.skill.mpcost[this.mode]+this.separator+this.skill.next.mpcost[this.mode]);
    }
  },

  update_cd: function() {
    if (! this.skill.prev && ! this.skill.level) {
      $('#skill-cd .w').html(this.skill.next.cd[this.mode]);
    } else if (! this.skill.next) {
      $('#skill-cd .w').html(this.skill.cd[this.mode]);
    } else {
      if (this.skill.cd[this.mode] == this.skill.next.cd[this.mode]) {
        $('#skill-cd .w').html(this.skill.cd[this.mode]);
      } else {
        $('#skill-cd .w').html(this.skill.cd[this.mode]+this.separator+this.skill.next.cd[this.mode]);
      }
    }
  },

  update_required_level: function() {
    if (! this.skill.prev && ! this.skill.level) {
      $('#skill-required-level .w').html(this.skill.next.required_level);
    } else if (! this.skill.next) {
      $('#skill-required-level .w').html(this.skill.required_level);
    } else {
      $('#skill-required-level .w').html(this.skill.required_level+this.separator+this.skill.next.required_level);
    }
  },

  update_required_weapons: function() {
    var weaps = this.skill.get_required_weapons();
    if (! weaps.length) {
      $('#skill-required-weapon .w').html('Any');
    } else {
      $('#skill-required-weapon .w').html(weaps.join(', '));
    }
  },

  update_type: function() {
    $('#skill-type .w').html(this.skill.get_type());
  },

  update_descriptions: function() {
    if (! this.skill.prev && ! this.skill.level) {
      $('#skill-description .description').html(dnss.messages[this.skill.next.explanationid[this.mode]].message_format(this.skill.next.explanationparams[this.mode]).dn_format());
      $('#next-description').hide();
    } else {
      $('#skill-description .description').html(dnss.messages[this.skill.explanationid[this.mode]].message_format(this.skill.explanationparams[this.mode]).dn_format());
      if (this.skill.next) {
        $('#next-description .description').html(dnss.messages[this.skill.next.explanationid[this.mode]].message_format(this.skill.next.explanationparams[this.mode]).dn_format());
        $('#next-description').show();
      } else {
        $('#next-description').hide();
      }
    }
  },
};