<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<meta charset="utf-8">
<link href="/download.css?${time}" rel="stylesheet" type="text/css"/>
<body>
<main class="skill" style="background:url('/icons/skillicon${skill.sprite}.png?${time}') ${skill.spriteXY};"></main>
</body>
<script src="/download.js?${time}"></script>
<script type="text/javascript">var data=null;html2canvas(document.getElementsByTagName('main')[0],{onrendered:function(canvas){data=canvas.toDataURL("image/png")}});</script>
</html>