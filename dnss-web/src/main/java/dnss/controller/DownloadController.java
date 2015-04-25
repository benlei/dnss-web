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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import static dnss.enums.Advancement.PRIMARY;
import static dnss.enums.Advancement.SECONDARY;
import static dnss.enums.Advancement.TERTIARY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@Controller
@RequestMapping("/download")
public class DownloadController {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SP sp;

    @RequestMapping("/{alignment:[h|v]}/{identifier:[a-z,]+}/{level:[1-9][0-9]*}/{build:[0-9a-zA-Z-\\.\\+]{48,}}")
    public String download(HttpServletRequest request, HttpServletResponse response,
                           @PathVariable("identifier") String identifier,
                           @PathVariable("level") int level,
                           @PathVariable("build") String build,
                           @PathVariable("alignment") String alignment,
                           ModelMap model) throws Exception {
        String[] identifiers = identifier.split(",");
        if (identifier.length() != 3) { // will do next release
            response.sendError(SC_NOT_FOUND, "Not supported");
        }

        ArrayList<Job> list = new ArrayList<Job>();
        // first make sure they are all valid jobs
        for (String s : identifiers) {
            if (!context.containsBean("job_" + s)) {
                response.sendError(SC_NOT_FOUND, "No job '"+s+"'");
            }

            list.add((Job)context.getBean("job_" + s));
        }

        Jobs jobs = sortJobList(list);


        if (jobs == null) {
            response.sendError(SC_NOT_FOUND, "Invalid job list");
        }

        // just any tertiary jobs
        Job tertiary = (Job)context.getBean("job_gladiator");

        // set the level+sp cap
        jobs.setLevel(level);
        jobs.setMaxSP(sp.forCap(level));

        model.addAttribute("alignment", alignment);
        return "download";
    }


    private Jobs sortJobList(ArrayList<Job> list) {
        Jobs jobs = new Jobs();

        for (Job j : list) {
            if (jobs.getJob(j.getAdvancement()) == null) {
                jobs.setJob(j);
            } else {
                return null;
            }
        }

        return jobs;
    }

}
