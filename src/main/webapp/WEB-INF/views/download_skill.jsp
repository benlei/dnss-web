<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><fmt:setBundle basename="dnss" var="dnss"/>
<html lang="en">
<meta charset="utf-8">
<link href="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.css" rel="stylesheet" type="text/css"/>
<body>
<main class="skill" style="background:url('/skillicons/<fmt:message key="skillicon.version" bundle="${dnss}"/>_skillicon${skill.sprite}.png') ${skill.spriteXY};"></main>
</body>
<script src="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.js"></script>
<script type="text/javascript">var data=null;html2canvas(document.getElementsByTagName('main')[0],{onrendered:function(canvas){data=canvas.toDataURL("image/png")}});</script>
</html>