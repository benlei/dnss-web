<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><fmt:setBundle basename="dnss" var="dnss"/>
<html lang="en"><c:set var="i" value="0"/>
<body>
<main>
</main>
</body>
</html>
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
