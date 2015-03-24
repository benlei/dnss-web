package dnss.controller;

import dnss.model.Job;
import dnss.enums.Advancement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/job")
public class JobController {
    @Autowired
    private WebApplicationContext context;

    @RequestMapping("/{job_identifier}")
    public String hello(HttpServletResponse response,
                        @PathVariable("job_identifier") String jobIdentifier,
                        ModelMap model) throws IOException {
        String bean = "job_"  +jobIdentifier;
        if (! context.containsBean(bean)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No job '" + jobIdentifier + "'");
        }

        Job tertiary = (Job)context.getBean(bean);
        if (! tertiary.getAdvancement().equals(Advancement.TERTIARY)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, tertiary.getName() + " is not a Tertiary Job");
        }

        Job secondary = tertiary.getParent();
        Job primary = secondary.getParent();

        model.addAttribute("primary", primary);
        model.addAttribute("secondary", secondary);
        model.addAttribute("tertiary", tertiary);

        // level & SP stuff
        List<Integer> levels = (List<Integer>)context.getBean("levels");
        int maxSP = levels.get(levels.size() - 1);
        model.addAttribute("max_sp_1", (int)(maxSP * tertiary.getSpRatio1()));
        model.addAttribute("max_sp_2", (int)(maxSP * tertiary.getSpRatio2()));
        model.addAttribute("max_sp_3", (int)(maxSP * tertiary.getSpRatio3()));
        model.addAttribute("max_sp_total", maxSP);


        return "home";
    }
}