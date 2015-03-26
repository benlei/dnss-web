<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<title>${jobs[2].name} - DNSS</title>
<meta charset="utf-8">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
<link href="/css/main.css" rel="stylesheet" type="text/css" />
<body>
<main>
  <aside id="sidebar-1">
    <ul id="job-list-sp"><c:forEach items="${jobs}" var="job" varStatus="loop">
      <li data-job="${loop.index}"<c:if test="${loop.first}"> class="active"</c:if>>${job.name}<div class="sp">0/${job.maxSP}</div></li></c:forEach>
      <li>Total SP<div class="sp">0/${max_sp}</div></li>
    </ul>
  </aside>
  <section><c:forEach items="${jobs}" var="job" varStatus="jobLoop">
    <table class="skill-tree" id="skill-tree-${jobLoop.index}"><c:forEach items="${job.skillTree}" var="skillRow" varStatus="skillRowLoop">
      <tr><c:forEach items="${skillRow}" var="skill" varStatus="skillLoop"><c:choose><c:when test="${skill != 0}">
        <td class="skill-container">
          <div class="skill" data-id="${skill}" />
          <div class="lvl">0/0</div></c:when><c:otherwise>
        <td  class="skill-container"></c:otherwise></c:choose></c:forEach></c:forEach>
    </table></c:forEach>
  </section>
  <aside id="sidebar-2">
    <h2>Magma Monument</h2>
    <div class="skill-description">
      <ul class="meta">
        <li id="skill-level"><span>Skill Lv.: </span>15 &rarr; 16</li>
        <li id="skill-mpcost"><span>Fee MP: </span>4.9% &rarr; 5.1% of base MP</li>
        <li id="skill-cd"><span>Cooldown: </span>25 &rarr; 20 sec</li>
        <li id="skill-required-level"><span>Level Limit: </span>43 &rarr; 49</li>
        <li id="skill-required-weapon"><span>Required Weapon: </span>Bubble Blaster</li>
        <li id="skill-type"><span>Skill Type: </span>Reactive Passive</li>
        <li id="skill-current-description"><span>Skill Description:</span>
          <div class="description">
            Happy happy
          </div>
        </li>
        <li  id="skill-next-description"><span>Next Description:</span>
          <div class="description">
            joy joy
          </div>
        </li>
      </ul>
    </div>
  </aside>
</main>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script type="text/javascript">
function setActive(skill, perm) {
  if (perm) {
    skill.data('permanent', 1);
    incSkill(skill, 0);
    return;
  }

  var bg = skill.css('background-image');
  if (bg.indexOf('_b.png') != -1) { // remove it
    skill.css('background-image', bg.replace('_b.png', '.png'));
  }
}

function setInactive(skill) {
  if (skill.data('perm')) return; // it's permanent
  var bg = skill.css('background-image');
  if (bg.indexOf('_b.png') == -1) { // remove it
    skill.css('background-image', bg.replace('.png', '_b.png'));
  }
}

function incSkill(skill, max) {
  setActive(skill);
  var lvl = skill.children();
  var l = lvl.text().split('/');
  l = {'current': parseInt(l[0]), 'max': parseInt(l[1])};
  if (l.current == l.max) {
    return;
  }

  var start = l.current;
  if (max) { // max
    l.current = l.max - 1;
  }

  lvl.text((l.current+1) + "/" + l.max);

  var change = 0, levels = skills[skill.data('id')].levels;
  for (var i = start; i < l.current + 1; i++) {
    change += levels[i].spcost;
  }

  alterJobSP(skill.data('job'), change);
}

function decSkill(skill, max) {
  var lvl = skill.children();
  var l = lvl.text().split('/');
  l = {'current': parseInt(l[0]), 'max': parseInt(l[1])};
  if (l.current == 0 || (skill.data('permanent') && l.current == 1)) {
    return;
  }

  var start = l.current;
  if (max) { // max
    l.current = 1;
  }

  if (skill.data('permanent') && l.current == 1) {
    l.current = 2;
  }

  if (l.current == 1) {
    setInactive(skill);
  }

  lvl.text((l.current-1) + "/" + l.max);

  var change = 0, levels = skills[skill.data('id')].levels;
  for (var i = l.current - 1; i < start; i++) {
    change -= levels[i].spcost;
  }

  alterJobSP(skill.data('job'), change);
}

function alterJobSP(idx, change) {
  var spJob = $('#job-list-sp li[data-job=' + idx + '] .sp:first');
  var totalSP = $('#job-list-sp li:last .sp');

  var l = spJob.text().split('/');
  l = {'current': parseInt(l[0]) + change, 'max': parseInt(l[1])};
  spJob.text(l.current + '/' + l.max);
  if (l.max < l.current) {
    spJob.css('color', 'red');
  } else {
    spJob.css('color', '');
  }

  l = totalSP.text().split('/');
  l = {'current': parseInt(l[0]) + change, 'max': parseInt(l[1])};
  totalSP.text(l.current + '/' + l.max);
  if (l.max < l.current) {
    totalSP.css('color', 'red');
  } else {
    totalSP.css('color', '');
  }
}

var jobIds = [<c:forEach items="${jobs}" var="job" varStatus="loop">'${job.identifier}'<c:if test="${!loop.last}">,</c:if></c:forEach>];
var jobs = {};
var skills = {};
$.each(jobIds, function(i, jobId){
  $.getJSON('/json/' + jobId + '.json', function(job) {
    jobs[i] = job;
    skills = $.extend({}, skills, job.skills);
    $('.skill[data-id]').each(function() {
      var skill = job.skills[$(this).data('id')];
      if (skill) {
        var image = skill.image;
        var xpos = (skill.icon % 10) * -50;
        var ypos = Math.floor(skill.icon / 10) * -50;
        $(this).data('job', i);
        $(this).css('background', 'url(/images/skillicon' + image + '_b.png) ' + xpos + 'px ' + ypos + 'px');
        $(this).children().text('0/' + skill.maxlevel);
        $(this).click(function(e){ incSkill($(this), e.ctrlKey); });
        this.oncontextmenu = function() {return false;};
        $(this).mousedown(function(e) { if (e.button == 2) {decSkill($(this), e.ctrlKey);  return false;}});
      }
    });

    if (job.default_skills) {
      $.each(job.default_skills, function(k, skill) { setActive($('.skill[data-id=' + skill + ']'), 1); });
    }

    if (!job.skills[4001]) return;
    // $('#skill-description').html('
    //   <h2>' + jobs[$('.skill[data-id=4001]').data('job')].message[skills[4001].nameid] + '</h2>
    //     <ul>
    //       <li></li>
    //     </ul>
    // ');
  });  
});

$('.skill-tree').hide();
$('#skill-tree-0').show();

$('#job-list-sp li[data-job]').each(function() {
  var idx = $(this).data('job');
  $(this).click(function() {
    $('#job-list-sp li[data-job][class="active"]').removeClass('active');
    $(this).addClass('active');
    $('.skill-tree').hide();
    $('#skill-tree-' + idx).show();
  })
});



</script>
</body>
</html>