const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();

let baseConfig = {
  loginUserId: loginUserId,
  loginUserType: loginUserType
}
let courseConfig = {
  pageNo: 1,
  pageSize: 10,
  type: "coName", // Builder.Default (coName)
  keyword: null,
  orderBy: "coStartDate", // Builder.Default (coStartDate)
  orderDirection: "ASC", // Builder.Default (ASC)
  // 필터링
  coIsInProgress: null,
  coId: null
};

$(document).ready(function() {

  window.addEventListener('beforeunload', function(e) {

    // 외부로 이동 시 config 데이터 삭제
    if (!sessionStorage.getItem('isEnteringDetail')) {
      sessionStorage.removeItem('courseConfig');
    }
    // 내부로 이동 시 플래그만 삭제 (목록으로 돌아와도 courseConfig는 남아있음)
    else {
      sessionStorage.removeItem('isEnteringDetail');
    }
  });

  // 세션 정보 불러오기
  getStatus();

  // ADMINISTRATOR는 메뉴로 접근, 그 외에는 과정이력조회에서 접근 (상단셀렉트박스 불필요)
  $('#courseSelector').hide();

  // 셀렉트박스의 기본값 설정
  $("#is-in-progress").val(
      courseConfig.coIsInProgress == null ? '' : courseConfig.coIsInProgress);
  $('#search-input').val(
      courseConfig.keyword == null ? '' : courseConfig.keyword);
  $('#order-by').val(courseConfig.orderBy);
  $('#order-direction').val(courseConfig.orderDirection);

  // 리스트 조회 후 뷰로 반환
  fetchAndDisplayCourses();

  // 이벤트 핸들러
  $(document).on('change', '#is-in-progress', handleIsInProgressSelectChange);
  $(document).on('click', '#search-button', handleSearchButtonClick);
  $(document).on('keydown', '#search-input', function(e) {
    if (e.key == "Enter") {
      e.preventDefault();
      handleSearchButtonClick();
    }
  });
  $(document).on('change', '#order-by', handleOrderBySelectChange);
  $(document).on('change', '#order-direction', handleOrderDirectionSelectChange);
  $(document).on('click', '.page-link', handlePageButtonClick);
});

/* ================================================================================ */

async function fetchAndDisplayCourses() {
  let coursesWithPaging= await apiGetRequestParams(
      '/api/coursemanagement/courses',
      {...baseConfig, ...courseConfig});
  // console.log(coursesWithPaging);

  displayView(coursesWithPaging);
}
async function apiGetRequestParams(endpoint, params) {
  try {
    const response = await axios.get(endpoint, {params: params});
    return response.data.data;
  } catch (error) {
    return [];
  }
}
function displayView(coursesWithPaging) {
  displayTableBody(coursesWithPaging);
  displayPagination(coursesWithPaging, $("#course-pagination"));
}
function displayTableBody(coursesWithPaging) {
  $('#table-body').empty();

  let courses = coursesWithPaging?.records || [];
  if (!Array.isArray(courses)) courses = [];

  if(courses.length == 0) {
    $("#table-body").html(`
        <tr class='text-center'><td colspan='7'>데이터가 없습니다.</td></tr>
    `);
    return;
  }

  courses.forEach(function(record) {
    let course = record.courseWithAssignedInfo;
    let rowHtml = `
      <tr>
        <td class="text-center align-middle ${course.coIsInProgress ? 'text-primary':''}">
          ${course.coIsInProgress ? '진행 중' : '종료'}</td>
        <td class="title align-middle">
          <a href="/courseManagement/courseDetail?courseId=${course.coId}" onclick="setFlag();">${course.coName}</a>
        </td>
        <td class="text-center align-middle">${course.coStartDate} ~ ${course.coEndDate}</td>
        <td class="text-center align-middle">${course.coNumberOfLearner}</td>
        <td class="text-center align-middle">${course.coInstructorName}</td>
        <td class="text-center align-middle">${course.coClassroomName}</td>
        <td class="text-center align-middle">
          <button class="btn btn-primary btn-icon-split btn-sm">
            <span class="text"><a href="/courseManagement/courseSchedule?courseId=${course.coId}">조회</a></span>
          </button>
        </td>
      </tr>`;
    $('#table-body').append(rowHtml);
  });
}
function displayPagination(data, $selector) {

  // 기록이 없을 때, 페이지네이션도 표시되지 않음
  if(data.totalRecords == 0) {
    $selector.html("");
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

  $selector.html(output);
}

/* ================================================================================ */

function handleIsInProgressSelectChange() {
  // 기존 검색과 페이징 초기화
  courseConfig.keyword = null;
  $('#search-input').val('');
  courseConfig.pageNo = 1;
  courseConfig.pageSize = 10;

  // 셀렉트박스 옵션 변경
  courseConfig.coIsInProgress =
      $('#is-in-progress').val() === '' ? null : $('#is-in-progress').val();

  setStatus();
  fetchAndDisplayCourses();
}
function handleOrderBySelectChange() {
  // 모든 상태 유지하고 orderBy만 변경
  courseConfig.orderBy = $(this).val();

  setStatus();
  fetchAndDisplayCourses();
}
function handleOrderDirectionSelectChange() {
  // 모든 상태 유지하고 orderDirection 변경
  courseConfig.orderDirection = $(this).val();

  setStatus();
  fetchAndDisplayCourses();
}
function handleSearchButtonClick() {
  // 페이징 초기화
  courseConfig.pageNo = 1;
  courseConfig.pageSize = 10;

  // 전체에서 검색
  courseConfig.coIsInProgress = null;
  $('#is-in-progress').val('');

  // 키워드로 검색
  courseConfig.keyword = $("#search-input").val();

  setStatus();
  fetchAndDisplayCourses();
}
function handlePageButtonClick() {
  courseConfig.pageNo = $(this).data('page');

  setStatus();
  fetchAndDisplayCourses();
}

function getStatus() {

  let courseStatusByUser = sessionStorage.getItem('courseConfig');

  if (courseStatusByUser) {
    const parsedCourseConfig = JSON.parse(sessionStorage.getItem('courseConfig'));
    Object.assign(courseConfig, parsedCourseConfig);
  }
}
function setStatus() {
  sessionStorage.setItem(
      'courseConfig', JSON.stringify(courseConfig));
}
function setFlag() {
  sessionStorage.setItem(
      'isEnteringDetail', 'true');
}