//------------------------------------------------------------------------------
// [[DOM 셀렉터 캐싱]]
//------------------------------------------------------------------------------

const $courseSelect = $("#courseSelector");
const $adminCourseFilter = $("#admin-filter");
const $statusFilter = $("#progress-filter");
const $adminCourseSelect = $("#course-filter");

const $paginationContainer = $(".pagination");

const $testRegisterBtn = $("#test-register-btn");
const $testTableBody = $(".test-table-body");

//------------------------------------------------------------------------------
// [[어플리케이션 상태]]
//------------------------------------------------------------------------------

let selectedCourse;
let currentPageNo = 1;
let detailPageUrl;
let userId;

//------------------------------------------------------------------------------
// [[필터]]
//------------------------------------------------------------------------------

// 관리자 필터 호출 callAdminCourses
function getAdminCourses(isInProgress = null) {

  fetchAdminCourses(isInProgress)
  .then((res) => {
    renderAdminCourseOptions("#course-filter", res.data.data);
  })
  .catch((err) => Swal.fire("오류", "제출 중 오류가 발생했습니다. 다시 시도해주세요.", "error"));

}

// 사용자(수강생/강사) 필터 호출
function getUserCourses() {

  fetchUserCourses()
  .then((res) => {
    renderUserCourseOptions("#courseSelector", res.data.data);

  })
  .catch((err) => Swal.fire("오류", "제출 중 오류가 발생했습니다. 다시 시도해주세요.", "error"));
}

// 관리자 필터 생성
function renderAdminCourseOptions(selector, data) {
  const $adminCourseSelect = $(selector);
  $adminCourseSelect.empty();

  $adminCourseSelect.append("<option value=\"\">전체(과정별)</option>");

  $.each(data, function (index, el) {
    $adminCourseSelect.append(`<option value="${el}">${el}</option>`);
  });

}

// 사용자(수강생/강사) 필터 생성
function renderUserCourseOptions(selector, data) {
  let $adminCourseSelect = $(selector);
  $adminCourseSelect.empty();

  // 진행중인 강좌 요소 찾기
  const defaultEl = data.find(el => el.inProgress) || data[0];
  const selectedCourseName = defaultEl ? defaultEl.courseName : "";

  $.each(data, function (index, el) {

    $adminCourseSelect.append(
        `<option value="${el.courseName}" data-is-in-progress="${el.inProgress}">${el.courseName}</option>`);
  });

  if (selectedCourse === null || selectedCourse === undefined) {
    selectedCourse = selectedCourseName;
    $adminCourseSelect.val(selectedCourseName);
  }

  $adminCourseSelect.val(selectedCourse);
  renderTestListPage(selectedCourse, currentPageNo);
}

// 진행상황별 필터 선택시 조건에 맞는 강좌 불러오기 (관리자)
$statusFilter.on("change", function () {

  const isInProgress = $(this).val(); // 진행상황 값

  if (isInProgress === "") {
    // "전체(진행별)"을 클릭했을 경우 => 전체 리스트 가져오기
    $adminCourseSelect.empty().append("<option value=\"\">전체(과정별)</option>");

    // 과정별 필터에 모든 과정명 불러오기
    getAdminCourses();
    renderTestListPage();
    return;
  }

  getAdminCourses(isInProgress);
});

// 과정별 필터 값을 바꾸었을 때 리스트 불러오기 (관리자)
$adminCourseSelect.on("change", function () {

  const courseName = $(this).val();

  if (courseName === "") {
    $statusFilter.val("");
  }

  selectedCourse = courseName;
  renderTestListPage(courseName);
});

$courseSelect.on("change", function () {
  selectedCourse = $(this).val();

  const isInProgress = $(this).find("option:selected").data("is-in-progress");

  if (isInProgress) {
    $testRegisterBtn.show();
  } else {
    $testRegisterBtn.hide();
  }

  renderTestListPage(selectedCourse);
});

//------------------------------------------------------------------------------
// [[Pagination 함수]]
//------------------------------------------------------------------------------

// Pagination 요소 생성
function renderPagination(data) {
  $paginationContainer.empty(); // 페이지 부분 초기화
  console.log(data.items);

  if (!data.items.length) {
    return;
  }

  // 이전 그룹 이동 버튼
  $paginationContainer.append(`
  <li class="page-item prev-page-group ${!data.prev ? "disabled"
      : ""}" 
        data-page-no=${(data.currentPageGroup - 1) * data.pagesPerGroup}>
      <span class="page-link">이전</span>
  </li>
  `);

  // 페이지 번호 생성
  for (let i = data.startPageNo;
      i <= Math.min(data.endPageNo, data.totalPages); i++) {
    $paginationContainer.append(`
     <li class="page-item page-no ${data.currentPageNo === i ? "active"
        : ""}" data-page-no=${i}>
         <span class="page-link">${i}</span>
     </li>
    `);
  }

  // 다음 그룹 이동 버튼
  $paginationContainer.append(`
  <li class="page-item next-page-group ${data.next ? "" : "disabled"}"
   data-page-no=${data.currentPageGroup * data.pagesPerGroup + 1}>
     <span class="page-link page-move">다음</span>
  </li>
  `);

  $paginationContainer.off();
  $paginationContainer.on("click", ".prev-page-group", function () {
    onPageChange($(this).data("page-no"));
  });
  $paginationContainer.on("click", ".page-no", function () {
    onPageChange($(this).data("page-no"));
  });
  $paginationContainer.on("click", ".next-page-group", function () {
    onPageChange($(this).data("page-no"));
  });

}

function onPageChange(pageNo) {

  currentPageNo = pageNo;
  renderTestListPage(selectedCourse, currentPageNo);
}

//------------------------------------------------------------------------------
// [[템플릿 함수]]
//------------------------------------------------------------------------------

function makeTestRow(userType, test) {

  detailPageUrl = "LEARNER" === userType
      ? `/test/learner/testDetail/${test.testId}`
      : `/test/testDetail/${test.testId}`;

  return `
    <tr>
      <td class="text-center align-middle">${test.testId}</td>
      <td class="title align-middle test-detail-btn"><a href="${detailPageUrl}?userId=${userId}&currentPageNo=${currentPageNo}&courseName=${$courseSelect.val()}&testStatus=${test.testStatus}">${test.testTitle}</a></td>     
      <td class="title align-middle text-truncate" style="max-width: 200px;">${test.courseName}</td>
      <td class="text-center align-middle">${test.testPeriod}</td>
      <td class="text-center align-middle">${test.testStatus}</td>
      <td class="text-center align-middle">${test.testTime}</td>
      <td class="text-center align-middle">${test.participantCount}</td>
    </tr>
    `;
}

//------------------------------------------------------------------------------
// [[시험 리스트 공통 함수]]
//------------------------------------------------------------------------------

// userType에 따른 화면 변화
function renderPageByUserType(userType, currentPageNo = 1) {

  if (userType === "ADMINISTRATOR") {
    $courseSelect.remove();
    $testRegisterBtn.remove();
    getAdminCourses();

    renderTestListPage("", currentPageNo);
  } else {

    $adminCourseFilter.remove();
    getUserCourses();
    // renderTestListPage();

    if (userType === "LEARNER") {
      $testRegisterBtn.remove();
    }
  }
}

// 시험 리스트 페이지 호출
function renderTestListPage(courseName = "", currentPageNo = 1) {

  console.log(courseName);

  fetchTests({ courseName: courseName, currentPageNo: currentPageNo })
  .then(function (response) {
    console.log(response);

    renderTestList(response.data.message, response.data.data.items);
    renderPagination(response.data.data);
  })
  .catch(function (error) {
    Swal.fire("오류", "제출 중 오류가 발생했습니다. 다시 시도해주세요.", "error");
  });
}

// 시험 리스트 요소 생성
function renderTestList(userType, data) {
  $testTableBody.empty(); // table body 초기화

  if (data.length === 0) {
    // 아무 데이터도 없을 경우
    $testTableBody.append(`
    <tr>
      <td colspan="7" class="text-center text-muted py-3">
      등록된 시험이 없습니다.
      </td>
    </tr>
    `);
    return;
  }

  console.log(data);
  $.each(data, function (index, el) {
    console.log(el);

    let test = {
      testId: el.id,
      testTitle: el.title,
      courseName: el.courseName,
      testPeriod: el.startDate.split("T")[0] + " ~ " +
          el.endDate.split(
              "T")[0],
      testStatus: assignValueByStatus(el.testStatus),
      testTime: el.testTime + "분",
      participantCount: `${el.completedCount}/${el.numberOfLearner}`
    };

    $testTableBody.append(makeTestRow(userType, test));
  });

}

// DB에서 가져온 testStatus 값에 따라 진행상태 값 할당하는 함수
function assignValueByStatus(testStatus) {
  let testStatusMap = {
    NOT_STARTED: "진행 전",
    IN_PROGRESS: "진행 중",
    COMPLETED: "종료"
  };

  return testStatusMap[testStatus];
}

//------------------------------------------------------------------------------
// [[브라우저 첫 로딩]]
//------------------------------------------------------------------------------

$(document).ready(function () {

  userId = $("#login-user-id").val();

  const Toast = Swal.mixin({
    toast: true,
    position: "bottom-end",
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    didOpen: (toast) => {
      toast.onmouseenter = Swal.stopTimer;
      toast.onmouseleave = Swal.resumeTimer;
    }
  });

  if (UrlUtils.getQueryParam("saved") === "true") {
    Toast.fire({
      icon: "success",
      title: "새 시험이 등록되었습니다."
    });
  } else if (UrlUtils.getQueryParam("removed") === "true") {
    Toast.fire({
      icon: "success",
      title: "시험이 삭제되었습니다."
    });
  }

  let currentPageNo;
  if (new URLSearchParams(window.location.search)
  .get("currentPageNo")) {

    currentPageNo = new URLSearchParams(window.location.search)
    .get("currentPageNo");

  }

  if (UrlUtils.getQueryParam("courseName")) {

    selectedCourse = UrlUtils.getQueryParam("courseName")
        ? UrlUtils.getQueryParam(
            "courseName") : null;
    $courseSelect.val(selectedCourse);
    console.log(selectedCourse);
  }

  fetchTests({ courseName: selectedCourse, currentPageNo: currentPageNo })
  .then(function (response) {
    console.log(response);

    renderPageByUserType(response.data.message,
        response.data.data.currentPageNo);
  })
  .catch(function (error) {

  });

});

$testRegisterBtn.on("click", function () {

  let currentPageNo = $(this).data("current-page-no");
  // let courseName = $courseSelect.val();

  location.href = `/test/register?currentPageNo=${currentPageNo}`;
});