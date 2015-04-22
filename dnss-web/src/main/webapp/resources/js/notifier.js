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
      switch (t) {
        case SP: skill.notifySP(k, val); break;
        case SKILL: skill.notifySkill(k, val); break;
      }
    }
  }
})();