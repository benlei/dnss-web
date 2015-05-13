var build = new (function Build() {
  // private
  var t = this;
  var map = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+";
  var r = Array(72 + 1 + 1).join('-').split('');

  // public
  this.FLAG_POS = r.length - 1;

  this.get = function(position) {
    return map.indexOf(r[position]);
  };

  this.put = function(position, value) {
    direct_put(position, map.charAt(value));
  };

  function direct_put(position, c) {
    r[position] = c;
    if (position == t.FLAG_POS) {
      description.hook();
      search.hook();
    }
  }

  this.commit = function() {
    $("#build").val(window.location.protocol + "//" + window.location.host + "/job/" + properties.jobs[2].id + "-" + properties.cap + "?" + r.join(''));
  };

  this.toString = function() {
    return r.join('');
  };

  this.use = function(s) {
    var s = s.match(/^[0-9a-zA-Z-]+/g);
    if (! s) {
      return;
    }

    s = s.shift().split('');
    var hasFlag = s.length % 2;

    // Kali & Sin fix for backwards compatibility
    if ((hasJob("screamer") && s.length == 64+hasFlag) ||
        (hasJob("dancer")|hasJob("assassin") && s.length == 68+hasFlag)) {
      s.splice(20, 0, '-', '-', '-', '-');
    }

    if (hasJob("screamer") && s.length == 68+hasFlag) {
      s.splice(44, 0, '-', '-', '-', '-');
    }


    direct_put(this.FLAG_POS, hasFlag ? s[s.length - 1] : '-');

    for (var i = 0; i < s.length - 1; i++) {
      direct_put(i, s[i]);
    }
  };

  function hasJob(id) {
    var jobs = properties.jobs;
    return id == jobs[0].id || id == jobs[1].id || id == jobs[2].id;
  }

  // select the whole thing when clicked on
  $("#build").click(function() {$(this).select() });
})();
