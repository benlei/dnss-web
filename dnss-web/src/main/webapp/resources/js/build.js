var build = new (function Build() {
  // private
  var t = this;
  var map = ["-", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", ".", "+"];
  var imap = {};
  var r = Array(72 + 1 + 1).join(map[0]).split("");

  // public
  this.FLAG_POS = r.length - 1;

  this.get = function(position) {
    return imap[r[position]];
  };

  this.put = function(position, mapPos) {
    r[position] = map[mapPos]

    if (position == this.FLAG_POS) {
      description.hook();
    }
  };

  function iput(position, i) {
    t.put(position, imap[i]);
  }

  this.notify = function() {
    $("#build").val(window.location.protocol + "//" + window.location.host + "/job/" + properties.base + "?" + r.join(""));
  };

  this.use = function(s) {
    var s = s.match(/^[0-9a-zA-Z-]+/g);
    if (! s) {
      return;
    }

    s = s.shift().split("");
    var hasFlag = s.length % 2;

    // Kali & Sin fix for backwards compatibility
    if ((hasJob("screamer") && s.length == 64+hasFlag) ||
        (hasJob("dancer")|hasJob("assassin") && s.length == 68+hasFlag)) {
      s.splice(20, 0, "-", "-", "-", "-");
    }

    if (hasJob("screamer") && s.length == 68+hasFlag) {
      s.splice(44, 0, "-", "-", "-", "-");
    }


    iput(this.FLAG_POS, hasFlag ? s[s.length - 1] : 0);

    for (var i = 0; i < s.length - 1; i++) {
      iput(i, s[i]);
    }
  };

  function hasJob(id) {
    var jobs = properties.jobs;
    return id == jobs[0].id || id == jobs[1].id || id == jobs[2].id;
  }

  for (var i = 0; i < map.length; i++) {
    imap[map[i]] = i;
  }
})();
