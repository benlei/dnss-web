var messages = {};
var skills = {};

var dnss = {
  jobs: [],
  skillicon: {},


  init: function(options) {
    this.jobs = options.jobs;
    this.skillicon = options.skillicon;

    // want the skill information first
    for (var i = 0; i < options.jobs.length; i++) {
      this.loadSkillJSON(options.json.version, optionsjobs[i].id);
    }

    for (var i = 0; i < options.jobs.length; i++) {
      this.loadMessagesJSON(options.json.version, optionsjobs[i].id);
    }
  },


  loadSkillJSON: function(version, id) {
    jQuery.getJSON('/json/' + version + '-' + id + '-skills.json', function(json) {
      jQuery.each(json, function(i, skill) {
        skills[i] = new Skill(json);
      });
    });
  },

  loadMessagesJSON: function(version, id) {
    jQuery.getJSON('/json/' + version + '-' + id + '-messages.json', function(json) {
      jQuery.each(json, function(i, message) {
        messages[i] = message;
      });
    });
  }
};