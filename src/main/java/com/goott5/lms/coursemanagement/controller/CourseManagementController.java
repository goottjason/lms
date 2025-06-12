package com.goott5.lms.coursemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CourseManagementController {


    // 과정 - 과정관리
    @GetMapping("/courseList")
    public String courseList() {
        return "/courseManagement/courseList";
    }
    @GetMapping("/courseRegister")
    public String courseRegister() {
        return "/courseManagement/courseRegister";
    }
    @GetMapping("/courseDetail")
    public String courseDetail() {
        return "/courseManagement/courseDetail";
    }
    @GetMapping("/courseModify")
    public String courseModify() {
        return "/courseManagement/courseModify";
    }
    @GetMapping("/learnerAssignment")
    public String learnerAssignment() {
        return "/courseManagement/learnerAssignment";
    }



}
