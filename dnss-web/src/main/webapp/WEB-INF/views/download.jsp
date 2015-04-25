<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><fmt:setBundle basename="dnss" var="dnss"/>
<html lang="en">
<meta charset="utf-8">
<link href="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.css" rel="stylesheet" type="text/css"/>
<body>
<main>
</main>
<script src="/<fmt:message key="timestamp" bundle="${dnss}"/>-download.js"></script>
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
</body>
</html>
