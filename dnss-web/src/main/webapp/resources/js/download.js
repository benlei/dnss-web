var download = new (function Download() {
  var vertical = $("#dlv");
  var horizontal = $("#dlh");
  var DELAY = 250; // ms
  var WAIT_TIME = 60000;

  this.skillTrees = function(alignment) {
      var ids = [];
      $("#job-sp input:checked").each(function() {
        ids.push($(this).val());
      });

      if (! ids.length) {
        alert("No job has been checked to be downloaded!");
        return;
      }

      ids = ids.join('-');

      dl(ids+"-"+properties.cap, alignment + "/" + ids + "/" + properties.cap + "/" + build.toString());
  };

  this.skill = function(id, name) {
    dl(name.toLowerCase().replace(/ /g,'-').replace(/[^\w-]+/g,''), "skill/" + id);
  };

  function dl(name, path) {
    var frame = $("<iframe />");
    frame.attr("src", window.location.protocol + "//" + window.location.host + "/download/" + path);
    frame.css({height: "1px", width: "1px"});
    frame.appendTo("body");
    timedDelete(frame, 0, name);
  }

  function timedDelete(frame, total_delayed, name) {
    if (total_delayed < WAIT_TIME) {
      if (total_delayed && frame.get(0).contentWindow.data) {
        var dl = document.createElement("a");
        dl.href = frame.get(0).contentWindow.data;
        dl.download = name+".png";
        document.body.appendChild(dl);
        dl.click();
        document.body.removeChild(dl);
        frame.remove();
      } else {
        setTimeout(function() { timedDelete(frame, total_delayed + DELAY, name) }, DELAY); // check every 1 seconds
      }

    } else { // just rmeove it, it still isn't done after 1 min
      frame.remove();
    }
  }
})();