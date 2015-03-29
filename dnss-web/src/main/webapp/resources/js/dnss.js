var dnss = {
  jobs: [],
  types: {},
  job_queue: [],

  init: function(jobs, max_levels) {
    $.getJSON('/json/types.min.json', function(json) {
      this.types = json;
      while (job_queue.length) {
        var j = job_queue.shift();
        this.add_job(j.adv, j.json);
      }
    });

    for (var i in jobs) {
      $.getJSON(fmt('/json/$0.min.json',jobs[i]), function(json) {
        this.jobs[i] = json;
        if (this.types) {
          this.add_job(i, json);
        } else {
          this.job_queue.push({adv: i, json: json});
        }
      });
    }
  },

  add_job: function(adv, json) {
    jobs[i] = new Job(adv, json);
  }
};