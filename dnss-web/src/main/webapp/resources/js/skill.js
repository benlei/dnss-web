function Skill(skill) {
  this.id = skill.id;
  this.next_level = null;
  this.prev_level = null;
  this.level = skill.level;
  this.advancement = skill.advancement;
  this.required_level = skill.required_level;
  this.max_level = null;

  // same for both pve and pvp
  this.get_name = function() { return dnss.messages[skill.nameid]; };
  this.get_spcost = function() { return skill.level <= dnss.max_skill_levels[skill.advancement] ? dnss.skill.spcost : 0; };
  this.get_type = function() { return dnss.types.skills[skill.type]; };

  // mode specific stuff
  this.get_mpcost = function() { return skill.mpcost[dnss.mode]; };
  this.get_cd = function() { return skill.cd[dnss.mode]; };
  this.get_description = function() { return dnss.messages[skill.explanationid[dnss.mode]].message_format(skill.explanationparams[dnss.mode]); };
  this.get_required_weapons = function() {
    var weaps = [];
    for (var i = 0; i < skill.needweaps.length; i++) {
      weaps.push(dnss.types.weapons[skill.needweaps[i]]);
    }

    return weaps;
  };

  this.get_skill_reqs = function() {
    var skills = [];
    for (var i = 0; i < skill.requires; i++) {
      var req = skill.requires[i];
      skills.push(dnss.get_skill(req.id, req.level));
    }

    return skills;
  };

  this.get_sp_reqs = function() { return skill.need_sp; };

  this.get_image_link = function() {
    return '/images/skillicon' + skill.image + '.png';
  };

  this.get_icon_coordinates = function() {
    var x = (skill.icon % 10) * -50;
    var y = Math.floor(skill.icon / 10) * -50;
    return {x: x, y:y};
  };


  // max level, not last level
  this.get_max_level = function() {
    if (this.max_level !== null) {
      return this.max_level;
    }

    var max_level = dnss.max_levels[this.advancement];
    var curr = this;
    while (curr.prev_level) {
      curr = curr.prev_level;
      if (curr.max_level) { // optimization to reduce path search
        this.max_level = curr.max_level;
        return this.max_level;
      }
    }

    while (curr.next_level && curr.next_level.required_level <= max_level) {
      curr = curr.next_level;
      if (curr.max_level) { // optimization to reduce path search
        this.max_level = curr.max_level;
        return this.max_level;
      }
    }

    this.max_level = curr;
    return curr;
  };

  this.use = function() {
    var $skill = dnss.$(this.id);
    $skill.css('background-image', fmt('url($0)',this.get_image_link()));
    $skill.css('background-position', fmt('${x}px ${y}px', this.get_icon_coordinates()));
    $skill.find('.lvl').text(fmt('$0/$1', this.level, this.get_max_level().level));
  };
}