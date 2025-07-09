let selectCourse;
let weekNum;
let weekRange;
let inProgressType = -1;
const today = new Date();
const params = new URLSearchParams(window.location.search);

$(function(){

    // courseSelector에 이벤트 걸기 & courseSelector 리스트 불러오기
    if(loginUser.type == "INSTRUCTOR" || loginUser.type == "LEARNER"){

        $("#courseSelector").change(function(){
            let selectedOption = $("#courseSelector option:selected");
            selectCourse = $(selectedOption).data("course");
            let todayStr = [today.getFullYear(),
                            String(today.getMonth() + 1).padStart(2, "0"),
                            String(today.getDate()).padStart(2, "0")].join("-");
            weekNum = calWeekNumber(selectCourse.startDate, selectCourse.endDate, todayStr);
            weekRange = calDateRangeOfWeek(selectCourse.startDate, selectCourse.endDate, weekNum);
            makeScheduleTable(selectCourse, weekRange);
            getAndShowSchedules(selectCourse, weekRange);
            $("#week-num").text(`${weekNum}주차`);
        })

        $.ajax({
                   url: "/courseManagement/courseSchedule/getCoursesByUser",
                   type    : "GET",
                   dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
                   // data       : JSON.stringify(courseId),
                   // contentType: "application/json; charset=utf-8",
                   async   : false, // 비동기옵션 off
                   success : function (data) { // 통신이 성공하면 수행할 함수


                       let output = ``;
                       $(data).each(function(index, item){
                          output += `<option value="${item.id}" data-course='${JSON.stringify(item)}'>${item.name}</option>>`;
                       });
                       $("#courseSelector").html(output);

                   },
                   error   : function () {
                   },
                   complete: function () {
                   }
               });
    }

    // 관리자 분류 메뉴에 이벤트 걸기 & 리스트 불러오기
    if(loginUser.type == "ADMINISTRATOR"){

        getCoursesByInProgressType();

        $("#in-progress-type").change(function(){
            inProgressType = $(this).val();
            getCoursesByInProgressType();

        });

        $("#course-type").change(function(){
            let selectedOption = $("#course-type option:selected");
            selectCourse = $(selectedOption).data("course");
            let todayStr = [today.getFullYear(),
                            String(today.getMonth() + 1).padStart(2, "0"),
                            String(today.getDate()).padStart(2, "0")].join("-");
            weekNum = calWeekNumber(selectCourse.startDate, selectCourse.endDate, todayStr);
            weekRange = calDateRangeOfWeek(selectCourse.startDate, selectCourse.endDate, weekNum);
            makeScheduleTable(selectCourse, weekRange);
            getAndShowSchedules(selectCourse, weekRange);
            $("#week-num").text(`${weekNum}주차`);
        });
    }


    // 첫 화면에 띄울 과정 결정
    if(loginUser.type == "ADMINISTRATOR" && params.get("courseId") != null && params.get("courseId") != ""){

        $.ajax({
                   url: "/courseManagement/courseSchedule/getCourseByIdAndUser",
                   type    : "GET",
                   dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
                   data       : {
                       courseId: params.get("courseId")
                   },
                   // contentType: "application/json; charset=utf-8",
                   async   : false, // 비동기옵션 off
                   success : function (data) { // 통신이 성공하면 수행할 함수


                       if(data.id != 0){
                           selectCourse = data;
                           let todayStr = [today.getFullYear(),
                                           String(today.getMonth() + 1).padStart(2, "0"),
                                           String(today.getDate()).padStart(2, "0")].join("-");
                           weekNum = calWeekNumber(selectCourse.startDate, selectCourse.endDate, todayStr);
                           weekRange = calDateRangeOfWeek(selectCourse.startDate, selectCourse.endDate, weekNum);
                           makeScheduleTable(selectCourse, weekRange);
                           getAndShowSchedules(selectCourse, weekRange);
                           $("#week-num").text(`${weekNum}주차`);
                           $("#courseSelector").val(data.id);
                           $("#course-type").val(data.id);
                           // tmpCourse = data; // 교육생, 강사 코스선택 바인드
                       }
                       // data가 널일시(배정 한번도 없는 관리자)체크
                       // console.log(data);

                   },
                   error   : function () {
                   },
                   complete: function () {
                   }
               });


    } else {

        selectFirstCourse();

    }


    $("#prev-btn").click(function(){
        if(weekNum <= 1){
            return;
        }
        weekNum--;
        weekRange = calDateRangeOfWeek(selectCourse.startDate, selectCourse.endDate, weekNum);
        makeScheduleTable(selectCourse, weekRange);
        getAndShowSchedules(selectCourse, weekRange);
        $("#week-num").text(`${weekNum}주차`);

    });

    $("#next-btn").click(function(){
        if(weekNum >= calTotalWeeks(selectCourse.startDate, selectCourse.endDate)){
            return;
        }
        weekNum++;
        weekRange = calDateRangeOfWeek(selectCourse.startDate, selectCourse.endDate, weekNum);
        makeScheduleTable(selectCourse, weekRange);
        getAndShowSchedules(selectCourse, weekRange);
        $("#week-num").text(`${weekNum}주차`);
    });

});

// 특정 날짜가 몇 주차인지 계산
function calWeekNumber(startDateStr, endDateStr, targetDateStr) {
    const startDate = new Date(startDateStr);
    const endDate = new Date(endDateStr);
    const targetDate = new Date(targetDateStr);

    if (targetDate < startDate){
        return 1;
    }
    else if (targetDate > endDate) {
        return calTotalWeeks(startDateStr, endDateStr);
    }

    // 1. 과정 시작일이 포함된 주의 일요일 찾기
    const startDay = startDate.getDay(); // 0: 일, 1: 월, ..., 6: 토
    const firstWeekStart = new Date(startDate);
    firstWeekStart.setDate(startDate.getDate() - startDay); // 일요일

    // 2. targetDate가 몇 주 후에 있는지 계산
    const diffDays = Math.floor((targetDate - firstWeekStart) / (1000 * 60 * 60 * 24));
    const weekNumber = Math.floor(diffDays / 7) + 1;

    return weekNumber;
}

// N주차의 날짜 범위 구하기
function calDateRangeOfWeek(startDateStr, endDateStr, weekNumber) {
    const startDate = new Date(startDateStr);
    const endDate = new Date(endDateStr);

    const startDay = startDate.getDay();
    const firstWeekStart = new Date(startDate);
    firstWeekStart.setDate(startDate.getDate() - startDay); // 첫 주의 일요일

    // N주차의 월요일 ~ 금요일 계산
    const weekStart = new Date(firstWeekStart);
    weekStart.setDate(firstWeekStart.getDate() + (weekNumber - 1) * 7 + 1);

    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 4);

    // 종료일 넘어가면 조정
    // const rangeStart = weekStart < startDate ? startDate : weekStart;
    // const rangeEnd = weekEnd > endDate ? endDate : weekEnd;

    const format = d => d.toISOString().slice(0, 10); // yyyy-mm-dd

    let days = [];
    for (let d = new Date(weekStart); d <= weekEnd; d.setDate(d.getDate() + 1)){
        days.push(format(new Date(d)));
    }

    return days;
}

// 총 몇주차짜리 과정인지 계산
function calTotalWeeks(startDateStr, endDateStr) {
    const startDate = new Date(startDateStr);
    const endDate = new Date(endDateStr);

    if (isNaN(startDate) || isNaN(endDate) || startDate > endDate) {
        throw new Error("유효한 날짜를 입력하세요. 시작일은 종료일보다 빨라야 합니다.");
    }

    // 첫 주차는 시작일부터 그 주의 토요일까지
    const startDay = startDate.getDay(); // 0: 일요일, ..., 6: 토요일
    const daysToSaturday = 6 - startDay;

    const firstWeekEnd = new Date(startDate);
    firstWeekEnd.setDate(startDate.getDate() + daysToSaturday);

    if (firstWeekEnd >= endDate) {
        return 1; // 종료일이 첫 주 안에 포함된다면 1주차로 끝
    }

    // 나머지 일수 계산
    const remainingStart = new Date(firstWeekEnd);
    remainingStart.setDate(firstWeekEnd.getDate() + 1); // 다음 주 일요일

    // 총 남은 일 수 계산
    const msPerDay = 1000 * 60 * 60 * 24;
    const remainingDays = Math.ceil((endDate - remainingStart) / msPerDay) + 1;

    // 전체 남은 주 수 계산
    const fullWeeks = Math.ceil(remainingDays / 7);

    return 1 + fullWeeks; // 첫 주차 포함
}

function getAndShowSchedules(course, weekRange){

    $.ajax({
               url: "/courseManagement/courseSchedule/getCourseScheduleByWeekRange",
               type    : "GET",
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               data       : {
                   courseId : course.id,
                   weekStart : weekRange[0],
                   weekEnd : weekRange[4]
                                           },
               // contentType: "application/json; charset=utf-8",
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   if(data.length >= course.dailyHours){
                       for(let i = 0; i <= course.dailyHours - 1; i++){
                           $(`#${i+1}-period-time`).text(`${(data[i].periodStartTime).split(":").slice(0, 2).join(":")} ~ ${(data[i].periodEndTime).split(":").slice(0, 2).join(":")}`)
                       }
                   }

                   $(data).each(function(index, item){
                       $(`#date-${item.classDate}-period-${item.period}`).find(".subject-name").text(`<${item.subjectName}>`);
                       $(`#date-${item.classDate}-period-${item.period}`).find(".classroom-name").text(`(${item.classroomName})`);
                       $(`#date-${item.classDate}-period-${item.period}`).find(".instructor-name").text(`${item.instructorName} 강사님`);

                   });



               },
               error   : function () {
               },
               complete: function () {
               }
           });

    if(weekNum <= 1){
        $("#prev-btn").hide();
    } else {
        $("#prev-btn").show();
    }

    if(weekNum >= calTotalWeeks(selectCourse.startDate, selectCourse.endDate)){
        $("#next-btn").hide();
    } else {
        $("#next-btn").show();
    }


}

// 주차 누를때마다 다시 만들자
function makeScheduleTable(course, weekRange){

    let dailyHours = course.dailyHours;

    for(let i = 0; i < 5; i++){
        $(".th-date").eq(i).html(weekRange[i]);
    }

    let output = ``;

    for(let i = 1; i <= dailyHours; i++){

        output += `<tr><td class="text-center align-middle">${i}교시<p id="${i}-period-time">(09:30 ~ 10:20)</p></td>`;

        $(weekRange).each(function(index, item){
            output += `<td id="date-${item}-period-${i}" class="text-center align-middle pt-3">
              <p class="mb-0 subject-name"></p>
              <p class="classroom-name"></p>
              <p class="mb-0 instructor-name"></p>
            </td>`;
        });

        output += `</tr>`;

    }

    $("#schedule-table").html(output);


}

// inProgressType에 따라 과정별 리스트 불러오기
function getCoursesByInProgressType(){
    $.ajax({
               url: "/courseManagement/courseSchedule/getCoursesByInProgressAndLoginUser",
               type    : "GET",
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               data       : {
                   inProgressType: inProgressType
               },
               // contentType: "application/json; charset=utf-8",
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   // console.log("확인");
                   // console.log(data);
                   let output = `<option value="-1">과정별</option>`;
                   $(data).each(function(index, item){
                       output += `<option value="${item.id}" data-course='${JSON.stringify(item)}'>${item.name}</option>`

                   });
                   $("#course-type").html(output);

               },
               error   : function () {
               },
               complete: function () {
               }
           });
}

// 첫 화면에 띄울 과정 결정
function selectFirstCourse(){

    $.ajax({
               url: "/courseManagement/courseSchedule/getFirstCourseByUser",
               type    : "GET",
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               // data       : JSON.stringify(courseId),
               // contentType: "application/json; charset=utf-8",
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수


                   if(data.id != 0){
                       selectCourse = data;
                       let todayStr = [today.getFullYear(),
                                       String(today.getMonth() + 1).padStart(2, "0"),
                                       String(today.getDate()).padStart(2, "0")].join("-");
                       weekNum = calWeekNumber(selectCourse.startDate, selectCourse.endDate, todayStr);
                       weekRange = calDateRangeOfWeek(selectCourse.startDate, selectCourse.endDate, weekNum);
                       makeScheduleTable(selectCourse, weekRange);
                       getAndShowSchedules(selectCourse, weekRange);
                       $("#week-num").text(`${weekNum}주차`);
                       $("#courseSelector").val(data.id);
                       $("#course-type").val(data.id);
                       // tmpCourse = data; // 교육생, 강사 코스선택 바인드

                   }


                   // data가 널일시(배정 한번도 없는 관리자)체크
                   // console.log(data.id == 0);

               },
               error   : function () {
               },
               complete: function () {
               }
           });
}