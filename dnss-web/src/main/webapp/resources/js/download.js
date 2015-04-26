var download = new (function Download() {
  var vertical = $("#dlv");
  var horizontal = $("#dlh");
  var frames = [];

  function dl(alignment) {
    var ids = [];
    $("#download input:checkbox:checked").each(function() {
      ids.push($(this).val());
    });

    if (! ids.length) {
      alert("No job has been checked to be downloaded!");
      return;
    }

    ids = ids.join(',');

    var frame = $("<iframe />");
    frame.attr("src", window.location.protocol + "//" + window.location.host + "/download/" + alignment + "/" + ids + "/" + properties.cap + "/" + build.toString());
    frame.css({height: "1px", width: "1px"});
    frame.appendTo("body");
    timedDelete(frame, 0);

  }

  function timedDelete(frame, attempts) {
    if (attempts < 12) { // 12*5 = 60s = 1min
      if (attempts && frame.contentWindow.done) {
        frame.remove();
      } else {
        setTimeout(function() {
          timedDelete(frame, attempts+1);
        }, 5000); // check every 5 seconds
      }

    } else { // just rmeove it, it still isn't done after 1 min
      frame.remove();
    }
  }

  vertical.click(function() {dl("v")});
  horizontal.click(function() {dl("h")});
})();