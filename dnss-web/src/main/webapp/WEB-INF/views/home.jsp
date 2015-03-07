<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="en">
<title>HTML5 Skeleton</title>
<meta charset="utf-8">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
<style>
* {
  padding: 0;
  margin: 0;
}

body {
  font-family: Verdana, sans-serif;
  font-size:0.9em;
  background: #fff;
}

header,main,footer {
  width: 1075px;
  margin-left: auto;
  margin-right: auto;
  margin-bottom: 10px;
}

table {
  border-collapse: collapse;
}

ul {
  list-style-type: none;
  padding: 0px;
  margin: 0px;
}

.meta, #skill-description {
  width: 250px;
}

#skill-set {
  width: 575px;
  background: rgb(16, 14, 15);
  border-left: 1px solid rgb(80, 80, 68);
  border-right: 1px solid rgb(80, 80, 68);
}

#skill-set .skill {
  width: 105px;
  height: 60px;
  margin: 20px 15px;
  border: 1px solid rgb(80, 80, 68);
}

#skill-set td:first-child .skill {
  margin-left: 30px;
}

#skill-set td:last-child .skill {
  margin-right: 30px;
}

#class {
  vertical-align: top;
}

.meta li {
  display: block;
  background: rgb(16, 14, 15);
  color: rgb(140, 140, 140);
  padding: 10px 15px;
  border-bottom: 1px solid rgb(80, 80, 68);
}

.right {
  float: right;
}

#class li:last-child {
  color: rgb(190, 156, 67);
}

#class li.active {
  background: rgb(38, 41, 24);
  color: rgb(144, 203, 233);
}

#configurable input {
  text-align: right;
}

#configurable,#video {
  vertical-align: bottom;
}

#configurable select, #configurable input {
  width: 60px;
  text-align: center;
}

#skill-description {
  background: #fff;
  color: rgb(140, 140, 140);
}

footer {
  clear: both;
}
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
          <li>
            Dark Summoner
            <div class="right">XXX/YYY</div>
          </li>
          <li class="active">
            Sting Breezer
            <div class="right">XXX/YYY</div>
          </li>
          <li>
            Dark Avenger
            <div class="right">XX/YYY</div>
          </li>
          <li>
            Total SP
            <div class="right">XXX/YYY</div>
          </li>
        </ul>
      <td id="skill-set" rowspan="2">
        <table>
          <tr>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
          <tr>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
          <tr>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
          <tr>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
          <tr>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
          <tr>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
            <td><div class="skill"></div>
        </table>
      <td id="skill-description" rowspan="2">
<!--    <tr>
      <td id="video" class="meta">
        <ul>
          <li>
            <div style="width: 220px; height: 165px; background: #fff; margin-top: 5px;"></div>
            <div style="text-align: center; margin-top: 10px;">Skill Name</div>
          </li>
        </ul>-->
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

</body>
</html>