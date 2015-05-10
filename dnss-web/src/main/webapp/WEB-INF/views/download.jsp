<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><fmt:setBundle basename="dnss" var="dnss"/>
<html lang="en">
<meta charset="utf-8">
<link href="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.css" rel="stylesheet" type="text/css"/>
<body>
<main><c:forEach items="${jobs.iterator}" var="job" varStatus="jobLoop"><c:if test="${not empty job}">
	<section class="${alignment}">
		<div class="job">${job.name}<div class="sp">${job.usedSP}/${job.maxSP}</div></div>
		<table class="skill-tree no-select" id="skill-tree-${jobLoop.index}"><c:forEach items="${job.skillTree}" var="skillRow" varStatus="skillRowLoop">
			<tr><c:forEach items="${skillRow}" var="skill" varStatus="skillLoop"><c:choose><c:when test="${empty skill}">
				<td class="container" /></c:when><c:otherwise>
				<td class="container">
					<div class="skill" style="background:url('/skillicons/<fmt:message key="skillicon.version" bundle="${dnss}"/>_skillicon${skill.sprite}.png') ${skill.spriteXY};"/>
					<div class="lvl">${skill.level}/${skill.maxLevel}</div></c:otherwise></c:choose></c:forEach></c:forEach>
		</table>
	</section></c:if></c:forEach>
	<aside>Total SP: ${jobs.totalSP}/${jobs.maxSP}</aside>
</main>
</body>
<script src="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.js"></script>
<script type="text/javascript">
var data=null;<c:if test="${alignment == 'h'}">
var width = document.getElementsByTagName("section").length*285;<c:if test="${not empty jobs.tertiary}">
width -= 130;</c:if></c:if><c:if test="${alignment == 'v'}">
width = 280;</c:if>
document.getElementsByTagName("main")[0].style.width = width+"px";
html2canvas(document.getElementsByTagName('main')[0],{onrendered:function(canvas){data=canvas.toDataURL("image/png")}});
</script>
</html>