<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<title>DNSS &ndash; ${tertiary.name}</title>
<meta charset="utf-8">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
<link href="/css/main.css" rel="stylesheet" type="text/css" />
<style>
#primary {background-image: url(/images/${primary.identifier}.png)}
</style>

<body>
<header>
  <h1>Dragon Nest &ndash; Skill Simulator</h1>
</header>
<main>
  <table>
    <tr>
      <td id="class" class="meta">
        <ul>
          <li class="active">
            ${primary.name}
            <div class="right">0/${max_sp_1}</div>
          </li>
          <li>
            ${secondary.name}
            <div class="right">0/${max_sp_2}</div>
          </li>
          <li>
            ${tertiary.name}
            <div class="right">0/${max_sp_3}</div>
          </li>
          <li>
            Total SP
            <div class="right">0/${max_sp_total}</div>
          </li>
        </ul>
      <td id="skill-set" rowspan="2">
        <table id="primary">
        	<c:forEach items="${primary.skillTree}" var="row">
        	<tr>
        	  <c:forEach items="${row}" var="skill">
              <c:choose>
                <c:when test="${skill != 0}">
                <td>
                  <div class="skill" data-id="${skill}">
                    <div class="skillmod">
                      <div class="level">1/1</div>
                      <div class="mod">
                        <div class="plus">+</div>
                        <div class="minus">-</div>
                      </div>
                    </div>
                    <div class="skillicon"/>
                  </div>
                </c:when>
                <c:otherwise>
                <td>
                </c:otherwise>
              </c:choose>
        	  </c:forEach>
        	</c:forEach>
        </table>
      <td id="skill-description" rowspan="2">Hello
    <tr>
      <td id="configurable" class="meta">
        <ul>
          <li>
            Build URL
            <form class="right">
              <input type="button" value="Copy" />
            </form>
          </li>
          <li>
            Mode
            <form class="right">
              <select class="right">
                <option value="PvE" selected>PvE</option>
                <option value="PvP">PvP</option>
              </select>
            </form>
          </li>
          <li>
            Level
            <form class="right">
              <select lass="right">
                <option value="80" selected>80</option>
                <option value="75">75</option>
                <option value="70">70</option>
                <option value="65">65</option>
                <option value="60">60</option>
                <option value="55">55</option>
                <option value="50">50</option>
                <option value="45">45</option>
                <option value="40">40</option>
                <option value="32">32</option>
                <option value="24">24</option>
              </select>
            </form>
          </li>
        </ul>
  </table>
</main>

<footer>
  Help
</footer>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script type="text/javascript">
$.getJSON("/json/${primary.identifier}.json", function(json) {
  $('#primary .skill').each(function() {
    var skillid = $(this).data('id');
    var icon = $(this).find('.skillicon').first();
    var frame = 'url(/images/uit_skillslotbutton.png) 0 0 no-repeat';
    var image = 'url(/images/skillicon' + json['skills'][skillid]['image'] + '_b.png) ' + ((json['skills'][skillid]['icon'] % 10) * -50) + 'px ' + (Math.floor(json['skills'][skillid]['icon'] / 10) * -50) + 'px';
    icon.css('background', frame+','+image );
    icon.addClass('grayscale');

  });
});
</script>
</body>
</html>