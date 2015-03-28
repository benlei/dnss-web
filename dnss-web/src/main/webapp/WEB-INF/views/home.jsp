<!DOCTYPE html><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html lang="en">
<title>${jobs[fn:length(jobs) - 1].name} - DNSS</title>
<meta charset="utf-8">
<link rel="shortcut icon" type="image/x-icon" href="/favicon.ico">
<link href="/css/main.css" rel="stylesheet" type="text/css" />
<body>
<main>
  <nav id="jobs">
    <ul><c:forEach items="${job0}" var="primary" varStatus="pLoop">
      <li class="primary">${primary.name}
        <ul class="sub-jobs"><c:forEach items="${job1}" var="secondary" varStatus="sLoop"><c:if test="${secondary.parent == primary}">
          <li class="secondary">${secondary.name}</li><c:forEach items="${job2}" var="tertiary" varStatus="tLoop"><c:if test="${tertiary.parent == secondary}">
          <li class="tertiary"><a href="/job/${tertiary.identifier}">${tertiary.name}</a></li></c:if></c:forEach>
        </c:if></c:forEach></ul>
      </li></c:forEach>
    </ul>
  </nav>
  <aside id="builder" data-base="http://dnss.herokuapp.com/job/${jobs[fn:length(jobs) - 1].identifier}"></aside>
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
        <td class="skill-container"></c:otherwise></c:choose></c:forEach></c:forEach>
    </table></c:forEach>
  </section>
  <aside id="sidebar-2">
    <input type="button" id="mode" value="pve" /><h2 id="skill-name">Magma Monument</h2>
    <div class="skill-description">
      <ul class="meta">
        <li id="skill-level"><span class="y">Skill Lv.: </span><span class="w"></span></li>
        <li id="skill-sp"><span class="y">SP Consumed: </span><span class="w"></span></li>
        <li id="skill-mpcost"><span class="y">Fee MP: </span><span class="w"></span> of base MP</li>
        <li id="skill-cd"><span class="y">Cooldown: </span><span class="w"></span> sec</li>
        <li id="skill-required-level"><span class="y">Level Limit: </span><span class="w"></span></li>
        <li id="skill-required-weapon"><span class="y">Required Weapon: </span><span class="w"></span></li>
        <li id="skill-type"><span class="y">Skill Type: </span><span class="w"></span></li>
        <li id="skill-description"><span class="y">Skill Description:</span><div class="description"></div></li>
        <li id="next-description"><span class="y">Next Description:</span><div class="description"></div></li>
      </ul>
    </div>
  </aside>
</main>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
<script type="text/javascript">
function generateBuild() {
  var build = [];
  var map = ['-'];
  for (var i = 0; i < 10; i++) {
    map.push(i);
  }

  for (var i = 65; i < 91; i++) {
    map.push(String.fromCharCode(i));
  }

  for (var i = 97; i < 123; i++) {
    map.push(String.fromCharCode(i));
  }

  $('.skill-container').each(function(i) {
    build[i] = $(this).find('.skill').length ? map[($(this).find('.skill').data('permanent') ? -1 : 0) + getLevel($(this).find('.skill')).current] : map[0];
  });

  var link = $('#builder').data('base') + '?' + build.join('')
  if ($('#mode').val() == 'pvp') {
    link = link + map[0];
  }

  $('#builder').html('<strong>Build URL:</strong> <a href="' + link + '">' + link + '</a>');
}

// lazy rebuild
function rebuildSimulation(str) {
  if (str.length < 24*3) { // not good
    return;
  }

  var map = {'-': 0}, k = 1;
  for (var i = 0; i < 10; i++) {
    map[i] = k++;
  }

  for (var i = 65; i < 91; i++) {
    map[String.fromCharCode(i)] = k++;
  }

  for (var i = 97; i < 123; i++) {
    map[String.fromCharCode(i)] = k++;
  }

  var other;
  if (str.length > 24*3) {
    other = str.substr(24*3);
    str = str.substr(0,24*3);
  }

  if (other && map[other.charAt(0)] == 0) {
    $('#mode').val('pvp');
  }

  $('.skill-container').each(function(i) {
    var curr = map[str.charAt(i)];
    for (var j = 0; j < curr; j++) {
      incSkill($(this).find('.skill'));
    }
  });
}

function formatDescription(str, params) {
  if (str) {
    for (i in params) {
      while (str.indexOf('{' + i + '}') != -1) {
        str = str.replace('{' + i + '}', params[i]);
      }
    }

    var y = 0, w = 0, p = 0, newStr = '', startPos = 0;
    for (var i = 0; i < str.length - 1; i++) {
      switch (str.substr(i, 2)) {
        case '#y':
        case '#p':
          if (y - w == 1) { // needed a closing </span>
            newStr += str.substring(startPos, i) + '</span><span class="' + str.substr(i+1,1) + '">';
          } else {
            newStr += str.substring(startPos, i) + '<span class="' + str.substr(i+1,1) + '">';
            y++;
          }
          
          startPos = i + 2;
          ++i;
          break;
        case '#w':
          if (w == y) { // early #w
            newStr +=  str.substring(startPos, i);
          } else {
            newStr += str.substring(startPos, i) + '</span>';
            w++;
          }

          startPos = i + 2;
          ++i;
          break;
        default:
          break;
      }
    }

    newStr = newStr + str.substring(startPos);

    if (y != w) {
      newStr = newStr + '</span>';
    }


    newStr = newStr.replace(/\\n/g, '<br />');

    return newStr;
  } else {
    return '';
  }
}

var lastSkillDesc;
function setDescription($skill) { // also sets warnings!
  lastSkillDesc = $skill;
  var job = jobs[$skill.data('job')], skills = job.skills, skill = skills[$skill.data('id')];
  var lvl = getLevel($skill);
  var skillLevel, skillSP, skillMP, skillCD, skillRequiredLevel, skillRequiredWeapon, skillDescription, nextDescription;

  var prefix = '';
  if ($('#mode').val() == 'pvp') {
    prefix = 'pvp_';
  }
  if (! lvl.current) {
    skillLevel = 1;
    skillCD = skill.levels[0].cd / 1000;
    skillSP = skill.levels[0].spcost;
    skillRequiredLevel = skill.levels[0].required_level;
    skillDescription = formatDescription(job.message[skill.levels[0][prefix+'explanationid']], skill.levels[0][prefix+'explanationparams']);
    skillMP = (skill.levels[0][prefix+'mpcost'] / 10.0) + '%';
  } else if (lvl.current == skill.levels.length) {
    skillLevel = lvl.current;
    skillCD = skill.levels[lvl.current - 1].cd / 1000;
    skillRequiredLevel = skill.levels[lvl.current - 1].required_level;
    skillDescription = formatDescription(job.message[skill.levels[lvl.current - 1][prefix+'explanationid']], skill.levels[lvl.current - 1][prefix+'explanationparams']);
    skillMP = (skill.levels[lvl.current - 1][prefix+'mpcost'] / 10.0) + '%';
    skillSP = skill.levels[lvl.current - 1].spcost;
  } else {
    skillLevel = lvl.current + ' &rarr; ' + (lvl.current+1);
    if (skill.levels[lvl.current - 1].cd == skill.levels[lvl.current].cd) {
      skillCD = skill.levels[lvl.current - 1].cd / 1000;
    } else {
      skillCD = (skill.levels[lvl.current - 1].cd / 1000) + ' &rarr; ' + (skill.levels[lvl.current].cd / 1000);
    }

    skillRequiredLevel = skill.levels[lvl.current - 1].required_level + ' &rarr; ' + skill.levels[lvl.current].required_level;
    skillDescription = formatDescription(job.message[skill.levels[lvl.current - 1][prefix+'explanationid']], skill.levels[lvl.current - 1][prefix+'explanationparams']);
    nextDescription = formatDescription(job.message[skill.levels[lvl.current][prefix+'explanationid']], skill.levels[lvl.current][prefix+'explanationparams']);
    skillSP = skill.levels[lvl.current].spcost;
    skillMP = (skill.levels[lvl.current - 1][prefix+'mpcost']/10.0) + '% &rarr; ' + (skill.levels[lvl.current][prefix+'mpcost']/10.0) + '%';
  }

  skillRequiredWeapon = [];
  for (i in skill.needweapon) {
    skillRequiredWeapon[i] = weapon_types[skill.needweapon[i]];
  }

  $('#skill-name').html(job.message[skill.nameid]);
  $('#skill-level .w').html(skillLevel);
  $('#skill-cd .w').html(skillCD);
  $('#skill-required-level .w').html(skillRequiredLevel);
  $('#skill-required-weapon .w').html(skillRequiredWeapon.join(', '));
  $('#skill-type .w').html(skill_types[skill.type]);
  $('#skill-mpcost .w').html(skillMP);
  $('#skill-description .description').html(skillDescription);
  $('#next-description .description').html(nextDescription);
  $('#skill-sp .w').html(skillSP);

  // post-change adjustments
  if (skillCD) {
    $('#skill-cd').show();
  } else {
    $('#skill-cd').hide();
  }

  if (skillRequiredWeapon.length) {
    $('#skill-required-weapon').show();
  } else {
    $('#skill-required-weapon').hide();
  }

  if (skillDescription) {
    $('#skill-description').show();
  } else {
    $('#skill-description').hide();
  }

  if (nextDescription) {
    $('#next-description').show();
  } else {
    $('#next-description').hide();
  }
}

function getLevel($skill) {
  var lvl = $skill.children().text().split('/');
  lvl = {'current': parseInt(lvl[0]), 'max': parseInt(lvl[1])};
  return lvl;
}

function setActive($skill, perm) {
  if (perm) {
    $skill.data('permanent', 1);
    incSkill($skill, 0);
    return;
  }

  var bg = $skill.css('background-image');
  if (bg.indexOf('_b.png') != -1) { // remove it
    $skill.css('background-image', bg.replace('_b.png', '.png'));
  }
}

function setInactive($skill) {
  if ($skill.data('perm')) return; // it's permanent
  var bg = $skill.css('background-image');
  if (bg.indexOf('_b.png') == -1) { // remove it
    $skill.css('background-image', bg.replace('.png', '_b.png'));
  }
}

function incSkill($skill, max) {
  setActive($skill);
  var lvl = $skill.children(), levels = jobs[$skill.data('job')].skills[$skill.data('id')].levels;
  var l = lvl.text().split('/');
  l = {'current': parseInt(l[0]), 'max': parseInt(l[1])};
  if (l.current == levels.length) {
    return;
  }

  var start = l.current;
  if (max && l.current <= l.max) { // max
    l.current = l.max - 1;
  }

  lvl.text((l.current+1) + "/" + l.max);

  if (l.current < l.max)  {
    var change = 0;
    for (var i = start; i < l.current + 1; i++) {
      change += levels[i].spcost;
    }

    alterJobSP($skill.data('job'), change);
  }

  setDescription($skill);
}

function decSkill($skill, min) {
  var lvl = $skill.children();
  var l = lvl.text().split('/');
  l = {'current': parseInt(l[0]), 'max': parseInt(l[1])};
  if (l.current == 0 || ($skill.data('permanent') && l.current == 1)) {
    return;
  }

  var start = l.current;
  if (min) { // min
    l.current = 1;
  }

  if ($skill.data('permanent') && l.current == 1) {
    l.current = 2;
  }

  if (l.current == 1) {
    setInactive($skill);
  }

  lvl.text((l.current-1) + "/" + l.max);

  var change = 0, levels = jobs[$skill.data('job')].skills[$skill.data('id')].levels;
  for (var i = l.current - 1; i < Math.min(start, l.max); i++) {
    change -= levels[i].spcost;
  }

  alterJobSP($skill.data('job'), change);
  setDescription($skill);
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

  generateBuild();
}

$.ajaxSetup({async: false}); // change out of this later

var jobIds = [<c:forEach items="${jobs}" var="job" varStatus="loop">'${job.identifier}'<c:if test="${!loop.last}">,</c:if></c:forEach>];
var jobs = {}, build = {};
var weapon_types, skill_types;

$.getJSON('/json/weapon_types.min.json', function(json) {weapon_types=json;});
$.getJSON('/json/skill_types.min.json', function(json) {skill_types=json;});
$.each(jobIds, function(i, jobId){
  $.getJSON('/json/' + jobId + '.min.json', function(job) {
    var max = i == 1 ? 70 : 80;
    for (s in job.skills) {
        for (l in job.skills[s].levels) {
          if (job.skills[s].levels[l].required_level <= max) {
            job.skills[s].maxlevel = parseInt(l) + 1;
          }
        }
    }
    jobs[i] = job;
    $('.skill').each(function() {
      var skill = job.skills[$(this).data('id')];
      if (skill) {
        var image = skill.image;
        var xpos = (skill.icon % 10) * -50;
        var ypos = Math.floor(skill.icon / 10) * -50;
        $(this).data('job', i);
        $(this).css('background', 'url(/images/skillicon' + image + '_b.png) ' + xpos + 'px ' + ypos + 'px');
        $(this).children().text('0/' + skill.maxlevel);
        $(this).click(function(e){ incSkill($(this), e.ctrlKey|e.shiftKey); });
        this.oncontextmenu = function() {return false;};
        $(this).mousedown(function(e) { if (e.button == 2) {decSkill($(this), e.ctrlKey||e.shiftKey);  return false;}});
        $(this).hover(function() {setDescription($(this));});
      }
    });

    if (job.default_skills) {
      $.each(job.default_skills, function(k, skill) { setActive($('.skill[data-id=' + skill + ']'), 1); });
    }
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

$('#mode').click(function() {
  $(this).val($(this).val() == 'pve' ? 'pvp' : 'pve');
  generateBuild();
  setDescription(lastSkillDesc);
});

if (window.location.search && window.location.search.substr(1)) {
  var skills = window.location.search.substr(1);
  if (skills) {
    rebuildSimulation(skills);
  }
}
</script>
</body>
</html>