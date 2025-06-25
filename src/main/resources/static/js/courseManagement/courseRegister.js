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

let holidayArr  = [];
let scheduleArr = [];

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
               url: "/courseRegister/getNotAssignmentInstructor", // 데이터가
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

    // 강의실 셀렉트 박스 불러오기
    $.ajax({
               url     : "/courseRegister/getClassroom", // 데이터가 송수신될 서버의 주소
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
                   output += `<option value="">강의실선택</option>`;

                   $(data).each(function (index, item) {
                       output += `<option value="${item.id}">${item.name}</option>`;
                   });

                   $("#classroom").html(output);

               },
               error   : function () {
               },
               complete: function () {
               }
           });

    // 휴강일 리스트 불러오기
    $.ajax({
               url     : "/courseRegister/getCancelDates", // 데이터가 송수신될 서버의 주소
               type    : "GET", // 통신 방식 (GET, POST, PUT, DELETE)
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               // data       : JSON.stringify(courseId),
               // contentType: "application/json; charset=utf-8",
               // Content-Type헤더가 application/x-www-form-urlencoded;
               // charset=UTF-8로 자동 설정되는 것을 방지
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   holidayArr = data;
                   console.log(holidayArr);

               },
               error   : function () {
               },
               complete: function () {
               }
           });

    $("#cancel-btn").click(function(e){
        e.preventDefault();
       location.href = "/courseManagement/courseList";
    });

    $("#total-hours-input").blur(function () {

        clearErr(this);

        if (!checkInt($(this).val())) {
            showErr(this, "잘못된 입력입니다.");
        }
        calTotalDays();
        calEndDate();

    });

    $("#daily-hours-input").blur(function () {

        clearErr(this);

        if (!checkInt($(this).val())) {
            showErr(this, "잘못된 입력입니다.");
        }
        calTotalDays();
        calEndDate();
        showWhenLunch();
    });

    $("#break-time-input").blur(function () {
        $("#lunch-start-time-input").val("");
        $("#lunch-end-time-input").val("");
        clearErr(this);
        let breakTime = $("#break-time-input").val();
        if (breakTime >= 60 || !checkInt(breakTime)) {
            showErr(this, "잘못된 입력입니다.");
            return;
        }
        if ($("#when-lunch").val() == "") {
            return;
        }
        calLunchTime();

    });

    $("#start-date-input").change(function () {

        clearErr(this);
        $("#end-date-input").val("");
        scheduleArr = [];

        let startDate = new Date($("#start-date-input").val());
        let isHoliday = false;
        $(holidayArr).each(function (index, item) {
            if (startDate.getTime() ==
                new Date(item.cancelDate).getTime()) {
                isHoliday = true;
            }
        });

        if (startDate.getDay() == 6 || startDate.getDay() == 0) {
            isHoliday = true;
        }
        if (isHoliday) {
            showErr(this, "휴일은 입력할 수 없습니다.");
            $(this).val("");
            return;
        }
        calEndDate();
    });

    $("#lesson-start-time-input").change(function () {

        $("#lesson-end-time-input").val("");
        clearErr(this);
        let totalDays = $("#total-days-input").val();
        if (totalDays == "") {
            showErr(this, "총훈련시간과 일일훈련시간을 먼저 입력해주세요.");
            return;
        }
        let lessonStartTime   = $(this).val();
        let dailyHours        = $("#daily-hours-input").val();
        let lessonStartHour   = Number(lessonStartTime.split(":")[0]);
        let lessonStartMinute = Number(lessonStartTime.split(":")[1]);
        let lessonMinute      = lessonStartHour * 60 + lessonStartMinute +
                                (Number(dailyHours) * 60) + 50;
        let lessonEndHour     = String(Math.floor(lessonMinute / 60))
        .padStart(2, "0");
        let lessonEndMinute   = String(lessonMinute % 60).padStart(2, "0");
        let lessonEndTime     = lessonEndHour + ":" + lessonEndMinute;
        if (Number(lessonEndTime.split(":")[0]) >= 24) {
            showErr(this, "잘못된 입력입니다");
            return;
        }
        $("#lesson-end-time-input").val(lessonEndTime);

    });

    $("#when-lunch").change(function () {

        $("#lunch-start-time-input").val("");
        $("#lunch-end-time-input").val("");
        clearErr(this);
        let lessonEndTime = $("#lesson-end-time-input").val();
        let breakTime     = $("#break-time-input").val();
        if (lessonEndTime == "") {
            showErr(this, "훈련시간을 먼저 입력해 주세요.");
            return;
        }
        if (breakTime == "") {
            showErr(this, "쉬는시간을 먼저 입력해 주세요.");
            return;
        }
        calLunchTime();

    });

    $("body").on("click", ".remove-subject-row-btn", function (e) {
        e.preventDefault();
        $(this).closest(".subject-row").remove();
    });

    $("#add-subject-row-btn").click(function (e) {
        e.preventDefault();
        let output = `
        <tr class="subject-row">
                  <td class="text-center align-middle"><input
                      type="text"
                      class="form-control"
                      data-id=""></td>
                  <td class="text-center align-middle"><input
                      type="text"
                      class="form-control"
                      data-id=""></td>
                  <td class="text-center align-middle"><input
                      type="text"
                      class="form-control"
                      data-id=""></td>
                  <td class="text-center align-middle"><input
                      type="text"
                      class="form-control"
                      data-id=""></td>
                  <td class="text-center align-middle"><input
                      type="text"
                      class="form-control"
                      data-id=""></td>
                  <td class="text-center align-middle"><button
                      class='btn btn-danger btn-icon-split btn-sm remove-subject-row-btn'><span
                      class='text'>삭제</span></button></td>
        </tr>
        `;
        $("#subject-table").append(output);
    });
});

function calLunchTime() {

    let lessonStartTime      = $("#lesson-start-time-input").val();
    let lessonEndTime        = $("#lesson-end-time-input").val();
    let breakTime            = $("#break-time-input").val();
    let lunchStartTimeMinute = Number(lessonStartTime.split(":")[0]) * 60 +
                               Number(lessonStartTime.split(":")[1]) +
                               Number($("#when-lunch").val()) * 60 -
                               Number(breakTime);
    let lunchStartTime       = String(Math.floor(lunchStartTimeMinute / 60))
                               .padStart(2, "0") + ":" +
                               String(lunchStartTimeMinute % 60)
                               .padStart(2, "0");
    let lunchStartHour       = Number(lunchStartTime.split(":")[0]);
    let lunchStartMinute     = Number(lunchStartTime.split(":")[1]);
    let lunchMinute          = lunchStartHour * 60 + lunchStartMinute + 60 +
                               Number(breakTime);
    let lunchEndHour         = String(Math.floor(lunchMinute / 60))
    .padStart(2, "0");
    let lunchEndMinute       = String(lunchMinute % 60).padStart(2, "0");
    let lunchEndTime         = lunchEndHour + ":" + lunchEndMinute;
    if (lunchStartTime < lessonStartTime || lunchEndTime > lessonEndTime) {
        showErr(this, "잘못된 입력입니다.");
        return;
    }
    $("#lunch-start-time-input").val(lunchStartTime);
    $("#lunch-end-time-input").val(lunchEndTime);
}

function calEndDate() {

    scheduleArr   = [];
    let startDate = $("#start-date-input").val();
    let totalDays = $("#total-days-input").val();
    let endDate   = new Date(startDate);
    scheduleArr.push(changeDateToString(endDate));

    if (totalDays == "" || startDate == "") {
        return;
    }

    while (totalDays > 1) {

        endDate.setDate(endDate.getDate() + 1);

        let isHoliday = false;

        if (endDate.getDay() == 6 || endDate.getDay() == 0) {
            continue;

        }

        $(holidayArr).each(function (index, item) {
            if (endDate.getTime() == new Date(item.cancelDate).getTime()) {
                isHoliday = true;
            }
        });

        if (isHoliday) {
            continue;
        }

        scheduleArr.push(changeDateToString(endDate));
        totalDays--;

    }
    let endDateStr = changeDateToString(endDate);
    $("#end-date-input").val(endDateStr);
    console.log(scheduleArr);
}

function showWhenLunch() {
    let dailyHours = $("#daily-hours-input").val();
    let output     = ``;
    output += `<option value>선택</option>`;
    for (let i = 1; i <= dailyHours; i++) {
        output += `<option value="${i}">${i}교시 후</option>`;
    }
    $("#when-lunch").html(output);
}

function changeDateToString(date) {

    return date.getFullYear() + "-" +
           String(date.getMonth() + 1).padStart(2, "0") +
           "-" +
           String(date.getDate()).padStart(2, "0");
}

function calTotalDays() {
    // $.noConflict(true);
    $("#total-days-input").val("");
    // $.noConflict(false);

    let dailyHours = $("#daily-hours-input").val();
    let totalHours = $("#total-hours-input").val();
    if (dailyHours == "" || totalHours == "") {
        return;
    }
    clearErr("#total-days-input");

    if (!checkInt(totalHours / dailyHours)) {
        showErr("#total-days-input", "총훈련시간이나 일일훈련시간이 올바르지 않습니다.");

    } else {
        $("#total-days-input").val(totalHours / dailyHours);
    }

}

function clearErr(nextErr) {
    if ($(nextErr).prev().hasClass("errMsg")) {
        $(nextErr).prev().empty();
    }
}

function showErr(nextErr, errMsg) {

    if (!$(nextErr).prev().hasClass("errMsg")) {
        $(nextErr).before("<span class='text-danger small errMsg'></span>");
    }
    $(nextErr).prev().text(errMsg);
}

function checkInt(data) {

    let result = false;

    if (data % 1 == 0 && data >= 1) {
        result = true;
    }

    return result;
}

function handleLoadCourseClick() {
    fetchAndDisplayView();
}

async function fetchAndDisplayView() {

    let coursesWithPagination = await apiGetRequest(
        "/api/management/courses",
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
    let coursesWithPagination = await apiGetRequest(
        "/api/management/courses",
        {
            loginUserId  : loginUserId,
            loginUserType: loginUserType,
            courseId     : courseId
        });
    let course = coursesWithPagination?.respDTOS || [];
    console.log(course);
    insertContentBySelect(course[0]);
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
    // 기록이 없을 때, 페이지네이션도 표시되지 않음
    if (data.totalRecords == 0) {
        $("#course-pagination").html("");
        return;
    }

    let output = `<ul class="pagination justify-content-center" style="margin:20px 0">`;

    // 이전 버튼
    let prevBlockPage = data.blockStartPage > 1 ? data.blockStartPage - 1 : 1;
    output += `
    <li class="page-item ${data.blockStartPage == 1 ? "disabled" : ""}">
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
    let nextBlockPage = data.blockEndPage < data.lastPage ? data.blockEndPage +
                                                            1 : data.lastPage;
    output += `
    <li class="page-item ${data.blockEndPage == data.lastPage ? "disabled" : ""}">
      <a class="page-link page-btn" href="#" data-page="${nextBlockPage}">다음</a>
    </li></ul>`;

    $("#course-pagination").html(output);
}

async function insertContentBySelect(course) {

    $("#name-input").val(course.name);
    $("#number-of-learner-input").val(course.numberOfLearner);
    $("#total-hours-input").val(course.totalHours);
    $("#daily-hours-input").val(course.dailyHours);
    $("#total-days-input").val(course.totalDays);
    $("#break-time-input").val(course.breakTime);
    $("#lesson-start-time-input").val(course.lessonStartTime);
    $("#lesson-end-time-input").val(course.lessonEndTime);
    $("#lunch-start-time-input").val(course.lunchStartTime);
    $("#lunch-end-time-input").val(course.lunchEndTime);

    $("#subject-table").empty();

    course.subjects.forEach(subject => {
        let rowHtml = `
      <tr class="subject-row">
        <td class="text-center align-middle"><input type="text" value="${subject.subjectOrder}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.name}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.hours}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.textbookName}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><input type="text" value="${subject.textbookAuthor}" class="form-control" data-id="${subject.id}"></td>
        <td class="text-center align-middle"><button class='btn btn-danger btn-icon-split btn-sm remove-subject-row-btn'><span class='text'>삭제</span></button></td>
      </tr>
    `;
        $("#subject-table").append(rowHtml);
    });

    showWhenLunch();
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

function registerCourse(e) {
    e.preventDefault();

    let subjectArr = [];
    $(".subject-row").each(function (index, item) {
        let subjectRowData = {
            subjectOrder  : $(item).find("input").eq(0).val(),
            name          : $(item).find("input").eq(1).val(),
            hours         : $(item).find("input").eq(2).val(),
            textbookName  : $(item).find("input").eq(3).val(),
            textbookAuthor: $(item).find("input").eq(4).val()
        };
        subjectArr.push(subjectRowData);
    });

    let courseData = {
        name           : $("#name-input").val(),
        numberOfLearner: $("#number-of-learner-input").val(),
        startDate      : $("#start-date-input").val(),
        endDate        : $("#end-date-input").val(),
        totalHours     : $("#total-hours-input").val(),
        totalDays      : $("#total-days-input").val(),
        dailyHours     : $("#daily-hours-input").val(),
        breakTime      : $("#break-time-input").val(),
        lessonStartTime: $("#lesson-start-time-input").val(),
        lessonEndTime  : $("#lesson-end-time-input").val(),
        lunchStartTime : $("#lunch-start-time-input").val(),
        lunchEndTime   : $("#lunch-end-time-input").val(),
        lessonDays     : scheduleArr,
        instructorId   : $("#instructor").val(),
        administratorId: $("#administrator").val(),
        classroomId    : $("#classroom").val(),
        subjects       : subjectArr

    };

    if (checkValid()) {

        $.ajax({
                   url: "/courseRegister/saveCourse", // 데이터가 송수신될 서버의
                                                      // 주소
                   type       : "POST", // 통신 방식 (GET, POST, PUT, DELETE)
                   dataType   : "text", // 수신받을 데이터의 타입 (MIME TYPE)
                   data       : JSON.stringify(courseData),
                   contentType: "application/json; charset=utf-8",
                   // Content-Type헤더가 application/x-www-form-urlencoded;
                   // charset=UTF-8로 자동 설정되는 것을 방지
                   async   : false, // 비동기옵션 off
                   success : function (data) { // 통신이 성공하면 수행할 함수

                       console.log(data);
                       location.href = "/courseManagement/courseList";
                   },
                   error   : function () {
                   },
                   complete: function () {
                   }
               });
    }

}

function checkValid() {
    let result = true;
    $(".errMsg").empty();

    if ($("#name-input").val() == "") {
        showErr("#name-input", "과정명은 필수입니다.");
        result = false;

    } else {

        $.ajax({
                   url: "/courseRegister/checkNameDuplicate", // 데이터가 송수신될 서버의
                                                      // 주소
                   type       : "GET", // 통신 방식 (GET, POST, PUT, DELETE)
                   dataType   : "text", // 수신받을 데이터의 타입 (MIME TYPE)
                   data       : {
                      name: $("#name-input").val()
                   },
                   // contentType: "application/json; charset=utf-8",
                   // Content-Type헤더가 application/x-www-form-urlencoded;
                   // charset=UTF-8로 자동 설정되는 것을 방지
                   async   : false, // 비동기옵션 off
                   success : function (data) { // 통신이 성공하면 수행할 함수

                       if(data == "duplicateName"){
                           showErr("#name-input", "중복된 과정명은 사용할 수 없습니다.")
                           result = false;
                       }
                   },
                   error   : function () {
                   },
                   complete: function () {
                   }
               });
    }

    if ($("#number-of-learner-input").val() == "") {
        showErr("#number-of-learner-input", "등록인원은 필수입니다.");
        result = false;
    }
    if (!checkInt(Number($("#number-of-learner-input").val()))) {
        showErr("#number-of-learner-input", "잘못된 입력입니다.");
        result = false;
    }

    if ($("#break-time-input").val() == "" ||
        !checkInt(Number($("#break-time-input").val())) ||
        Number($("#break-time-input").val()) >= 60) {

        showErr("#break-time-input", "잘못된 입력입니다.");
        result = false;
    }

    if ($("#total-days-input").val() == "") {
        showErr("#total-days-input", "잘못된 입력입니다.");
        result = false;
    }

    if ($("#end-date-input").val() == "") {
        showErr("#end-date-input", "잘못된 입력입니다.");
        result = false;
    }

    if ($("#lesson-end-time-input").val() == "") {
        showErr("#lesson-end-time-input", "잘못된 입력입니다.");
        result = false;
    }

    if ($("#lunch-end-time-input").val() == "") {
        showErr("#lunch-end-time-input", "잘못된 입력입니다.");
        result = false;
    }

    if ($("#instructor").val() == "") {
        showErr("#instructor", "강사는 필수입니다.");
        result = false;
    }

    if ($("#administrator").val() == "") {
        showErr("#administrator", "관리자는 필수입니다.");
        result = false;
    }

    if ($("#classroom").val() == "") {
        showErr("#classroom", "강의실은 필수입니다.");
        result = false;
    }

    // 비었는지 확인하고
    // 중복여부확인하고
    // 배열에서 하나씩 팝
    let orderArr            = [];
    let inputOrderArr       = [];
    let totalSubjectHours   = 0;
    let isDuplicate         = false;
    let isValidOrder        = false;
    let isBlank             = false;
    let isValidSubjectHours = false;
    for (let i = 1; i <= $(".subject-row").length; i++) {
        orderArr.push(i);
    }

    $(".subject-row").each(function (index, item) {

        // 비었는지 검사
        let inputArr = $(item).find("input");
        $(inputArr).each(function (index, item) {

            if ($(item).val() == "") {
                isBlank = true;
            }
        });

        totalSubjectHours += Number($(inputArr[2]).val());
        inputOrderArr.push($(inputArr[0]).val());
    });

    // 중복검사
    for (let i = 0; i < inputOrderArr.length - 1; i++) {
        for (let j = i + 1; j < inputOrderArr.length; j++) {
            if (inputOrderArr[i] == inputOrderArr[j]) {
                isDuplicate = true;
            }
        }
    }

    // 배열팝
    for (let i = orderArr.length - 1; i >= 0; i--) {
        for (let j = 0; j < inputOrderArr.length; j++) {
            if (orderArr[i] == inputOrderArr[j]) {
                orderArr.splice(i, 1);
            }
        }
    }

    if (orderArr.length == 0) {
        isValidOrder = true;
    }

    if (totalSubjectHours == Number($("#total-hours-input").val())) {
        isValidSubjectHours = true;
    }

    if (isDuplicate || isBlank || !isValidOrder || !isValidSubjectHours) {
        showErr("#add-subject-row-btn", "잘못된 입력입니다.");
        result = false;
    }

    console.log(result);
    return result;
}

