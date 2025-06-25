
  function fetchTests(params) {
    return axios.get(`/api/tests`, { params: params });
  }

  function fetchAdminCourses(isInProgress) {
    return axios.get(`/api/admin/courses`, {params: {isInProgress: isInProgress}})
  }

  function fetchUserCourses() {
    return axios.get(`/api/courses`)
  }



