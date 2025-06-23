//------------------------------------------------------------------------------
// [[DOM 셀렉터 캐싱]]
//------------------------------------------------------------------------------
const $courseSelector = $("#courseSelector");
const $adminFilter = $("#admin-filter");
const $progressFilter = $("#progress-filter");
const $courseFilter = $("#course-filter");

const $testRegisterBtn = $("#test-register-btn");

const $testTableBody = $(".test-table-body");

const $pagePart = $(".pagination");

//------------------------------------------------------------------------------
// [[어플리케이션 상태]]
//------------------------------------------------------------------------------

let defaultCourse;
let currentPageNoForPrev = 1;
let basePath;

//------------------------------------------------------------------------------
// [[템플릿 함수]]
//------------------------------------------------------------------------------

function makeTestRow(userType, test) {
  console.log(test);

  basePath = "LEARNER" === userType ? `/test/testDetail/${test.testId}/learner`
      : `/test/testDetail/${test.testId}`;

  return `
    <tr>
      <td class="text-center align-middle">${test.testId}</td>
      <td class="title align-middle test-detail-btn"><a href="${basePath}?currentPageNo=${currentPageNoForPrev}&courseName=${$courseSelector.val()}&testStatus=${test.testStatus}">${test.testTitle}</a></td>     
      <td class="title align-middle text-truncate" style="max-width: 200px;">${test.courseName}</td>
      <td class="text-center align-middle">${test.testPeriod}</td>
      <td class="text-center align-middle">${test.testStatus}</td>
      <td class="text-center align-middle">${test.testTime}</td>
      <td class="text-center align-middle">${test.participantCount}</td>
    </tr>
    `;
}

//------------------------------------------------------------------------------
// [[render 함수]]
//------------------------------------------------------------------------------

// 관리자 필터 호출
function renderAdminCourseFilter(isInProgress = null) {
  axios.get(`/api/admin/courses`, { params: { isInProgress: isInProgress } })
       .then(function (response) {
         renderCourseFilterOptionsForAdmin("#course-filter",
             response.data.data);
       })
       .catch(function (error) {

       });
}

// 사용자(수강생/강사) 필터 호출
function renderUserCourseFilter() {
  return axios.get(`/api/courses`)
              .then(function (response) {
                console.log(response);
                const data = response.data.data;
                renderCourseFilterOptionsForAdminForUser("#courseSelector",
                    response.data.data);

                return data.find(el => el.inProgress)?.courseName ||
                    data[0]?.courseName || "";
              });
}

// 관리자 필터 생성
function renderCourseFilterOptionsForAdmin(selector, data) {
  let $courseFilter = $(selector);
  $courseFilter.empty();

  let courseFilterPlaceholder = "<option value=\"\">전체(과정별)</option>";
  $courseFilter.append(courseFilterPlaceholder);

  $.each(data, function (index, el) {

    console.log(el);

    let courseOption = `<option value="${el}">${el}</option>`;
    $courseFilter.append(courseOption);
  });

}

// 사용자(수강생/강사) 필터 생성
function renderCourseFilterOptionsForAdminForUser(selector, data) {
  let $courseFilter = $(selector);
  $courseFilter.empty();

  console.log(data);

  const defaultEl = data.find(el => el.inProresss) || data[0];
  const defaultCourseName = defaultEl ? defaultEl.courseName : "";

  $.each(data, function (index, el) {

    let courseOption = `<option value="${el.courseName}" data-is-in-progress="${el.inProgress}">${el.courseName}</option>`;
    $courseFilter.append(courseOption);
  });

  if (defaultCourse === null || defaultCourse == undefined) {
    defaultCourse = defaultCourseName;
    $courseFilter.val(defaultCourseName);
  }

  $courseFilter.val(defaultCourse);
}

//------------------------------------------------------------------------------
// [[시험 리스트 공통 함수]]
//------------------------------------------------------------------------------

// userType에 따른 화면 변화
async function renderPageByUserType(userType, currentPageNo = 1) {

  if (userType === "LEARNER") {
    $adminFilter.remove();
    $testRegisterBtn.remove();
    const courseName = await renderUserCourseFilter();

    renderTestListPage(defaultCourse, currentPageNo);

    return;
  }

  if (userType === "INSTRUCTOR") {
    $adminFilter.remove();
    const courseName = await renderUserCourseFilter();
    console.log(currentPageNo);
    console.log(courseName);
    renderTestListPage(defaultCourse, currentPageNo);

    return;
  }

  if (userType === "ADMINISTRATOR") {
    $courseSelector.remove();
    $testRegisterBtn.remove();
    renderAdminCourseFilter();
    renderTestListPage("", currentPageNo);
  }
}

// 시험 리스트 페이지 호출
function renderTestListPage(courseName = "", currentPageNo = 1) {

  console.log(courseName);
  axios.get(`/api/tests`,
      {
        params: {
          courseName: courseName,
          currentPageNo: currentPageNo
        }
      })
       .then(function (response) {
         console.log(response);

         renderTestList(response.data.message, response.data.data.items);

         console.log(response.data.data.items.length === 0);

         renderPagination(response.data.data);

       })
       .catch(function (error) {

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
// [[Pagination 공통 함수]]
//------------------------------------------------------------------------------

// Pagination 요소 생성
function renderPagination(data) {
  $pagePart.empty(); // 페이지 부분 초기화
  console.log(data.items);

  if (data.items.length) {

    // 이전 그룹 이동 버튼
    $pagePart.append(`
  <li class="page-item prev-page-group ${!data.prev ? "disabled"
        : ""}" data-current-page-group=${data.currentPageGroup} data-pages-per-group=${data.pagesPerGroup}>
      <span class="page-link">이전</span>
  </li>
  `);

    // 페이지 번호 생성
    for (let i = data.startPageNo;
        i <= Math.min(data.endPageNo, data.totalPages); i++) {
      $pagePart.append(`
     <li class="page-item page-no ${data.currentPageNo === i ? "active"
          : ""}" data-page-no=${i}>
         <span class="page-link">${i}</span>
     </li>
    `);
    }

    // 다음 그룹 이동 버튼
    $pagePart.append(`
  <li class="page-item next-page-group ${data.next ? "" : "disabled"}"
   data-current-page-group=${data.currentPageGroup} data-pages-per-group=${data.pagesPerGroup}>
     <span class="page-link page-move">다음</span>
  </li>
  `);

  }
}

//------------------------------------------------------------------------------
// [[브라우저 첫 로딩]]
//------------------------------------------------------------------------------

$(document).ready(function () {

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

  const currentPageNo = new URLSearchParams(window.location.search)
  .get("currentPageNo");
  console.log(currentPageNo);

  defaultCourse = UrlUtils.getQueryParam("courseName") ? UrlUtils.getQueryParam(
      "courseName") : null;
  console.log(defaultCourse);

  axios.get(`/api/tests`, {
    params: {
      courseName: defaultCourse,
      currentPageNo: currentPageNo
    }
  })
       .then(function (response) {
         console.log(response);

         renderPageByUserType(response.data.message,
             response.data.data.currentPageNo);
       })
       .catch(function (error) {

       });

});

//------------------------------------------------------------------------------
// [[Pagination]]
//------------------------------------------------------------------------------

// 이전 페이지 그룹으로 이동 이벤트
$(document).on("click", ".prev-page-group", function () {
  if ($(this).hasClass("disabled")) {
    // 버튼에 ".disabled"가 존재하면 작동 X
    return;
  }

  let currentPageGroup = $(this).data("current-page-group");
  let pagesPerGroup = $(this).data("pages-per-group");

  // 이전 페이지 그룹의 첫 번호
  let courseName = $courseFilter.length ? $courseFilter.val()
      : $(
          "#courseSelector").val();
  let prevPageGroup = (currentPageGroup - 1) * pagesPerGroup;

  currentPageNoForPrev = prevPageGroup;
  // $testRegisterBtn.data("current-page-no", prevPageGroup)
  //                 .attr("data-current-page-no", prevPageGroup);

  renderTestListPage(defaultCourse, prevPageGroup);
});

// 페이지 번호 클릭시 해당 페이지 번호로 이동
$(document).on("click", ".page-no", function () {
  let courseName = $courseFilter.length ? $courseFilter.val() : $(
      "#courseSelector").val();
  console.log(courseName);
  let currentPageNo = $(this).data("page-no");

  currentPageNoForPrev = currentPageNo;
  // $testRegisterBtn.data("current-page-no", currentPageNo)
  //                 .attr("data-current-page-no", currentPageNo);

  renderTestListPage(defaultCourse, currentPageNo);
});

// 다음 페이지 그룹으로 이동
$(document).on("click", ".next-page-group", function () {
  if ($(this).hasClass("disabled")) {
    // 버튼에 ".disabled"가 존재하면 작동 X
    return;
  }

  let currentPageGroup = $(this).data("current-page-group");
  let pagesPerGroup = $(this).data("pages-per-group");

  // 다음 페이지 그룹의 첫 번째 번호
  let courseName = $courseFilter.length ? $courseFilter.val()
      : $(
          "#courseSelector").val();
  let nextPageGroup = currentPageGroup * pagesPerGroup + 1;

  currentPageNoForPrev = nextPageGroup;
  // $testRegisterBtn.data("current-page-no", nextPageGroup)
  //                 .attr("data-current-page-no", nextPageGroup);

  renderTestListPage(defaultCourse, nextPageGroup);
});

//------------------------------------------------------------------------------
// [[필터]]
//------------------------------------------------------------------------------

// 진행상황별 필터 선택시 조건에 맞는 강좌 불러오기 (관리자)
$progressFilter.on("change", function () {
  console.log("진행상황 필터!!!");
  let isInProgress = $(this).val(); // 진행상황 값

  // 전체 시험 리스트 페이지 불러오기
  // renderTestListPage();

  if (isInProgress === "") {
    // "전체(진행별)"을 클릭했을 경우 => 전체 리스트 가져오기
    $courseFilter.empty(); // 진행별 드롭다운 초기화

    let courseFilterPlaceholder = "<option value=\"\">전체(과정별)</option>";
    $courseFilter.append(courseFilterPlaceholder);

    // 과정별 필터에 모든 과정명 불러오기
    renderAdminCourseFilter();

    renderTestListPage();
    return;
  }

  renderAdminCourseFilter(isInProgress);
});

// 과정별 필터 값을 바꾸었을 때 리스트 불러오기 (관리자)
$courseFilter.on("change", function () {
  console.log("과정별 필터!!!");
  let courseName = $(this).val();

  if (courseName === "") {

    $progressFilter.val("");
  }
  console.log(courseName);
  defaultCourse = courseName;
  renderTestListPage(courseName);
});

$courseSelector.on("change", function () {
  defaultCourse = $(this).val();
  console.log(defaultCourse);

  let isInProgress = $(this).find("option:selected").data("is-in-progress");

  if (isInProgress) {
    $testRegisterBtn.show();
  } else {
    $testRegisterBtn.hide();
  }

  renderTestListPage(defaultCourse);
});

$testRegisterBtn.on("click", function () {

  let currentPageNo = $(this).data("current-page-no");
  // let courseName = $courseSelector.val();

  location.href = `/test/register?currentPageNo=${currentPageNo}`;
});