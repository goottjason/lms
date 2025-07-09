const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();
let isInProgress =  '1';
let includeAll = null;

const courseConfig = {
  pageNo: null,
  pageSize: null,
  type: null,
  keyword: null,
  orderBy: null,
  orderDirection: null,
};
const learnerConfig = {
  pageNo: null,
  pageSize: null,
  type: null,
  keyword: null,
  orderBy: null,
  orderDirection: null,
};


/* =================================================================================================== */

$(document).ready(() => {

  initCourseSelect();

  $('#course-select').on('change', handleCourseSelectChange);
  $('#search-button').on('click', handleSearchChange);
  $(document).on('keydown', '#search-input', function(e) {
    if (e.key == "Enter") {
      e.preventDefault();
      handleSearchChange();
    }
  });
  $(document).on('click', '.not-enrolled-add-button', handleAddEnrollment);
  $(document).on('click', '.enrolled-remove-button', handleRemoveEnrollment);
  $(document).on('change', '#include-all', handleIncludeAllCheckbox);

});

/* =================================================================================================== */

async function initCourseSelect() {

  let coursesWithPagination = await apiGetRequestAboutCourses(
    '/api/management/courses',
    {
      loginUserId: loginUserId,
      loginUserType: loginUserType,
      isInProgress: isInProgress});
  let courses = coursesWithPagination?.respDTOS || [];
  if (!Array.isArray(courses)) courses = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0); // 시간 제거
  // courses 배열 필터링
  const filteredCourses = courses.filter(course => {
    const startDate = new Date(course.startDate);
    startDate.setHours(0, 0, 0, 0); // 시간 제거
    // start_date가 오늘보다 하루 더 늦은 날짜(내일 이후)인지 확인
    return startDate > today;
  });
  if (filteredCourses.length < 1) {
    $('#course-select').append($('<option>').val('').text('배정할 과정이 없습니다.'));
    $('#course-select').prop('disabled', true);
  } else {
    updateSelectBox('#course-select', filteredCourses);
  }

  // 첫 번째 옵션을 선택 상태로 변경
  $("#course-select option:eq(0)").prop("selected", true);
  // 이벤트 핸들러 강제 실행
  $("#course-select").trigger("change");
}

async function getLearnersByEnrollment() {
  let courseId = $('#course-select').val();
  let enrolledLearners = await apiGetRequestAboutLearners(
    '/api/learners/enrolled',
    { courseId: courseId });
  enrolledLearners =
    Array.isArray(enrolledLearners) ? enrolledLearners : [enrolledLearners];

  let notEnrolledLearners = await apiGetRequestAboutLearners(
    '/api/learners/not-enrolled',
    {includeAll: includeAll });
  notEnrolledLearners =
    Array.isArray(notEnrolledLearners) ? notEnrolledLearners : [notEnrolledLearners];

  return { enrolledLearners, notEnrolledLearners };
}

async function apiGetRequestAboutCourses(endpoint, additionalParams = {}) {
  try {
    const response = await axios.get(endpoint, {
      params: { ...courseConfig, ...additionalParams }
    });
    return response.data.data;
  } catch (error) {
    return [];
  }
}

async function apiGetRequestAboutLearners(endpoint, additionalParams = {}) {
  try {
    const response = await axios.get(endpoint, {
      params: { ...learnerConfig, ...additionalParams }
    });
    return response.data;
  } catch (error) {
    return [];
  }
}

async function apiPostRequest(endpoint, payload = {}, additionalParams = {}) {
  try {
    const response = await axios.post(endpoint, payload);
    return response.data;
  } catch (error) {
    Swal.fire({
      icon: "error",
      title: "배정불가능!",
      text: "정원을 초과하였거나 배정할 과정이 없습니다.",
      footer: ''
    });
    return [];
  }
}

async function apiDeleteRequest(endpoint, additionalParams = {}) {
  try {
    const response = await axios.delete(endpoint, {
      params: { ...learnerConfig, ...additionalParams }
    });
    return response.data;
  } catch (error) {
    return [];
  }
}

function displayTables(enrolledLearners, notEnrolledLearners) {
  $('#enrolled-list').empty();
  if(enrolledLearners.length == 0) {
    $("#enrolled-list").html("<tr class='text-center'><td colspan='7'>데이터가 없습니다.</td></tr>");
  }
  enrolledLearners.forEach(function(learner) {
    let rowHtml = `
        <tr>
          <td class="text-center align-middle">
            ${
                learner.completionStatus == null ? '미배정' :
                  learner.completionStatus === 'IN_PROGRESS' ? '교육생' :
                    learner.completionStatus === 'COMPLETED' ? '수료생' :
                      learner.completionStatus === 'DROPPED' ? '중퇴생' :
                        learner.completionStatus
              }
          </td>
          <td class="text-center align-middle">${learner.fullname}</td>
          <td class="text-center align-middle">${learner.mobile ? learner.mobile : '-'}</td>
          <td class="text-center align-middle">
            <button class="btn btn-danger btn-icon-split btn-sm enrolled-remove-button" data-id="${learner.id}">
              <span class="text">삭제</span>
            </button>
          </td>
        </tr>
      `;
    $('#enrolled-list').append(rowHtml);
  });

  $('#not-enrolled-list').empty();
  if (notEnrolledLearners.length == 0) {
    let rowHtml = `
      <tr>
        <td colspan="4" class="text-center align-middle">
          데이터가 없습니다.
        </td>
      </tr>
    `;
    $('#not-enrolled-list').append(rowHtml);
  }
  else {
    notEnrolledLearners.forEach(function(learner) {
      let rowHtml = `
          <tr>
            <td class="text-center align-middle">
              ${
                learner.completionStatus == null ? '미배정' :
                  learner.completionStatus === 'IN_PROGRESS' ? '교육생' :
                    learner.completionStatus === 'COMPLETED' ? '수료생' :
                      learner.completionStatus === 'DROPPED' ? '중퇴생' :
                        learner.completionStatus
              }
            </td>
            <td class="text-center align-middle">${learner.fullname}</td>
            <td class="text-center align-middle">${learner.mobile ? learner.mobile : '-'}</td>
            <td class="text-center align-middle">
              <button class="btn btn-info btn-icon-split btn-sm not-enrolled-add-button" data-id="${learner.id}">
                <span class="text">추가</span>
              </button>
            </td>
          </tr>
        `;
      $('#not-enrolled-list').append(rowHtml);
    });
  }
}

function updateSelectBox(selector, data) {
  const $select = $(selector).empty();
  data.forEach(course => {
    $select.append($('<option>').val(course.id).text(course.name));
  });
}

/* =================================================================================================== */

async function handleCourseSelectChange() {
  // 검색창 초기화
  $('#search-input').val('');


  let { enrolledLearners, notEnrolledLearners} = await getLearnersByEnrollment();
  displayTables(enrolledLearners, notEnrolledLearners);
}

async function handleAddEnrollment() {
  let learnerId = $(this).data('id');
  let courseId = $("#course-select").val();

  // learner_enrollment 테이블에 courseId, learnerId를 추가
  let data = await apiPostRequest(
    '/api/learner-enrollments',
    {
      learnerId: Number(learnerId),
      courseId: Number(courseId) });

  let {enrolledLearners, notEnrolledLearners} = await getLearnersByEnrollment();
  if (notEnrolledLearners.length == 0) {
    $("#search-input").val('');
    handleSearchChange();
  }
  displayTables(enrolledLearners, notEnrolledLearners);
}

async function handleRemoveEnrollment() {
  let learnerId = $(this).data('id');
  let courseId = $("#course-select").val();

  let data = await apiDeleteRequest(
    '/api/learner-enrollments',
    { learnerId: Number(learnerId), courseId: Number(courseId) });

  let {enrolledLearners, notEnrolledLearners} = await getLearnersByEnrollment();
  displayTables(enrolledLearners, notEnrolledLearners);
}

async function handleSearchChange() {
  learnerConfig.type = 'fullname';
  learnerConfig.keyword = $("#search-input").val();
  let { enrolledLearners, notEnrolledLearners} = await getLearnersByEnrollment();
  displayTables(enrolledLearners, notEnrolledLearners);
}

async function handleIncludeAllCheckbox() {

  includeAll = $('#include-all').is(':checked') ? '1' : null;
  let { enrolledLearners, notEnrolledLearners} = await getLearnersByEnrollment();
  displayTables(enrolledLearners, notEnrolledLearners);
}

