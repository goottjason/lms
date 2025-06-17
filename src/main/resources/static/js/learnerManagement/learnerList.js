const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();
let courses = null;
const courseConfig = {
  pageNo: null,
  pageSize: null,
  type: null,
  keyword: null,
  orderBy: null,
  orderDirection: null,
};
let isInProgress = null;

const learnerConfig = {
  pageNo: null,
  pageSize: null,
  type: null,
  keyword: null,
  orderBy: null,
  orderDirection: null,
};
let courseId = null;

/* ================================================================================ */

$(document).ready(() => {

  getListState()
  $("#is-in-progress").val(isInProgress == null ? '' : isInProgress);
  $("#course-select").val(courseId == null ? '' : courseId);
  $('#search-input').val(learnerConfig.keyword);

  initCourseSelect();

  // 이벤트 핸들러 강제 실행
  $("#course-select").trigger("change");

  handleProgressStatusChange();
  handleCourseSelectChange();

  $('#is-in-progress').on('change', handleProgressStatusChange);
  $('#course-select').on('change', handleCourseSelectChange);
  $('#search-button').on('click', handleSearchChange);
  $(document).on('keydown', '#search-input', function(e) {
    if (e.key == "Enter") {
      e.preventDefault();
      handleSearchChange();
    }
  });
  $(document).on('click', '.page-link', handlePageBtnClick);
});

/* ================================================================================ */

async function initCourseSelect() {
  let coursesWithPagination = await apiGetRequestAboutCourses(
    '/api/courses/all',
    {
      loginUserId: loginUserId,
      loginUserType: loginUserType,
      isInProgress: isInProgress});
  courses = coursesWithPagination?.respDTOS || [];
  if (!Array.isArray(courses)) courses = [];
  updateSelectBox('#course-select', courses);
}

function getListState() {

  let learnerListState = sessionStorage.getItem('learnerListState');

  if (learnerListState) {
    const learnerListState = JSON.parse(sessionStorage.getItem('learnerListState'));
    Object.assign(learnerConfig, learnerListState);
    courseId = learnerListState.courseId;
  }
  let courseListState = sessionStorage.getItem('courseListState');
  if (courseListState) {
    const courseListState = JSON.parse(sessionStorage.getItem('courseListState'));
    Object.assign(courseConfig, courseListState);
    isInProgress = courseListState.isInProgress;
  }
}

function saveListState() {
  sessionStorage.setItem('learnerListState', JSON.stringify({
    ...learnerConfig,
    courseId: courseId
  }));
  sessionStorage.setItem('courseListState', JSON.stringify({
    ...courseConfig,
    isInProgress: isInProgress
  }));

}

async function apiGetRequestAboutCourses(endpoint, additionalParams = {}) {
  try {
    const response = await axios.get(endpoint, {
      params: { ...courseConfig, ...additionalParams }
    });
    console.log(response.data);
    return response.data.data;
  } catch (error) {
    console.error(`${endpoint} 요청 오류:`, error);
    return [];
  }
}
async function apiGetRequestAboutLearners(endpoint, additionalParams = {}) {
  try {
    const response = await axios.get(endpoint, {
      params: { ...learnerConfig, ...additionalParams }
    });
    console.log(response.data);
    return response.data;
  } catch (error) {
    console.error(`${endpoint} 요청 오류:`, error);
    return [];
  }
}

// 진행 상태 변경 핸들러
async function handleProgressStatusChange() {
  learnerConfig.pageNo = 1;
  learnerConfig.pageSize = 12;
  isInProgress = $('#is-in-progress').val() === '' ? null : $('#is-in-progress').val();
  saveListState();

  let coursesWithPagination = await apiGetRequestAboutCourses(
    '/api/courses/all',
    {
      loginUserId: loginUserId,
      loginUserType: loginUserType,
      isInProgress: isInProgress});
  courses = coursesWithPagination?.respDTOS || [];
  if (!Array.isArray(courses)) courses = [];

  updateSelectBox('#course-select', courses);

  displayCardView();
}

function handleCourseSelectChange() {
  learnerConfig.pageNo = 1;
  learnerConfig.pageSize = 12;
  /*learnerConfig.type = null;
  learnerConfig.keyword = null;
  $('#search-input').val('');*/
  courseId = $('#course-select').val() === '' ? null : $('#course-select').val();

  displayCardView();
}

async function handleSearchChange() {

  courseId = null;
  isInProgress = null;
  $('#is-in-progress').val('');
  $('#course-select').val('');

  initCourseSelect();

  learnerConfig.pageNo = 1;
  learnerConfig.pageSize = 12;
  learnerConfig.keyword = $("#search-input").val();
  learnerConfig.type = 'fullname'; // learner API 호출을 위함
  displayCardView();
}

async function displayCardView() {
  learnerConfig.orderBy = 'fullname';
  learnerConfig.orderDirection = 'ASC';
  saveListState();
  let learnersWithPagination = await apiGetRequestAboutLearners(
    '/api/learners/all',
    {
      loginUserId: loginUserId,
      loginUserType: loginUserType,
      courseId: courseId,
      isInProgress: isInProgress });

  let learners = Array.isArray(learnersWithPagination.respDTOS) ?
    learnersWithPagination.respDTOS : [learnersWithPagination.respDTOS];
  displayLearners(learners);
  displayPagination(learnersWithPagination);
  updateStatusBar(learnersWithPagination);
}

function displayLearners(learners) {
  $('#card-list').empty();
  if(learners.length == 0) {
    $("#card-list").html("<span class='text-center'>데이터가 없습니다.</span>");
    return;
  }
  learners.forEach(function(learner) {
    let rowHtml = `
        <div class="col mb-4">
          <div class="card text-center">
            <img src="${learner.userProfileImg}" class="rounded-circle mt-3 mx-auto d-block" style="width: 150px; height: 150px; object-fit: cover;">
            <div class="card-body">
              <h5 class="card-title mb-1">${learner.userFullname}</h5>
              <p class="card-text mb-1">${learner.userMobile || '-'}</p>
              <p class="card-text">${learner.userEmail}</p>
              <p class="card-text">${learner.courseName}</p>
              <p class="card-text">
                ${
                  learner.leCompletionStatus == null ? '미배정' :
                    learner.leCompletionStatus === 'IN_PROGRESS' ? '교육생' :
                      learner.leCompletionStatus === 'COMPLETED' ? '수료생' :
                        learner.leCompletionStatus === 'DROPPED' ? '중퇴생' :
                          learner.leCompletionStatus
                }
              </p>
              <p class="card-text">출석률: ${learner.pageParticipationRespDTO.attendanceRate}%</p>
              <a href="learnerDetail?leId=${learner.leId}" class="btn btn-primary w-100">상세보기</a>
            </div>
          </div>
        </div>
      `;
    $('#card-list').append(rowHtml);
  });
}

function displayPagination(data) {

  // 기록이 없을 때, 페이지네이션도 표시되지 않음
  if(data.totalRecords == 0) {
    $("#learner-pagination").html(""); // 바꿔야 할 부분
    return;
  }

  let output = `<ul class="pagination justify-content-center" style="margin:20px 0">`;

  // 이전 버튼
  let prevBlockPage = data.blockStartPage > 1 ? data.blockStartPage - 1 : 1;
  output += `
    <li class="page-item ${data.blockStartPage == 1? 'disabled': ''}">
      <a class="page-link page-btn" href="#" data-page="${prevBlockPage}">이전</a>
    </li>`;

  // 페이지 번호 버튼
  for (let i = data.blockStartPage; i <= data.blockEndPage; i++) {
    let active = data.pageNo == i ? "active" : "";
    output += `
      <li class="page-item ${active}">
        <a class="page-link page-btn" href="#" data-page="${i}">${i}</a>
      </li>`;
  }

  // 다음 버튼
  let nextBlockPage = data.blockEndPage < data.lastPage ? data.blockEndPage + 1 : data.lastPage;
  output += `
    <li class="page-item ${data.blockEndPage == data.lastPage ? 'disabled': ''}">
      <a class="page-link page-btn" href="#" data-page="${nextBlockPage}">다음</a>
    </li></ul>`;

  $("#learner-pagination").html(output);
}

function updateSelectBox(selector, data) {
  const $select = $(selector).empty().append('<option value="">전체</option>');
  data.forEach(course => {
    $select.append($('<option>').val(course.id).text(course.name));
  });
}

function updateStatusBar(learnersWithPagination)  {
  if (learnerConfig.keyword == null || learnerConfig.keyword == '') {
    if (isInProgress == null) {
      $("#course-name-date").text(`전체 교육생`);
      $("#course-number-of-learner").text(`총 인원: ${learnersWithPagination.totalRecords}명`);
    } else if (isInProgress == '1') {
      $("#course-name-date").text(`현재 과정 진행중인 교육생`);
      $("#course-number-of-learner").text(`총 인원: ${learnersWithPagination.totalRecords}명`);
    } else if (isInProgress == '0') {
      $("#course-name-date").text(`종료된 과정의 교육생`);
      $("#course-number-of-learner").text(`총 인원: ${learnersWithPagination.totalRecords}명`);
    }
    if (courseId != null) {
      const selectedCourse = courses.find(course => course.id == courseId);
      $("#course-name-date").text(`${selectedCourse.name} (${selectedCourse.startDate} ~ ${selectedCourse.endDate})`);
      $("#course-number-of-learner").text(`총 인원: ${learnersWithPagination.totalRecords}명`);
    }
  } else {
    $("#course-name-date").text(`'${learnerConfig.keyword}' 검색결과`);
    $("#course-number-of-learner").text(`총 인원: ${learnersWithPagination.totalRecords}명`);
  }
}

function handlePageBtnClick() {
  learnerConfig.pageNo = $(this).data('page');
  saveListState();
  displayCardView();
}