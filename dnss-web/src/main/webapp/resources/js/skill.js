function Skill(skill, perm, messages) {
  this.image = skill.image;
  this.icon = skill.icon;
  this.perm = perm;

  this.get_image_link = function(active) {
    if (active) {
      return '/images/skillicon' + this.image + '.png';
    }

    return '/images/skillicon' + this.image + '_b.png';
  };

  this.get_icon_xy = function() {
    var x = (this.icon % 10) * -50;
    var y = Math.floor(this.icon / 10) * -50;
    return {x: x, y:y};
  };  
}