package dnss.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@RequestMapping("/api/[1-9][0-9]*")
public class ImagesController {
    @Autowired
    private WebApplicationContext context;

}
