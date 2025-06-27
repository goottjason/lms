const $courseSelect = $("#courseSelector");
const $progressFilter = $("#progress-filter");
const $courseFilter = $("#course-filter");

const $qnaRegisterBtn = $(".qna-register-btn");
const $qnaTableBody = $(".qna-table-body");

const $paginationContainer = $(".pagination");

let selectedCourse;
let currentPageNo = 1;
let userType;

let qnaRequest;

$(document).ready(async function () {

  qnaRequest = {
    "currentPageNo": currentPageNo,
    "pageSize": 10,
    "currentPageGroup": 1,
    "pagesPerGroup": 10,
    "totalItemsCount": 0,
    "searchOptions": {
      "isInProgress": null,
      "courseName": "",
      "searchType": "",
      "keyWord": "",
      "answerStatus": "",
      "sortBy": "created_at",
      "sortOrder": "DESC"
    }
  };

  const rest = qnaObjectToQuery(qnaRequest);

  let qnaRes = await fetchQnA(rest);

  console.log(qnaRes);
  userType = qnaRes.data.message;

  if (userType === "ADMINISTRATOR") {
    await getAdminCourses();
  } else {
    await getUserCourses();

    console.log(selectedCourse);
    qnaRequest.searchOptions.courseName = selectedCourse;
    console.log(qnaRequest);
    qnaRes = await fetchQnA(qnaObjectToQuery(qnaRequest));
    console.log(qnaRes);
  }

  renderQnAPageUserType(userType);
  renderQnAList(qnaRes.data.data.items);
  renderPagination(qnaRes.data.data);
});

function qnaObjectToQuery(qnaRequest) {

  const { searchOptions, ...rest } = qnaRequest;

  Object.entries(searchOptions).forEach(([key, val]) => {
    rest[`searchOptions.${key}`] = val;
  });
  return rest;
}

//------------------------------------------------------------------------------
// [[필터]]
//------------------------------------------------------------------------------

// 관리자 필터 호출
function getAdminCourses(isInProgress = null) {

  fetchAdminCourses(isInProgress)
  .then((res) => {
    renderAdminCourseOptions("#course-filter", res.data.data);
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

// 사용자(수강생/강사) 필터 호출
function getUserCourses() {

  return fetchUserCourses()
  .then((res) => {
    renderUserCourseOptions("#courseSelector", res.data.data);
  })
  .catch((err) => console.log(err));
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
}

//------------------------------------------------------------------------------
// [[render 함수]]
//------------------------------------------------------------------------------

function renderQnAPageUserType(userType) {

  if (userType === "LEARNER") {
    // 관리자 필터 제거
    removeAdminFilter();

    // 비밀글 여부 적용

  } else if (userType === "INSTRUCTOR") {
    // 관리자 필터 제거
    removeAdminFilter();

    // 글 등록 제거
    $qnaRegisterBtn.remove();

  } else if (userType === "ADMINISTRATOR") {
    // 유저 필터 제거
    removeUserFilter();

    // 글 등록 제거
    $qnaRegisterBtn.remove();

  }
}

function removeUserFilter() {

  $courseSelect.remove();
}

function removeAdminFilter() {
  $progressFilter.remove();
  $courseFilter.remove();
}

// 시험 리스트 요소 생성
function renderQnAList(data) {
  $qnaTableBody.empty(); // table body 초기화

  if (data.length === 0) {
    // 아무 데이터도 없을 경우
    $qnaTableBody.append(`
    <tr>
      <td colspan="6" class="text-center text-muted py-3">
      등록된 QnA가 없습니다.
      </td>
    </tr>
    `);
    return;
  }

  console.log(data);
  $.each(data, function (index, el) {
    console.log(el);
    const qnaData = {
      boardNo: el.id,
      title: el.title,
      courseName: el.courseName,
      writer: el.writerName,
      regDate: el.createdAt,
      answerStatus: el.answer === false ? "답변 전" : "답변 완료",
      secretStatus: el.secret
    };

    $qnaTableBody.append(makeQnARow(userType, qnaData));
  });

}

function makeQnARow(userType, qnaData) {

  // detailPageUrl = "LEARNER" === userType
  //     ? `/test/testDetail/${test.testId}/learner`
  //     : `/test/testDetail/${test.testId}`;

  return `
    <tr class="text-center">
      <th scope="col" class="align-middle">${qnaData.boardNo}</th>
      <th scope="col" class="align-middle">${qnaData.title}</th>
      <th scope="col" class="align-middle">${qnaData.courseName}</th>
      <th scope="col" class="align-middle">${qnaData.writer}</th>
      <th scope="col" class="align-middle">${qnaData.regDate}</th>
      <th scope="col" class="align-middle">${qnaData.answerStatus}</th>
    </tr>
    `;
}

// function renderQnAList()

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

  console.log(data);
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
  $paginationContainer.on("click",
      ".prev-page-group, .page-no, .next-page-group", function (e) {

        const $btn = $(this);

        if ($btn.hasClass("disabled")) {
          return;
        }

        const page = $btn.data("page-no");
        onPageChange(page);
      });
}

function onPageChange(pageNo) {

  qnaRequest.currentPageNo = pageNo;

  fetchQnA(qnaObjectToQuery(qnaRequest))
  .then((res) => {
    console.log(res.data.data.items);
    renderQnAList(res.data.data.items);
    renderPagination(res.data.data);
  });
}

//------------------------------------------------------------------------------
// [[필터 정렬 이벤트]]
//------------------------------------------------------------------------------

const $answerStatus = $("#answer-status");
const $sortBy = $("#sort-by");
const $sortOrder = $("#sort-order");

$answerStatus.on("click", function () {

})