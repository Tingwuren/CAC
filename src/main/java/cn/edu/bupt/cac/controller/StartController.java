package cn.edu.bupt.cac.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class StartController {
    @RequestMapping("")
    public String start() {
        return "start";
    }

    @RequestMapping("/workspace")
    public String workspace() {
        return "workspace";
    }
}
