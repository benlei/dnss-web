function Skill(skill) {
  /* direct assignments */
  this.id = skill.id;
  this.nameid = skill.nameid;
  this.level = skill.level;
  this.advancement = skill.advancement;
  this.required_level = skill.required_level;
  this.mpcost = skill.mpcost;
  this.cd = skill.cd;
  this.explanationid = skill.explanationid;
  this.explanationparams = skill.explanationparams;
  this.sp_requirements = skill.sp_requirements;
  this.spcost = skill.spcost;


  /* other attributes */
  this.total_sp_cost = 0;

  /* pointers to other levels */
  this.next = null;
  this.prev = null;
  this.max = null;
  this.start = null;
  this.end = null;


  /* methods */
  this.get_spcost = function() {
    return this.required_level <= dnss.max_levels[this.advancement] ? this.spcost : 0;
  };

  this.get_type = function() {
    return dnss.types.skills[skill.type];
  };

  // mode specific stuff
  this.get_required_weapons = function() {
    var weaps = [];
    for (var i = 0; i < skill.needweapon.length; i++) {
      weaps.push(dnss.types.weapons[skill.needweapon[i]]);
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

  this.get_description = function() {
  }

  this.get_image_link = function() {
    return '/images/skillicon' + skill.image + '.png';
  };

  this.get_icon_coordinates = function() {
    var x = (skill.icon % 10) * -50;
    var y = Math.floor(skill.icon / 10) * -50;
    return {x: x, y:y};
  };

  this.bind = function($skill) {
    $skill.css('background-image', fmt('url($0)', this.get_image_link()));
    $skill.css('background-position', fmt('${x}px ${y}px', this.get_icon_coordinates()));
    $skill.find('.lvl').text(fmt('$0/$1', this.level, this.max.level));
    $skill.data('skill', this);
    dnss.build[$skill.data('position')] = build_map[this.level - (this.start.level == 1)];
  };
}