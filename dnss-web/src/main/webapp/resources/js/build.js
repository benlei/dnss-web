var buildMap = ["-", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"], iBuildMap = {};
for (var i = 0; i < buildMap.length; i++) { iBuildMap[buildMap[i]] = i; }

function Build() {
  var r = Array(72 + 1 + 1).join("-").split("");

  this.get = function(position) {
    return r[position];
  };

  this.put = function(position, level) {
    r[position] = buildMap[level]
  };

  this.notify = function() {
    $("#build").val(window.location.protocol + "//" + window.location.host + "/job/" + properties.base + "?" + r.join(""));
  };

  function hasJob(id) {
    return id == properties.jobs[0].id || id == properties.jobs[1].id || id == properties.jobs[2].id;
  }

  if (window.location.search.substr(1).length > 48) {
    var s = window.location.search.substr(1).match(/^[0-9a-zA-Z-]+/g);
    if (s) {
      s = s.shift().split("");
      var hasMode = s.length % 2;

      // Kali & Sin fix for backwards compatibility
      if ((hasJob("screamer") && s.length == 64+hasMode) ||
          (hasJob("dancer")|hasJob("assassin") && s.length == 68+hasMode)) {
        s.splice(20, 0, "-", "-", "-", "-");
      }

      if (hasJob("screamer") && s.length == 68+hasMode) {
        s.splice(44, 0, "-", "-", "-", "-");
      }

      r[r.length - 1] = s[s.length - 1];
      for (var i = 0; i < s.length - 1; i++) {
        r[i] = s[i];
      }
    }
  }
};