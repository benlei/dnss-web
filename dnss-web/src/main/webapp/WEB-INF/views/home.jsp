<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><fmt:setBundle basename="dnss" var="dnss"/>
<html lang="en">
<title>${jobs.tertiary.name} - Dragon Nest Skill Simulator</title>
<meta charset="utf-8">
<meta name="description" content="Simulate and share your Dragon Nest skill builds using the Dragon Nest Skill Simulator (North America)!">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
<link href="/<fmt:message key="timestamp" bundle="${dnss}"/>-dnss.css" rel="stylesheet" type="text/css"/>
<body>
<main>
<nav id="jobs" class="no-select">
<ul><c:forEach items="${primaries}" var="primary" varStatus="pLoop">
<li class="primary">${primary.name}
<ul class="sub-jobs"><c:forEach items="${secondaries}" var="secondary" varStatus="sLoop"><c:if test="${secondary.parent.identifier == primary.identifier}">
<li class="secondary">${secondary.name}</li><c:forEach items="${tertiaries}" var="tertiary" varStatus="tLoop"><c:if test="${tertiary.parent.identifier == secondary.identifier}">
<li class="tertiary"><a href="/job/${tertiary.identifier}">${tertiary.name}</a></li></c:if></c:forEach></c:if></c:forEach></ul>
</li></c:forEach>
</ul>
</nav>
<aside id="build-box"><div id="build-text">Build URL:</div><input type="text" id="build"/></aside>
<aside id="sidebar-1">
<ul id="job-sp" class="no-select"><c:forEach items="${jobs.iterator}" var="job" varStatus="loop">
<li id="job-sp-${loop.index}"<c:if test="${loop.first}"> class="active"</c:if>>${job.name}<div class="sp"></div></li></c:forEach>
<li/>
<li>Total SP<div class="sp"></div></li>
</ul>
</aside>
<section><c:forEach items="${jobs.iterator}" var="job" varStatus="jobLoop">
<table class="skill-tree no-select" id="skill-tree-${jobLoop.index}"><c:forEach items="${job.skillTree}" var="skillRow" varStatus="skillRowLoop">
<tr><c:forEach items="${skillRow}" var="skill" varStatus="skillLoop"><c:choose><c:when test="${empty skill}">
<td class="container" /></c:when><c:otherwise>
<td class="container">
<div class="skill" id="skill-${skill.id}"/>
<div class="lvl"></div></c:otherwise></c:choose></c:forEach></c:forEach>
</table></c:forEach>
</section>
<aside id="sidebar-2">
<input type="button" id="mode" value="pve"/><h2 id="skill-name"></h2>
<div class="skill-description">
<ul class="meta">
<li id="skill-level"><span class="y">Skill Lv.: </span><span class="w"></span></li>
<li id="skill-sp"><span class="y">Consumes SP: </span><span class="w"></span></li>
<li id="skill-total-sp"><span class="y">Total SP Consumed: </span><span class="w"></span></li>
<li id="skill-mp"><span class="y">Fee MP: </span><span class="w"></span> of base MP</li>
<li id="skill-cd"><span class="y">Cooldown: </span><span class="w"></span> sec</li>
<li id="skill-type"><span class="y">Skill Type: </span><span class="w"></span></li>
<li id="skill-required-weapon"><span class="y">Required Weapon(s): </span><span class="w"></span></li>
<li id="skill-required-level"><span class="y">Level Limit: </span><span class="w"></span></li>
<li id="sp-required"><span class="y">SP Required: </span><span class="w"></span></li>
<li id="skills-required"><span class="y">Required Skills: </span><span class="w"></span></li>
<li id="skill-description"><span class="y">Skill Description:</span><div class="d"></div></li>
<li id="next-description"><span class="y">Next Description:</span><div class="d"></div></li>
</ul>
</div>
</aside>
</main>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script src="/<fmt:message key="timestamp" bundle="${dnss}"/>-dnss.js"></script>
<script type="text/javascript">
var properties = {jobs:[<c:forEach items="${jobs.iterator}" var="job" varStatus="loop">{id:"${job.identifier}",name:"${job.name}"}<c:if test="${!loop.last}">,</c:if></c:forEach>],
  max:{required_level:[<c:forEach items="${jobs.iterator}" var="job" varStatus="loop">${job.maxSkillRequiredLevel}<c:if test="${!loop.last}">,</c:if></c:forEach>],sp:[<c:forEach items="${jobs.iterator}" var="job" varStatus="loop">${job.maxSP},</c:forEach>${jobs.maxSP}]},
  version:{json:<fmt:message key="json.version" bundle="${dnss}"/>,skillicon:<fmt:message key="skillicon.version" bundle="${dnss}"/>},
  skilltypes:[<c:forEach items="${skill_types}" var="type" varStatus="loop">"${type}"<c:if test="${!loop.last}">,</c:if></c:forEach>],
  weapontypes:{<c:forEach items="${weapon_types}" var="e" varStatus="loop">${e.key}:"${e.value}"<c:if test="${!loop.last}">,</c:if></c:forEach>},
  cap: ${jobs.level}};
dnss.start();
</script>
</body>
</html>