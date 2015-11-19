<%@page pageEncoding="UTF-8" %><!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<title>${jobs.tertiary.name} - Dragon Nest Skill Simulator</title>
<meta charset="utf-8">
<meta name="description" content="Simulate and share your Dragon Nest skill builds using the Dragon Nest Skill Simulator (North America)!">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico"/>
<link href="/dnss.css?${time}" rel="stylesheet" type="text/css"/>
<body>
<main>
	<nav id="jobs" class="no-select">
		<ul><c:forEach items="${primaries}" var="primary" varStatus="pLoop">
			<li class="primary<c:if test="${primary == jobs.primary}"> active</c:if>">${primary.name}
			<ul class="sub-jobs"><c:forEach items="${secondaries}" var="secondary" varStatus="sLoop"><c:if test="${secondary.parent == primary}">
				<li class="secondary">${secondary.name}</li><c:forEach items="${tertiaries}" var="tertiary" varStatus="tLoop"><c:if test="${tertiary.parent == secondary}">
				<li class="tertiary"><a href="/job/${tertiary.identifier}">${tertiary.name}</a></li></c:if></c:forEach></c:if></c:forEach></ul>
			</li></c:forEach>
		</ul>
	</nav>
	<aside id="build-box">
		<div id="build-text">Build URL:</div>
		<input type="text" id="build"/>
		<div id="download">
			<ul>
				<li>Image
					<ul>
						<li><a href="javascript:download.skillTrees('h')">Landscape (Horizontal)</a></li>
						<li><a href="javascript:download.skillTrees('v')">Portrait (Vertical)</a></li>
					</ul>
				</li>
			</ul>
		</div>
	</aside>
	<aside id="sidebar-1">
		<ul id="job-sp" class="no-select"><c:forEach items="${jobs.iterator}" var="job" varStatus="loop">
			<li id="job-sp-${loop.index}"<c:if test="${loop.first}"> class="active"</c:if>><input type="checkbox" value="${job.identifier}" checked>${job.name}<div class="sp">0/${job.maxSP}</div></li></c:forEach>
		</ul>
		<ul id="total-sp">
			<li>Total SP<div class="sp">${jobs.maxSP}/0/${jobs.maxSP}</div></li>
		</ul>
		<div id="levelcap">
			Level Cap
			<div id="levelinputs">
				<input type="text" id="cap" value="${jobs.level}"/>
				<input type="button" id="capchanger" value="Reset"/>
			</div>
		</div>
		<div id="searchbox">
			Search
			<input type="search" id="search"/>
		</div>
	</aside>
	<section><c:forEach items="${jobs.iterator}" var="job" varStatus="jobLoop">
		<table class="skill-tree no-select" id="skill-tree-${jobLoop.index}"><c:forEach items="${job.skillTree}" var="skillRow" varStatus="skillRowLoop">
			<tr<c:if test="${skillRowLoop.last && job.compactable}"> style="display:none"</c:if>><c:forEach items="${skillRow}" var="skill" varStatus="skillLoop"><c:choose><c:when test="${empty skill}">
				<td class="container" /></c:when><c:otherwise>
				<td class="container">
					<div class="skill" id="skill-${skill.id}" style="background:url('/icons/skillicon${skill.sprite}.png?${time}') ${skill.spriteXY};"/>
					<div class="lvl">${skill.level}/${skill.maxLevel}</div></c:otherwise></c:choose></c:forEach></c:forEach>
		</table></c:forEach>
	</section>
	<aside id="sidebar-2">
		<input type="button" id="mode" value="pve"/><h2 id="skill-name"></h2>
		<div id="skill-level" class="y">Skill Lv.: <span class="w"></span></div>
		<div id="skill-mp" class="y">Fee MP: <span class="w" data-after="of MP"></span></div>
		<div id="skill-required-weapon" class="y">Required Weapon(s): <span class="w"></span></div>
		<div id="skill-type" class="y">Skill Type: <span class="w"></span></div>
		<div id="skill-element" class="y">Attribute: <span class="w"></span></div>
		<div id="skill-cd" class="y">Cooldown: <span class="w" data-after="sec"></span></div>
		<div id="skill-total-sp" class="y">Total SP: <span class="w"></span></div>
		<div class="separator"></div>
		<div class="y">Level Up Requirements:</div>
		<div id="skill-required-level">Character Level <span class="w"></span></div>
		<div id="skills-required"></div>
		<div id="sp-required"></div>
		<div id="skill-sp">SP <span class="w"></span></div>
		<div class="separator"></div>
		<div id="skill-description" class="y">Skill Description:
			<div class="d"></div>
		</div>
		<div id="next-description" class="y">Next Description:
			<div class="d"></div>
		</div>
	</aside>
</main>
</body>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script src="/dnss.js?${time}"></script>
<script type="text/javascript">
var properties = {jobs:[<c:forEach items="${jobs.iterator}" var="job" varStatus="loop">{id:"${job.identifier}",name:"${job.name}"}<c:if test="${!loop.last}">,</c:if></c:forEach>],
	sp:[<c:forEach items="${jobs.iterator}" var="job" varStatus="loop">${job.maxSP},</c:forEach>${jobs.maxSP}],
	skilltypes:[<c:forEach items="${skill_types}" var="type" varStatus="loop">"${type}"<c:if test="${!loop.last}">,</c:if></c:forEach>],
	skillelements:[<c:forEach items="${skill_elements}" var="element" varStatus="loop">"${element}"<c:if test="${!loop.last}">,</c:if></c:forEach>],
	weapontypes:{<c:forEach items="${weapon_types}" var="e" varStatus="loop">${e.key}:"${e.value}"<c:if test="${!loop.last}">,</c:if></c:forEach>},
	cap: ${jobs.level},
	max_cap: ${max_cap}};
dnss.start("${time}");
</script>
</html>