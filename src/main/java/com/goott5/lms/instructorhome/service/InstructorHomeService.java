package com.goott5.lms.instructorhome.service;

import com.goott5.lms.instructorhome.domain.HomeResponseDTO;

public interface InstructorHomeService {

  HomeResponseDTO getHomeData(int courseId);
}
