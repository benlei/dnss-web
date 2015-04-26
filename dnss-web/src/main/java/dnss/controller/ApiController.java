package dnss.controller;

import dnss.model.Job;
import dnss.model.SP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SP sp;

    @RequestMapping(value = "/level/{level:[1-9][0-9]*}", method = RequestMethod.GET, produces="application/json")
    public @ResponseBody String getLevel(@PathVariable("level") int level) {
        // just any tertiary jobs
        Job tertiary = (Job)context.getBean("job_gladiator");
        float[] ratios = tertiary.getSpRatio();
        int maxSP = sp.forCap(level);
        int primarySP = (int)(maxSP * ratios[0]);
        int secondarySP = (int)(maxSP * ratios[1]);
        int tertiarySP = (int)(maxSP * ratios[2]);
        return "{\"sp\":[" + primarySP + "," + secondarySP + "," + tertiarySP + "," + maxSP + "]}";
    }
}
