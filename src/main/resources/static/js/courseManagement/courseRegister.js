const loginUserId   = $("#login-user-id").val();
const loginUserType = $("#login-user-type").val();
let isInProgress    = null;

const config = {
    pageNo        : 1,
    pageSize      : 10,
    type          : "name",
    keyword       : null,
    orderBy       : "id",
    orderDirection: "DESC",
};

$(document).ready(function () {
    $(document).on("click", "#load-course", handleLoadCourseClick);
    // 과정
    $(document).on("click", ".load-course-select", fetchAndInsertDetail);

    $("#search-button").on("click", handleSearchChange);
    $(document).on("keydown", "#search-input", function (e) {
        if (e.key == "Enter") {
            e.preventDefault();
            handleSearchChange();
        }
    });
    $(document).on("click", ".page-link", handlePageBtnClick);

    // 강사 셀렉트 박스 불러오기
    $.ajax({
               url     : "/courseRegister/getNotAssignmentInstructor", // 데이터가
                                                                       // 송수신될
                                                                       // 서버의
                                                                       // 주소
               type    : "GET", // 통신 방식 (GET, POST, PUT, DELETE)
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               // data       : JSON.stringify(courseId),
               // contentType: "application/json; charset=utf-8",
               // Content-Type헤더가 application/x-www-form-urlencoded;
               // charset=UTF-8로 자동 설정되는 것을 방지
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   console.log(data);

                   let output = ``;
                   output += `<option value="">강사선택</option>`;

                   $(data).each(function (index, item) {
                       output += `<option value="${item.id}">${item.fullName}</option>`;
                   });

                   $("#instructor").html(output);

               },
               error   : function () {
               },
               complete: function () {
               }
           });

    // 관리자 셀렉트 박스 불러오기
    $.ajax({
               url     : "/courseRegister/getCourseHead", // 데이터가 송수신될 서버의 주소
               type    : "GET", // 통신 방식 (GET, POST, PUT, DELETE)
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               // data       : JSON.stringify(courseId),
               // contentType: "application/json; charset=utf-8",
               // Content-Type헤더가 application/x-www-form-urlencoded;
               // charset=UTF-8로 자동 설정되는 것을 방지
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   console.log(data);

                   let output = ``;
                   output += `<option value="">관리자선택</option>`;

                   $(data).each(function (index, item) {
                       output += `<option value="${item.id}">${item.fullName}</option>`;
                   });

                   $("#administrator").html(output);

               },
               error   : function () {
               },
               complete: function () {
               }
           });

});

function handleLoadCourseClick() {
    fetchAndDisplayView();
}

async function fetchAndDisplayView() {

    let coursesWithPagination = await apiGetRequest(
        "/api/courses/all",
        {
            loginUserId  : loginUserId,
            loginUserType: loginUserType,
            isInProgress : isInProgress
        });
    let courses               = coursesWithPagination?.respDTOS || [];
    if (!Array.isArray(courses)) {
        courses = [];
    }
    renderCourseTable(courses);
    renderCoursePagination(coursesWithPagination);
}

async function fetchAndInsertDetail() {

    let courseId = $(this).data("id");
    console.log(courseId);
    let course = await apiGetRequest(
        "/api/course",
        {
            loginUserId  : loginUserId,
            loginUserType: loginUserType,
            courseId     : courseId
        });
    // let course = coursesWithPagination?.respDTOS || [];
    console.log(course);
    insertContentBySelect(course);
}

async function apiGetRequest(endpoint, additionalParams = {}) {
    try {
        const response = await axios.get(endpoint, {
            params: {...config, ...additionalParams}
        });
        console.log(response.data);
        return response.data.data;
    } catch (error) {
        console.error(`${endpoint} 요청 오류:`, error);
        return [];
    }
}

function renderCourseTable(courses) {
    $("#table-body").empty();
    courses.forEach(function (course) {
        let rowHtml = `
        <tr>
          <td class="text-center align-middle">${course.isInProgress ? "진행 중"
                                                                     : "종료"}</td>
          <td class="title align-middle">${course.name}</td>
          <td class="text-center align-middle">${course.startDate} ~ ${course.endDate}</td>
          <td class="text-center align-middle">${course.totalHours}시간</td>
          <td class="text-center align-middle">${course.totalDays}시간</td>
          <td class="text-center align-middle">${course.dailyHours}시간</td>
          <td class="text-center align-middle">
            <button class="btn btn-primary btn-icon-split btn-sm load-course-select" data-id="${course.id}">
              <span class="text" data-dismiss="modal">선택</a></span>
            </button>
          </td>
        </tr>
      `;
        $("#table-body").append(rowHtml);
    });
}

function renderCoursePagination(data) {
    if (data.totalRecords == 0) {
        $("#course-pagination").html("");
        return;
    }
    let output   = `<ul class="pagination justify-content-center" style="margin:20px 0">`;
    let prevPage = data.pageNo > 1 ? data.pageNo - 1 : 1;
    // 이전 버튼
    output += `
    <li class="page-item ${data.pageNo == 1 ? "disabled" : ""}">
      <a class="page-link page-btn" href="#" data-page="${prevPage}">이전</a>
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
    let nextPage = data.pageNo < data.blockEndPage ? data.pageNo + 1
                                                   : data.lastPage;
    output += `
    <li class="page-item ${data.pageNo == data.lastPage ? "disabled" : ""}">
      <a class="page-link page-btn" href="#" data-page="${nextPage}">다음</a>
    </li></ul>`;
    $("#course-pagination").html(output);
}

async function insertContentBySelect(course) {

    $("#name-input").val(course.name);
    $("#number-of-learner-input").val(course.numberOfLearner);
    $("#total-hours-input").val(course.totalHours);
    $("#daily-hours-input").val(course.dailyHours);
    $("#break-time-input").val(course.breakTime);
    $("#lesson-start-time-input").val(course.lessonStartTime);
    $("#lesson-end-time-input").val(course.lessonEndTime);
    $("#lunch-start-time-input").val(course.lunchStartTime);
    $("#lunch-end-time-input").val(course.lunchEndTime);

    $("#subject-table").empty();

    course.subjects.forEach(subject => {
        let rowHtml = `
      <tr>
        <td class="text-center align-middle"><input type="text" value="${subject.subjectOrder}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.name}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.hours}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.textbookName}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.textbookAuthor}" class="form-control" data-id="${subject.id}"></td>
      </tr>
    `;
        $("#subject-table").append(rowHtml);
    });
}

function handleSearchChange() {
    config.keyword = $("#search-input").val();
    isInProgress   = null; // 전체에서 검색
    config.pageNo  = 1; // 검색결과 1페이지 보여주기
    $("#is-in-progress").val("all");
    fetchAndDisplayView();
}

function handlePageBtnClick() {
    config.pageNo = $(this).data("page");
    fetchAndDisplayView();
}
