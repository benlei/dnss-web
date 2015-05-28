var notifier = new (function Notifier(){
  var SKILL = 0;
  var SP = 1;
  var n = {};
  this.SKILL = SKILL;
  this.SP = SP;

  // initialize n
  n[SKILL] = {};
  n[SP] = {};


  this.addNotifier = function(t, id, k, v) {
    if (!n[t][k]) {
      n[t][k] = {};
    }

    n[t][k][id] = v;
  };


  this.notify = function(t, k, v) {
    if (! n[t][k]) {
      return;
    }

    for (var id in n[t][k]) {
      var val = v >= n[t][k][id], skill = dnss.getSkill(id);
      // minor optimizaiton... if it's balanced, and the val is true, why notify again?
      // the converse is not true... just because it's not balanced does not mean this specific id for the skill == val
      if (skill.isBalanced() && val) {
        continue;
      }

      switch (t) {
        case SP: skill.notifySP(k, val); break;
        case SKILL: skill.notifySkill(k, val); break;
      }
    }
  }
})();