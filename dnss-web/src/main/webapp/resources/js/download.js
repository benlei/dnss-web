var download = new (function Download() {
  var vertical = $("#dlv");
  var horizontal = $("#dlh");

  function dl(alignment) {
    var ids = [];
    $("#job-sp input:checked").each(function() {
      ids.push($(this).val());
    });

    if (! ids.length) {
      alert("No job has been checked to be downloaded!");
      return;
    }

    ids = ids.join('-');

    var frame = $("<iframe />");
    frame.attr("src", window.location.protocol + "//" + window.location.host + "/download/" + alignment + "/" + ids + "/" + properties.cap + "/" + build.toString());
    frame.css({height: "1px", width: "1px"});
    frame.appendTo("body");
    timedDelete(frame, 0, alignment+"-"+ids);
  }

  this.skill = function(id, name) {
    var frame = $("<iframe />");
    frame.attr("src", window.location.protocol + "//" + window.location.host + "/download/skill/" + id);
    frame.css({height: "1px", width: "1px"});
    frame.appendTo("body");
    timedDelete(frame, 0, name.toLowerCase().replace(/ /g,'-').replace(/[^\w-]+/g,''));
  };

  function timedDelete(frame, attempts, name) {
    if (attempts < 60) {
      if (attempts && frame.get(0).contentWindow.data) {
        var dl = document.createElement("a");
        dl.href = frame.get(0).contentWindow.data;
        dl.download = name+".png";
        document.body.appendChild(dl);
        dl.click();
        document.body.removeChild(dl);
        frame.remove();
      } else {
        setTimeout(function() { timedDelete(frame, attempts+1, name) }, 1000); // check every 1 seconds
      }

    } else { // just rmeove it, it still isn't done after 1 min
      frame.remove();
    }
  }

  vertical.click(function() {dl("v")});
  horizontal.click(function() {dl("h")});
})();