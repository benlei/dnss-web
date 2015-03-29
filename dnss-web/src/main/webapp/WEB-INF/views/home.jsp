<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html lang="en">
<title>${jobs[fn:length(jobs) - 1].name} - DNSS</title>
<meta charset="utf-8">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
<link href="/css/main.css" rel="stylesheet" type="text/css" />
<body>
<main>
  <nav id="jobs" class="no-select">
    <ul><c:forEach items="${job0}" var="primary" varStatus="pLoop">
      <li class="primary">${primary.name}
        <ul class="sub-jobs"><c:forEach items="${job1}" var="secondary" varStatus="sLoop"><c:if test="${secondary.parent == primary}">
          <li class="secondary">${secondary.name}</li><c:forEach items="${job2}" var="tertiary" varStatus="tLoop"><c:if test="${tertiary.parent == secondary}">
          <li class="tertiary"><a href="/job/${tertiary.identifier}">${tertiary.name}</a></li></c:if></c:forEach>
        </c:if></c:forEach></ul>
      </li></c:forEach>
    </ul>
  </nav>
  <aside id="builder" data-base="http://dnss.herokuapp.com/job/${jobs[fn:length(jobs) - 1].identifier}"></aside>
  <aside id="sidebar-1" class="no-select">
    <ul id="job-list-sp"><c:forEach items="${jobs}" var="job" varStatus="loop">
      <li data-job="${loop.index}"<c:if test="${loop.first}"> class="active"</c:if>>${job.name}<div class="sp">0/${job.maxSP}</div></li></c:forEach>
      <li>Total SP<div class="sp">0/${max_sp}</div></li>
    </ul>
  </aside>
  <section><c:forEach items="${jobs}" var="job" varStatus="jobLoop">
    <table class="skill-tree no-select" id="skill-tree-${jobLoop.index}"><c:forEach items="${job.skillTree}" var="skillRow" varStatus="skillRowLoop">
      <tr><c:forEach items="${skillRow}" var="skill" varStatus="skillLoop"><c:choose><c:when test="${skill != 0}">
        <td class="container">
          <div class="skill" data-id="${skill}" />
          <div class="lvl">0/0</div></c:when><c:otherwise>
        <td class="container"></c:otherwise></c:choose></c:forEach></c:forEach>
    </table></c:forEach>
  </section>
  <aside id="sidebar-2">
    <input type="button" id="mode" value="pve" /><h2 id="skill-name">Magma Monument</h2>
    <div class="skill-description">
      <ul class="meta">
        <li id="skill-level"><span class="y">Skill Lv.: </span><span class="w"></span></li>
        <li id="skill-sp"><span class="y">SP Consumed: </span><span class="w"></span></li>
        <li id="skill-mpcost"><span class="y">Fee MP: </span><span class="w"></span> of base MP</li>
        <li id="skill-cd"><span class="y">Cooldown: </span><span class="w"></span> sec</li>
        <li id="skill-required-level"><span class="y">Level Limit: </span><span class="w"></span></li>
        <li id="skill-required-weapon"><span class="y">Required Weapon: </span><span class="w"></span></li>
        <li id="skill-type"><span class="y">Skill Type: </span><span class="w"></span></li>
        <li id="skill-description"><span class="y">Skill Description:</span><div class="description"></div></li>
        <li id="next-description"><span class="y">Next Description:</span><div class="description"></div></li>
      </ul>
    </div>
  </aside>
</main>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script src="/js/lib.js"></script>
<script src="/js/dnss.js"></script>
<script src="/js/skill.js"></script>
<script type="text/javascript">
var jobIds = [<c:forEach items="${jobs}" var="job" varStatus="loop">'${job.identifier}'<c:if test="${!loop.last}">,</c:if></c:forEach>];
var max_sp = [<c:forEach items="${jobs}" var="job" varStatus="loop">${job.maxSP},</c:forEach>${max_sp}]
var max_level = 80;
var max_skill_levels = [80, 70, 80];

$('.skill-tree').hide();
$('#skill-tree-0').show();

$('#job-list-sp li[data-job]').each(function() {
  var idx = $(this).data('job');
  $(this).click(function() {
    $('#job-list-sp li[data-job][class="active"]').removeClass('active');
    $(this).addClass('active');
    $('.skill-tree').hide();
    $('#skill-tree-' + idx).show();
  })
});

// if (window.location.search && window.location.search.substr(1)) {
//   var skills = window.location.search.substr(1);
//   if (skills) {
//     rebuildSimulation(skills);
//   }
// }

$(document).ready(function() {
  dnss.init(jobIds, max_level, max_skill_levels, max_sp);
}); 
</script>
</body>
</html>