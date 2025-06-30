const $courseSelect = $("#courseSelector");
const $progressFilter = $("#progress-filter");
const $courseFilter = $("#course-filter");

const $statusFilter = $("#progress-filter");
const $adminCourseSelect = $("#course-filter");

const $qnaRegisterBtn = $("#qna-register-btn");
const $qnaTableBody = $(".qna-table-body");

const $searchType = $("#search-type");
const $qnaSearchKeyword = $("#qna-search-keyword");
const $qnaSearchBtn = $("#qna-search-btn");

const $answerStatus = $("#answer-status");
const $sortBy = $("#sort-by");
const $sortOrder = $("#sort-order");

const $paginationContainer = $(".pagination");

let selectedCourse;
let currentPageNo = 1;
let userType;
let userId;

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

  const search = window.location.search;
  if (search && search !== "?") {
    initFromQuery();
  }

  const rest = qnaObjectToQuery(qnaRequest);

  let qnaRes = await fetchQnA(rest);

  console.log(qnaRes);
  userType = qnaRes.data.message.split("&")[0];
  userId = qnaRes.data.message.split("&")[1];

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

function initFromQuery() {
  const params = new URLSearchParams(window.location.search);

  // 페이징 정보 복원
  if (params.has("currentPageNo")) {
    currentPageNo = parseInt(params.get("currentPageNo"), 10);
    qnaRequest.currentPageNo = currentPageNo;
  }
  if (params.has("pageSize")) {
    qnaRequest.pageSize = parseInt(params.get("pageSize"), 10);
  }
  // (필요하면 currentPageGroup, pagesPerGroup 등도 복원)

  // searchOptions 복원
  Object.keys(qnaRequest.searchOptions).forEach(key => {
    const paramKey = `searchOptions.${key}`;
    if (params.has(paramKey)) {
      let val = params.get(paramKey);
      // Boolean 타입 복원
      if (val === "true" || val === "false") {
        val = val === "true";
      }
      qnaRequest.searchOptions[key] = val;
    }
  });

  if (!params.has("searchOptions.searchType") || !params.get(
      "searchOptions.searchType")) {
    qnaRequest.searchOptions.searchType = "title";
  }

  // 화면 컨트롤(폼)에 값 세팅
  $courseSelect.val(qnaRequest.searchOptions.courseName);
  $("#search-type").val(qnaRequest.searchOptions.searchType);
  $("#qna-search-keyword").val(qnaRequest.searchOptions.keyWord);
  $("#answer-status").val(qnaRequest.searchOptions.answerStatus);
  $("#sort-by").val(qnaRequest.searchOptions.sortBy);
  $("#sort-order").val(qnaRequest.searchOptions.sortOrder);

  if (userType === "ADMINISTRATOR") {
    // 진행상황별 필터
    const prog = qnaRequest.searchOptions.isInProgress;
    // null 또는 "" 이면 전체, 아니면 해당 값
    $progressFilter.val(prog == null ? "" : String(prog));

    // 과정별 필터
    const course = qnaRequest.searchOptions.courseName;
    $courseFilter.val(course || "");
  }
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
      writerId: el.loginId,
      regDate: el.createdAt,
      answerStatus: el.isAnswer === false ? "답변 전" : "답변 완료",
      secretStatus: el.isSecret
    };

    $qnaTableBody.append(makeQnARow(userType, qnaData));
  });

}

function makeQnARow(userType, qnaData) {

  // detailPageUrl = "LEARNER" === userType
  //     ? `/test/testDetail/${test.testId}/learner`
  //     : `/test/testDetail/${test.testId}`;

  console.log(qnaData);
  console.log(qnaData.writerId);
  console.log(userId);
  const isOwner = qnaData.writerId === userId;
  const isSecret = qnaData.secretStatus === true;
  let titleTh;
  if (userType === "LEARNER" && isSecret && !isOwner) {
    titleTh = `<td class="align-middle text-center text-muted"><em>비밀글입니다.</em></td>`;
  } else {
    titleTh = `
        <td class="align-middle">
            <a class="qna-detail-page" data-board-no="${qnaData.boardNo}" href="#">
              ${qnaData.title}
            </a>
        </td>
        `;
  }

  return `
    <tr class="text-center">
      <td class="align-middle" scope="col">${qnaData.boardNo}</td>
      ${titleTh}
      <td class="align-middle" scope="col">${qnaData.courseName}</td>
      <td class="align-middle" scope="col">${qnaData.writer}</td>
      <td class="align-middle" scope="col">${qnaData.regDate}</td>
      <td class="align-middle" scope="col">${qnaData.answerStatus}</td>
    </tr>
    `;
}

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
      ".prev-page-group, .page-no, .next-page-group",
      function (e) {

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
// [[검색 & 필터 정렬 이벤트]]
//------------------------------------------------------------------------------

$courseSelect.on("change", function () {

  qnaRequest.currentPageNo = 1;
  qnaRequest.searchOptions.courseName = $(this).val();
  qnaRequest.searchOptions.searchType = "";
  qnaRequest.searchOptions.keyWord = "";
  qnaRequest.searchOptions.answerStatus = "";
  qnaRequest.searchOptions.sortBy = "created_at";
  qnaRequest.searchOptions.sortOrder = "DESC";

  $searchType.val("title");
  $qnaSearchKeyword.val("");
  $answerStatus.val("");
  $sortBy.val("created_at");
  $sortOrder.val("DESC");

  fetchQnA(qnaObjectToQuery(qnaRequest))
  .then((res) => {
    console.log(res.data.data.items);
    renderQnAList(res.data.data.items);
    renderPagination(res.data.data);
  });

});

$qnaSearchBtn.on("click", function () {

  console.log($searchType.val());

  // if (!$qnaSearchKeyword.val().trim()) {
  //     Swal.fire({
  //                   icon             : "warning",
  //                   title            : "검색어를 입력해주세요",
  //                   text             : "검색어 없이 검색할 수 없습니다.",
  //                   confirmButtonText: "확인"
  //               });
  //     return;
  // }

  qnaRequest.currentPageNo = 1;
  qnaRequest.searchOptions.searchType = $searchType.val();
  qnaRequest.searchOptions.keyWord = $qnaSearchKeyword.val().trim();
  fetchQnA(qnaObjectToQuery(qnaRequest))
  .then((res) => {
    console.log(res.data.data.items);
    renderQnAList(res.data.data.items);
    renderPagination(res.data.data);
  });
});

$answerStatus.on("change", function () {

  console.log($(this).val());
  qnaRequest.currentPageNo = 1;
  qnaRequest.searchOptions.answerStatus = $(this).val();
  console.log(qnaRequest);
  fetchQnA(qnaObjectToQuery(qnaRequest))
  .then((res) => {
    console.log(res.data.data.items);
    renderQnAList(res.data.data.items);
    renderPagination(res.data.data);
  });
});

$sortBy.on("change", function () {

  qnaRequest.currentPageNo = 1;
  qnaRequest.searchOptions.sortBy = $(this).val();
  fetchQnA(qnaObjectToQuery(qnaRequest))
  .then((res) => {
    console.log(res.data.data.items);
    renderQnAList(res.data.data.items);
    renderPagination(res.data.data);
  });
});

$sortOrder.on("change", function () {

  qnaRequest.currentPageNo = 1;
  qnaRequest.searchOptions.sortOrder = $(this).val();
  fetchQnA(qnaObjectToQuery(qnaRequest))
  .then((res) => {
    console.log(res.data.data.items);
    renderQnAList(res.data.data.items);
    renderPagination(res.data.data);
  });
});

// 진행상황별 필터 선택시 조건에 맞는 강좌 불러오기 (관리자)
$progressFilter.on("change", function () {

  const isInProgress = $(this).val(); // 진행상황 값

  if (isInProgress === "") {
    // "전체(진행별)"을 클릭했을 경우 => 전체 리스트 가져오기
    $courseFilter.empty().append("<option value=\"\">전체(과정별)</option>");

    qnaRequest.searchOptions.isInProgress = null;
    qnaRequest.searchOptions.courseName = "";

    // 과정별 필터에 모든 과정명 불러오기
    getAdminCourses();
    fetchQnA(qnaObjectToQuery(qnaRequest))
    .then((res) => {
      console.log(res.data.data.items);
      renderQnAList(res.data.data.items);
      renderPagination(res.data.data);
    });
    return;
  }

  qnaRequest.searchOptions.isInProgress = $(this).val();

  getAdminCourses(isInProgress);
});

// 과정별 필터 값을 바꾸었을 때 리스트 불러오기 (관리자)
$courseFilter.on("change", function () {

  qnaRequest.currentPageNo = 1;
  qnaRequest.searchOptions.courseName = $(this).val();
  qnaRequest.searchOptions.searchType = "";
  qnaRequest.searchOptions.keyWord = "";
  qnaRequest.searchOptions.answerStatus = "";
  qnaRequest.searchOptions.sortBy = "created_at";
  qnaRequest.searchOptions.sortOrder = "DESC";

  $searchType.val("title");
  $qnaSearchKeyword.val("");
  $answerStatus.val("");
  $sortBy.val("created_at");
  $sortOrder.val("DESC");

  if ($(this).val() === "") {
    $progressFilter.val("");
  }

  selectedCourse = $(this).val();
  fetchQnA(qnaObjectToQuery(qnaRequest))
  .then((res) => {
    console.log(res.data.data.items);
    renderQnAList(res.data.data.items);
    renderPagination(res.data.data);
  });
});

//------------------------------------------------------------------------------
// [[qna 등록 이벤트]]
//------------------------------------------------------------------------------

function makeQnAQueryString(qnaRequest) {
  const { searchOptions, ...rest } = qnaRequest;
  const params = {};

  // 1) rest(페이지 정보) 중 null/'' 제외
  Object.entries(rest).forEach(([key, val]) => {
    if (val !== null && val !== undefined && val !== "") {
      params[key] = val;
    }
  });

  // 2) searchOptions 중 null/'' 제외
  Object.entries(searchOptions).forEach(([key, val]) => {
    // boolean(false)도 값이 있으므로 포함, 빈 문자열만 제외
    if (val !== null && val !== undefined &&
        !(typeof val === "string" && val.trim() === "")) {
      params[`searchOptions.${key}`] = val;
    }
  });

  return params;
}

$qnaRegisterBtn.on("click", function () {

  const queryString = $.param(makeQnAQueryString(qnaRequest));
  console.log(queryString);

  window.location.href = `/courseBoardQnA/register?${queryString}`;
});

$(document).on("click", ".qna-detail-page", function (e) {
  e.preventDefault();

  const boardNo = $(this).data("board-no");
  const queryString = $.param(makeQnAQueryString(qnaRequest));
  console.log(queryString);

  window.location.href = `/courseBoardQnA/detail/${boardNo}?${queryString}`;
});