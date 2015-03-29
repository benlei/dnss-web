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

var build_map = ["-", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"];
var inv_build_map = {"-":0, "0":1,"1":2,"2":3,"3":4,"4":5,"5":6,"6":7,"7":8,"8":9,"9":10,"A":11,"B":12,"C":13,"D":14,"E":15,"F":16,"G":17,"H":18,"I":19,"J":20,"K":21,"L":22,"M":23,"N":24,"O":25,"P":26,"Q":27,"R":28,"S":29,"T":30,"U":31,"V":32,"W":33,"X":34,"Y":35,"Z":36,"a":37,"b":38,"c":39,"d":40,"e":41,"f":42,"g":43,"h":44,"i":45,"j":46,"k":47,"l":48,"m":49,"n":50,"o":51,"p":52,"q":53,"r":54,"s":55,"t":56,"u":57,"v":58,"w":59,"x":60,"y":61,"z":62};