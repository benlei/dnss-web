package dnss.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
public class HomeController {
    @Autowired
    private WebApplicationContext context;

    @RequestMapping("/")
    public String hello(Model model) {
//        System.out.println(context.containsBean("warrior"));
//        System.out.println(context.containsBean("warriorz"));
        return "home";
    }
}