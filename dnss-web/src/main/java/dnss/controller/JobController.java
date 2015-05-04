package dnss.controller;

import dnss.model.Job;
import dnss.model.Jobs;
import dnss.model.SP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Random;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@Controller
public class JobController {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SP sp;

    @Resource(name="all_jobs_primary")
    private List<Job> primaries; // singleton, do not alter

    @Resource(name="all_jobs_secondary")
    private List<Job> secondaries; // singleton, do not alter

    @Resource(name="all_jobs_tertiary")
    private List<Job> tertiaries; // singleton, do not alter

    @Resource(name="skill_types")
    private List<String> skillTypes; // singleton, do not alter


    @RequestMapping("/job/{identifier:[a-z]+}-{level:[1-9][0-9]*}")
    public String job(HttpServletRequest request, HttpServletResponse response,
                      @PathVariable("identifier") String identifier,
                      @PathVariable("level") int level,
                      ModelMap model) throws Exception {
        String bean = "jobs_"  +identifier;
        if (! context.containsBean(bean)) {
            response.sendError(SC_NOT_FOUND, "No tertiary job '" + identifier + "'");
        }

        Jobs jobs = (Jobs)context.getBean(bean);

        jobs.setLevel(level);
        jobs.setMaxSP(sp.forCap(level));

        // the jobs
        model.addAttribute("jobs", jobs);

        // the list of all the primary/secondary/tertiary jobs
        model.addAttribute("primaries", primaries);
        model.addAttribute("secondaries", secondaries);
        model.addAttribute("tertiaries", tertiaries);

        // the path to this job simulator
        model.addAttribute("path", jobs.getTertiary().getIdentifier() + "-" + level);

        // the skill + weapon types
        model.addAttribute("skill_types", skillTypes);
        model.addAttribute("weapon_types", context.getBean(jobs.getPrimary().getIdentifier() + "_weapons"));

        // sets the most recent job
        Cookie cookie = new Cookie("mru_job", identifier);
        cookie.setMaxAge(31556926);
        cookie.setPath("/");
        response.addCookie(cookie);

        cookie = new Cookie("mru_level", Integer.toString(level));
        cookie.setMaxAge(31556926);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "home";
    }

    @RequestMapping("/job/{identifier:[a-z]+}")
    public String job(HttpServletRequest request, HttpServletResponse response,
                      @PathVariable("identifier") String identifier,
                      ModelMap model) throws Exception {
        int cap = sp.getLatestCap();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("mru_level")) {
                    try {
                        int c = Integer.parseInt(cookie.getValue());
                        if (c > 0 && c <= cap) {
                            cap = c;
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        return job(request, response, identifier, cap, model);
    }

    @RequestMapping("/")
    public String job(HttpServletRequest request, HttpServletResponse response,
                      ModelMap model) throws Exception {
        String identifier = null;
        int cap = -1;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("mru_job")) {
                    String bean = "jobs_"  +cookie.getValue();
                    if (context.containsBean(bean)) {
                        identifier = cookie.getValue();
                    }
                } else if (cookie.getName().equals("mru_level")) {
                    try {
                        int c = Integer.parseInt(cookie.getValue());
                        if (c > 0 && c <= sp.getLatestCap()) {
                            cap = c;
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        if (identifier == null || cap == -1) {
            Random random = new Random();
            identifier = tertiaries.get(random.nextInt(tertiaries.size())).getIdentifier();
            cap = sp.getLatestCap();
        }

        return job(request, response, identifier, cap, model);
    }
}