<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<title>${jobs[2].name} - DNSS</title>
<meta charset="utf-8">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
<link href="/css/main.css" rel="stylesheet" type="text/css" />
<body>
<main>
  <aside>
    <ul id="job-list-sp"><c:forEach items="${jobs}" var="job" varStatus="loop">
      <li<c:if test="${loop.first}"> class="active"</c:if>>${job.name}<div class="right">0/${job.maxSP}</div></li></c:forEach>
      <li>Total SP<div class="right">0/${max_sp}</div></li>
    </ul>
  </aside>
  <section><c:forEach items="${jobs}" var="job" varStatus="jobLoop">
    <table class="skill-tree"><c:forEach items="${job.skillTree}" var="skillRow" varStatus="skillRowLoop">
      <tr><c:forEach items="${skillRow}" var="skill" varStatus="skillLoop"><c:choose><c:when test="${skill != 0}">
        <td class="skill-container">
          <div class="skill" data-id="${skill}" />
          <div class="lvl">17<br />10</div></c:when><c:otherwise>
        <td  class="skill-container"></c:otherwise></c:choose></c:forEach></c:forEach>
    </table></c:forEach>
  </section>
</main>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script type="text/javascript">
var jobIds = [<c:forEach items="${jobs}" var="job" varStatus="loop">'${job.identifier}'<c:if test="${!loop.last}">,</c:if></c:forEach>];
$.each(jobIds, function(i, jobId){
  $.getJSON('/json/' + jobId + '.json', function(job) {
    var icon = job.jobicon, skills = job.skills, messages = job.messages;
    $('.skill[data-id]').each(function() {
      var skill = skills[$(this).data('id')];
      if (skill) {
        var image = skill.image;
        var xpos = (skill.icon % 10) * -50;
        var ypos = Math.floor(skill.icon / 10) * -50;
        $(this).css('background', 'url(/images/skillicon' + image + '_b.png) ' + xpos + 'px ' + ypos + 'px');
      }
    });
  });  
});
</script>
</body>
</html>