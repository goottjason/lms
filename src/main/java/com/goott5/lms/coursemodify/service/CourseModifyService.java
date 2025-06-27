package com.goott5.lms.coursemodify.service;

import com.goott5.lms.coursemodify.domain.CourseModifyDTO;
import com.goott5.lms.coursemodify.domain.CourseResponseDTO;

public interface CourseModifyService {

  CourseResponseDTO getDetailByCourseId(Integer courseId);

  boolean modifyCourse(CourseModifyDTO courseModifyDTO);
}
