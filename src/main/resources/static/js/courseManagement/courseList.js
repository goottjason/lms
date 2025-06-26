const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();
let isInProgress =  '1';
let courses = null;
const courseConfig = {
  pageNo: null,
  pageSize: null,
  type: null,
  keyword: null,
  orderBy: null,
  orderDirection: null,
};
const config = {
  pageNo: 1,
  pageSize: 10, // 테스트
  type: "name",
  keyword: null,
  orderBy: "name",
  orderDirection: "ASC",
};

/* ================================================================================ */

$(document).ready(function() {

  // 관리자, 강사 모두 숨김
  // $("#courseSelector").hide();
  if (loginUserType == "INSTRUCTOR") {
    updateTopCourseSelector();
  } else {
    $("#courseSelector").hide();
  }
  getListState();
  $("#is-in-progress").val(isInProgress == null ? '' : isInProgress);
  $('#search-input').val(config.keyword);
  $('#order-direction').val(config.orderDirection);
  $('#order-by').val(config.orderBy);

  fetchAndDisplayView();

  $('#is-in-progress').on('change', handleProgressStatusChange);
  $('#search-button').on('click', handleSearchChange);
  $(document).on('keydown', '#search-input', function(e) {
    if (e.key == "Enter") {
      e.preventDefault();
      handleSearchChange();
    }
  });
  $('#order-by').on('change', handleOrderByChange);
  $('#order-direction').on('change', handleOrderDirectionChange);
  $(document).on('click', '.page-link', handlePageBtnClick);
  $(document).on('change', '#courseSelector', handleCourseSelectChange)
});

/* ================================================================================ */

async function updateTopCourseSelector() {
  let coursesWithPagination = await apiGetRequestAboutCourses(
      '/api/management/courses',
      {
        loginUserId: loginUserId,
        loginUserType: loginUserType,
        isInProgress: null});
  let courses = coursesWithPagination?.respDTOS || [];
  if (!Array.isArray(courses)) courses = [];
  console.log(courses)
  updateTopCourseSelectorOption('#courseSelector', courses);
}
function updateTopCourseSelectorOption(selector, data) {
  console.log(selector, data);
  const $select = $(selector).empty();
  data.forEach(course => {
    $select.append($('<option>').val(course.id).text(course.name));
  });
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



function getListState() {

  let listState = sessionStorage.getItem('listState');

  if (listState) {
    const listState = JSON.parse(sessionStorage.getItem('listState'));
    Object.assign(config, listState);
    isInProgress = listState.isInProgress;
  } else {
    return;
  }
}

function saveListState() {
  sessionStorage.setItem('listState', JSON.stringify({
    ...config,
    isInProgress: isInProgress
  }));
}

async function fetchAndDisplayView() {
  let coursesWithPagination = await apiGetRequest(
    '/api/management/courses',
    {
      loginUserId: loginUserId,
      loginUserType: loginUserType,
      isInProgress: isInProgress
    }
  );
  let courses = coursesWithPagination?.respDTOS || [];
  if (!Array.isArray(courses)) courses = [];
  renderCourseTable(courses);
  renderCoursePagination(coursesWithPagination);
}

async function apiGetRequest(endpoint, additionalParams = {}) {
  try {
    const response = await axios.get(endpoint, {
      params: { ...config, ...additionalParams }
    });
    return response.data.data;
  } catch (error) {
    return [];
  }
}

function renderCourseTable(courses) {
  if(courses.length == 0) {
    $("#table-body").html("<tr class='text-center'><td colspan='7'>데이터가 없습니다.</td></tr>"); // 바꿔야 할 부분
    return;
  }
  $('#table-body').empty();
  courses.forEach(function(course) {
    let rowHtml = `
        <tr>
          <td class="text-center align-middle">${course.isInProgress ? '진행 중': '종료'}</td>
          <td class="title align-middle">
            <a href="/courseManagement/courseDetail?courseId=${course.id}" onclick="saveListState()">${course.name}</a></td>
          <td class="text-center align-middle">${course.startDate} ~ ${course.endDate}</td>
          <td class="text-center align-middle">${course.numberOfLearner}</td>
          <td class="text-center align-middle">${course.fulltimeInstructorFullname}</td>
          <td class="text-center align-middle">${course.classroomName}</td>
          <td class="text-center align-middle">
            <button class="btn btn-primary btn-icon-split btn-sm">
              <span class="text"><a href="/courseSchedule?courseId=${course.id}" onclick="saveListState()">조회</a></span>
            </button>
          </td>
        </tr>
      `;
    $('#table-body').append(rowHtml);
  });
}

function renderCoursePagination(data) {
  // 기록이 없을 때, 페이지네이션도 표시되지 않음
  if(data.totalRecords == 0) {
    $("#course-pagination").html(""); // 바꿔야 할 부분
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

  $("#course-pagination").html(output);
}

/* ================================================================================ */

function handleProgressStatusChange() {
  // 기존 검색 초기화 -> 유지하고 필터할 예정
  /*$("#search-input").val('');
  config.keyword = null;*/

  // 기존에 있던 페이지 초기화
  config.pageNo = 1;

  // 셀렉트박스 옵션 변경
  isInProgress = $('#is-in-progress').val() === '' ? null : $('#is-in-progress').val();

  saveListState();
  fetchAndDisplayView();
}

function handleOrderByChange() {
  config.orderBy = $(this).val();
  saveListState();
  fetchAndDisplayView();
}

function handleOrderDirectionChange() {
  config.orderDirection = $(this).val();
  saveListState();
  fetchAndDisplayView();
}

function handleSearchChange() {
  config.keyword = $("#search-input").val();
  isInProgress = null; // 전체에서 검색
  config.pageNo = 1; // 검색결과 1페이지 보여주기
  saveListState();
  $('#is-in-progress').val('');
  fetchAndDisplayView();
}

function handlePageBtnClick() {
  config.pageNo = $(this).data('page');
  saveListState();
  fetchAndDisplayView();
}