function Skill(skill) {
  this.next_level = null;
  this.prev_level = null;

  this.get_image_link = function() {
    return '/images/skillicon' + skill.image + '.png';
  };

  this.get_icon_coordinates = function() {
    var x = (skill.icon % 10) * -50;
    var y = Math.floor(skill.icon / 10) * -50;
    return {x: x, y:y};
  };


  // same for both pve and pvp
  this.get_name = function() { return dnss.messages[skill.nameid]; };
  this.get_level = function() { return skill.level; };
  this.get_spcost = function() { return skill.level <= dnss.max_skill_levels[skill.advancement] ? dnss.skill.spcost : 0; };
  this.get_required_level = function() { return skill.required_level; };
  this.get_advancement = function() { return skill.advancement; };
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
}