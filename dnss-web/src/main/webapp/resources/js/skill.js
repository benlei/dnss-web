function Skill(id, s, e) {
  var advancement = -1;
  var level = 0;
  this.setLevel = function(lvl) { level = lvl };
  this.getLevel = function() { return level+isDefault };

  var isDefault = false;
  this.setDefault = function(d) { isDefault = d };

  this.getAdvancement = function() {
    if (advancement == -1) {
      advancement = e.parents(".skill-tree").attr("id").substr(-1);
    }
    return advancement;
  };

  this.getPosition = function() { return e.data('pos') };

  this.getSprite = function() {
    var lvl = this.getLevel() ? "" : "_b";
    return "/images/" + version.skillicon + "_skillicon" + s.image + lvl + ".png";
  };

  this.getCoordinates = function() {
    var x = (s.icon % 10) * -50;
    var y = Math.floor(s.icon / 10) * -50;
    return x+"px "+y+"px";
  };

  this.trigger = function() {
    if (! this.getLevel()) {
    }
  };
}