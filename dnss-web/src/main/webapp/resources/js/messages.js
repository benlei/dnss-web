function Messages() {
  var dict = {};

  this.put = function(id, message) { dict[id] = message; };

  this.get = function(id, params) {
    var message = dict[id], m, k;
    if (params) {
      while (m=message.match(/{[0-9]+}/g)) {
        for (var i = 0; i < m.length; i++) {
          k = m[i].substring(1, m[i].length - 1);
          message = message.replace(m[i], params[k] === undefined ? "" : params[k]);
        }
      }
    }

    return message;
  };
}

var messages = new Messages();