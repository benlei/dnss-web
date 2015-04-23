package dnss.controller;

import dnss.model.Job;
import dnss.model.Jobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class JobController {
    @Autowired
    private WebApplicationContext context;

    @RequestMapping("/job/{job_identifier:[a-z]+}-{level:[1-9][0-9]*}")
    public String job(HttpServletResponse response,
                      @PathVariable("job_identifier") String jobIdentifier,
                      @PathVariable("level") int level,
                      ModelMap model) throws IOException {
        String bean = "jobs_"  +jobIdentifier;
        if (! context.containsBean(bean)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No job '" + jobIdentifier + "'");
        }

        ArrayList<Integer> levels = (ArrayList<Integer>)context.getBean("levels");
        if (level < 1 || level > levels.size() - 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No level '" + level + "' entry found");
        }

        int maxSP = levels.get(level);

        Jobs jobs = (Jobs)context.getBean(bean);
        float[] spRatios = jobs.getTertiary().getSpRatio();
        for (Job j : jobs) {
            j.setMaxSP((int)(maxSP * spRatios[j.getAdvancement().toInt()]));
        }

        // the jobs
        model.addAttribute("jobs", jobs);

        // the total usable SP
        model.addAttribute("max_sp", maxSP);

        // the list of all the primary/secondary/tertiary jobs
        model.addAttribute("primaries", context.getBean("all_jobs_primary"));
        model.addAttribute("secondaries", context.getBean("all_jobs_secondary"));
        model.addAttribute("tertiaries", context.getBean("all_jobs_tertiary"));

        // the path to this job simulator
        if (level == levels.size() - 1) {
            model.addAttribute("path", jobs.getTertiary().getIdentifier());
        } else {
            model.addAttribute("path", jobs.getTertiary().getIdentifier() + "-" + level);
        }


        // the job max level list
        int[] levelList = new int[] {Math.min(levels.size() - 1, level), Math.min(levels.size() - 11, level), Math.min(levels.size() - 1, level)};
        model.addAttribute("levels", levelList);

        // the skill + weapon types
        ArrayList<Integer> skillTypes = (ArrayList<Integer>)context.getBean("skill_types");
        HashMap<String, String> weapTypes = (HashMap<String,String>)context.getBean(jobs.getPrimary().getIdentifier() + "_weapons");
        model.addAttribute("skill_types", skillTypes);
        model.addAttribute("weapon_types", weapTypes);

        return "home";
    }

    @RequestMapping("/job/{job_identifier:[a-z]+}")
    public String job(HttpServletResponse response,
                        @PathVariable("job_identifier") String jobIdentifier,
                        ModelMap model) throws IOException {
        return job(response, jobIdentifier, 80, model);
    }

    @RequestMapping("/")
    public String job(HttpServletResponse response,
                      ModelMap model) throws IOException {
        return job(response, "moonlord", 80, model);
    }
}