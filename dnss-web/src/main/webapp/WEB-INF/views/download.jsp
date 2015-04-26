<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><fmt:setBundle basename="dnss" var="dnss"/>
<html lang="en">
<meta charset="utf-8">
<link href="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.css" rel="stylesheet" type="text/css"/>
<body>
<main>
	<section><c:forEach items="${jobs.iterator}" var="job" varStatus="jobLoop"><c:if test="${not empty job}">
		<table class="skill-tree no-select" id="skill-tree-${jobLoop.index}"><c:forEach items="${job.skillTree}" var="skillRow" varStatus="skillRowLoop">
			<tr><c:forEach items="${skillRow}" var="skill" varStatus="skillLoop"><c:choose><c:when test="${empty skill}">
				<td class="container" /></c:when><c:otherwise>
				<td class="container">
					<div class="skill" style="background:url('/skillicons/<fmt:message key="skillicon.version" bundle="${dnss}"/>_skillicon${skill.sprite}.png') ${skill.spriteXY};"/>
					<div class="lvl">${skill.level}/${skill.maxLevel}</div></c:otherwise></c:choose></c:forEach></c:forEach>
		</table></c:if></c:forEach>
	</section>
</main>
<script src="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.js"></script>
<script type="text/javascript">
<!-- THIS WORKS LOL
html2canvas($("#job-sp"), {
onrendered: function(canvas) {
var dl = document.createElement("a");
dl.href = canvas.toDataURL('image/png');
dl.download = 'test.png';
document.body.appendChild(dl);
dl.click();
document.body.removeChild(dl);
}
});-->
</script>
</body>
</html>
