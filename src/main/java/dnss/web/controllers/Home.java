package dnss.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class Home {
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}