package com.goott5.lms.courseboardmaterials.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CourseBoardMaterialsController {

    @GetMapping("/materialsList")
    public String materialsList() {
        return "/courseBoardMaterials/materialsList";
    }
}
