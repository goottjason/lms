function fetchQnA(qnaRequest) {
  return axios.get(`/api/qna`,
      { params: qnaRequest, header: { "Content-Type": "application/json" } });
}

function fetchAdminCourses(isInProgress) {
  return axios.get(`/api/admin/courses`, {params: {isInProgress: isInProgress}})
}

function fetchUserCourses() {
  return axios.get(`/api/courses`)
}