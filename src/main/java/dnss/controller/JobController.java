package dnss.controller;

import dnss.DragonNest;
import dnss.model.Job;
import dnss.model.Jobs;
import dnss.model.SP;
import dnss.web.Cookies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Random;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@Controller
@RequestMapping("/")
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

    private final static long TIME = System.currentTimeMillis() / 1000;

    @RequestMapping("/job/{identifier:[a-z]+}-{level:[1-9][0-9]*}")
    public String job(@PathVariable("identifier") String identifier,
                      @PathVariable("level") int level,
                      @ModelAttribute("cookies") Cookies cookies,
                      HttpServletResponse response,
                      ModelMap model) throws Exception {
        String bean = "jobs_"  +identifier;
        if (! context.containsBean(bean)) {
            response.sendError(SC_NOT_FOUND, "No tertiary job '" + identifier + "'");
        }

        Jobs jobs = (Jobs)context.getBean(bean);

        jobs.setLevel(level);
        jobs.setMaxSP(sp.forCap(level));

        // for prefixing stuff
        model.addAttribute("time", DragonNest.getVersion());

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
        if (! cookies.contains("mru_job") || ! identifier.equals(cookies.get("mru_job").getValue())) {
            cookies.create("mru_job", identifier);
        }

        if (! cookies.contains("mru_level") || ! String.valueOf(level).equals(cookies.get("mru_level").getValue())) {
            // lives for 30 minutes
            cookies.create("mru_level", level, 1800);
        }

        return "home";
    }

    @RequestMapping("/job/{identifier:[a-z]+}")
    public String job(@PathVariable("identifier") String identifier,
                      @ModelAttribute("cookies") Cookies cookies,
                      HttpServletResponse response,
                      ModelMap model) throws Exception {
        int cap = sp.getLatestCap();
        if (cookies.contains("mru_level")) {
            try {
                int c = Integer.parseInt(cookies.get("mru_level").getValue());
                if (c > 0 && c <= cap) {
                    cap = c;
                }
            } catch (Exception ignorable) {
            }
        }

        return job(identifier, cap, cookies, response, model);
    }

    @RequestMapping("/")
    public String job(@ModelAttribute("cookies") Cookies cookies,
                      HttpServletResponse response,
                      ModelMap model) throws Exception {
        String identifier = null;
        int cap = sp.getLatestCap();

        if (cookies.contains("mru_job")) {
            String bean = "jobs_"  + cookies.get("mru_job").getValue();
            if (context.containsBean(bean)) {
                identifier = cookies.get("mru_job").getValue();
            }
        }

        if (cookies.contains("mru_level")) {
            try {
                int c = Integer.parseInt(cookies.get("mru_level").getValue());
                if (c > 0 && c <= sp.getLatestCap()) {
                    cap = c;
                }
            } catch (Exception ignorable) {
            }
        }

        if (identifier == null) {
            identifier = tertiaries.get(tertiaries.size() - 1).getIdentifier();
        }

        return job(identifier, cap, cookies, response, model);
    }

    @ModelAttribute("cookies")
    private Cookies getCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookies cookies = new Cookies(request, response);
        Cookie[] c = request.getCookies();
        if (c == null) {
            return cookies;
        }

        for (Cookie cookie : c) {
            cookies.set(cookie.getName(), cookie);
        }

        return cookies;
    }
}