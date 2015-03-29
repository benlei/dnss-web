String.prototype.message_format = function (args) {
  if (args && args.constructor === Array) {
    arguments = args;
  }

  if (!arguments) {
    return this.toString();
  }

  var str = this, m;
  while (m=str.match(/{[0-9]+}/g)) {
    for (var i in m) {
      var k = m[i].substring(1, m[i].length - 1);
      str = str.replace(m[i], arguments[k] === undefined ? '' : arguments[k]);
    }
  }
  return str.toString();
};

String.prototype.description_format = function(params) {
  str = this.message_format(params);
  var c = 0, w = 0, p = 0, newStr = '', startPos = 0;
  for (var i = 0; i < str.length - 1; i++) {
    switch (str.substr(i, 2)) {
      case '#y':
      case '#p':
        if (c - w == 1) { // needed a closing </span>
          newStr += str.substring(startPos, i) + '</span><span class="' + str.substr(i+1,1) + '">';
        } else {
          newStr += str.substring(startPos, i) + '<span class="' + str.substr(i+1,1) + '">';
          c++;
        }
        
        startPos = i + 2;
        ++i;
        break;
      case '#w':
        if (w == c) { // early #w
          newStr +=  str.substring(startPos, i);
        } else {
          newStr += str.substring(startPos, i) + '</span>';
          w++;
        }

        startPos = i + 2;
        ++i;
        break;
      default:
        break;
    }
  }

  newStr = newStr + str.substring(startPos);

  if (c != w) {
    newStr = newStr + '</span>';
  }

  return newStr.replace(/\\n/g, '<br />');
};

function fmt (str) {
  if (arguments.length == 1) {
    return str;
  }

  var args = [];
  for (var i = 1; i < arguments.length; i++) {
    args.push(arguments[i]);
  }

  var m;
  if (typeof args[0] === 'object') {
    args = args[0];
    while (m = str.match(/\$[0-9a-zA-Z_]+/g)) {
      for (var i in m) {
        var k = m[i].substr(1);
        str = str.replace(m[0], args[k] === undefined ? '' : args[k]);
      }
    }

    while (m = str.match(/\${[0-9a-zA-Z_]+?}/g)) {
      for (var i in m) {
        var k = m[i].substring(2, m[i].length - 1);
        str = str.replace(m[0], args[k] === undefined ? '' : args[k]);
      }
    }

    return str;
  } else {
    while (m=str.match(/\$[0-9]+/g)) {
      for (var i in m) {
        var k = m[i].substr(1);
        str = str.replace(m[i], args[k] === undefined ? '' : args[k]);
      }
    }
    return str;
  }
}