function fetchUserCourses() {
  return axios.get(`/api/courses`);
}

function fetchAttendanceRateCourseProgressRate(courseName) {
  return axios.get(`/api/learner/attendance/progress`,
      { params: { courseName: courseName } });
}

function fetchTestHwScore(courseName) {
  return axios.get(`/api/learner/test/hw`,
      { params: { courseName: courseName } });
}

function fetchCourseSchedule(courseName) {
  return axios.get(`/api/learner/course/schedule`,
      { params: { courseName: courseName } });
}

function fetchTestHwSchedule(courseName) {
  return axios.get(`/api/learner/test/hw/schedule`,
      { params: { courseName: courseName } });
}

function fetchAttendanceStatus(courseName) {
  return axios.get(`/api/learner/attendance/status`,
      { params: { courseName: courseName } });
}

function fetchInquiry() {
  return axios.get(`/api/learner/inquiry`);
}

function fetchQnA() {
  return axios.get(`/api/learner/qna`);
}

function fetchNotice(courseName) {
  return axios.get(`/api/learner/notice`,
      { params: { courseName: courseName } });
}

function fetchForum(courseName) {
  return axios.get(`/api/learner/forum`,
      { params: { courseName: courseName } });
}

function fetchTestStatistic(courseName) {
  return axios.get(`/api/learner/test/statistic`,
      { params: { courseName: courseName } });
}