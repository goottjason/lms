const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();

let baseConfig = {
    loginUserId  : loginUserId,
    loginUserType: loginUserType
};

// 상단셀렉트박스용
let courseTopConfig = {
    pageNo        : null,
    pageSize      : null,
    type          : "coName",
    keyword       : null,
    orderBy       : "coStartDate",
    orderDirection: "ASC",
    // 필터링
    coIsInProgress: null,
    coId          : null
};

// 중간셀렉트박스용
let courseConfig = {
    pageNo        : null,
    pageSize      : null,
    type          : "coName",
    keyword       : null,
    orderBy       : "coStartDate",
    orderDirection: "ASC",
    // 필터링
    coIsInProgress: true,
    coId          : null
};

const learnerConfig = {
    pageNo        : 1,
    pageSize      : 8,
    type          : "userFullname",
    keyword       : null,
    orderBy       : "userFullname",
    orderDirection: "ASC",
    // 필터링
    coIsInProgress: null,
    leCourseId    : null,
    leId          : null
};

// =============================================================================

// 오늘 날짜 객체
let currentDate = new Date();
// 첫화면: 일간뷰
let activeTabText = 'pills-daily';
let stompP = null;
function connectP(){
    let socket = new SockJS(`${location.origin}/ws`);
    stompP = Stomp.over(socket);

    stompP.connect({}, function(){

        stompP.subscribe(`/topic/participation`,
                         function(message){

                             console.log(message);
                             if(message.body == "checkInOrOut"){

                                 updateTimeDisplay();

                             }

                         })

    });

}

connectP();


$(document).ready(() => {

    if (loginUserType == "INSTRUCTOR") {
        // 상단셀렉트박스 로드
        fetchAndLoadTopCourseSelector();
        // 리스트 로드
        fetchAndDisplayLearners();
        $(document)
        .on("change", "#courseSelector", handleCourseTopSelectorChange);

    } else {
        // 상단셀렉트박스 가림
        $("#courseSelector").hide();
        // 중간셀렉트박스 로드
        fetchAndLoadCourseSelect();
        // 리스트 로드
        fetchAndDisplayLearners();

        $(document).on("change", "#course-select", handleCourseSelectChange);

    }

    // 공통 핸들러
    $(document).on("click", "#search-button", handleSearchButtonClick);
    $(document).on("keydown", "#search-input", function (e) {
        if (e.key == "Enter") {
            e.preventDefault();
            handleSearchButtonClick();
        }
    });
    $(document).on("click", ".page-link", handlePageButtonClick);

    $('a[data-toggle="pill"]').on('shown.bs.tab', handleTabChange);

    $(document).on('click', '#update-btn', updateTimeDisplay);
    $(document).on('click', '#prev-btn', handlePrevBtnClick);
    $(document).on('click', '#next-btn', handleNextBtnClick);
    $(document).on('click', '.weekly-status', handleWeeklyStatusBtnClick);

    // 중앙 날짜 업데이트
    updateDateDisplay();
    // 좌측 시간 업데이트
    updateTimeDisplay();
});

/* ========================================================================== */



async function fetchAndLoadTopCourseSelector() {
    let coursesWithPaging = await apiGetRequestParams(
        "/api/coursemanagement/courses",
        {...baseConfig, ...courseTopConfig});
    LoadTopCourseSelector(coursesWithPaging);
}
async function apiGetRequestParams(endpoint, params) {
    try {
        const response = await axios.get(endpoint, {params: params});
        return response.data.data;
    } catch (error) {
        return [];
    }
}
function LoadTopCourseSelector(coursesWithPaging) {
    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) {
        courses = [];
    }
    if (courses.length == 0) {
        $("#courseSelector").append(`<option value="">해당하는 과정이 없습니다.`);
        return;
    }
    LoadTopCourseSelectorOption("#courseSelector", courses);
}
function LoadTopCourseSelectorOption(selector, records) {
    const $select = $(selector).empty();
    records.forEach(record => {
        const $option = $("<option>")
        .val(record.courseWithAssignedInfo.coId)
        .text(record.courseWithAssignedInfo.coName);
        $select.append($option);
    });
    // 첫 번째 옵션 (초기)
    if (learnerConfig.leCourseId == null && records.length > 0) {
        $select.find("option:first").prop("selected", true);
        learnerConfig.leCourseId = $select.find("option:first").val();
    } else if (learnerConfig.leCourseId != null) {
        $select.find(`option[value="${learnerConfig.leCourseId}"]`).prop("selected", true);
    }
    $select.prop("disabled", true);
}

async function fetchAndLoadCourseSelect() {
    let coursesWithPaging = await apiGetRequestParams(
        "/api/coursemanagement/courses",
        {...baseConfig, ...courseConfig});
    LoadCourseSelect(coursesWithPaging);
}
function LoadCourseSelect(coursesWithPaging) {
    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) {
        courses = [];
    }
    LoadCourseSelectOption("#course-select", courses);
}
function LoadCourseSelectOption(selector, records) {
    const $select = $(selector).empty().append("<option value=\"\">전체</option>");
    ;
    records.forEach(record => {
        const $option = $("<option>")
        .val(record.courseWithAssignedInfo.coId)
        .text(record.courseWithAssignedInfo.coName);
        $select.append($option);
        if (record.courseWithAssignedInfo.coId == courseConfig.coId) {
            $option.prop("selected", true);
        }
    });
    // 초기 또는 전체로 선택시, 첫 번째(전체) 옵션
    if (courseConfig.coId == null) {
        $select.find("option:first").prop("selected", true);
    }
}

function handleCourseTopSelectorChange() {
    // 기존 검색과 페이징 초기화
    learnerConfig.keyword = null;
    $("#search-input").val("");
    learnerConfig.pageNo     = 1;
    learnerConfig.pageSize   = 8;
    learnerConfig.leCourseId = $(this).val();

    fetchAndDisplayLearners();
}

function handleCourseSelectChange() {
    // 기존 검색과 페이징 초기화
    learnerConfig.keyword = null;
    $("#search-input").val("");
    learnerConfig.pageNo     = 1;
    learnerConfig.pageSize   = 8;
    learnerConfig.leCourseId = $(this).val();

    // 셀렉트박스 옵션 변경
    learnerConfig.leCourseId =
        $("#course-select").val() === "" ? null : $("#course-select").val();

    fetchAndDisplayLearners();
}
function handleSearchButtonClick() {

    // 페이징 초기화 및 검색한 키워드로 검색
    learnerConfig.pageNo   = 1;
    learnerConfig.pageSize = 8;
    learnerConfig.keyword  = $("#search-input").val();

    if (loginUserType == "ADMINISTRATOR") {
        learnerConfig.leCourseId = null;
        $("#course-select").val("");
    } else if (loginUserType == "INSTRUCTOR") {
        // 강사의 경우 해당 과정에서 검색 유지
    }
    fetchAndDisplayLearners();
}
function handlePageButtonClick() {
    learnerConfig.pageNo = $(this).data("page");
    fetchAndDisplayLearners();
}

async function fetchAndDisplayLearners() {
    console.log("learnerConfig: ", learnerConfig);
    let learnersWithPaging = await apiGetRequestParams(
        "/api/learnermanagement/learnersonlypart",
        {...baseConfig, ...learnerConfig});
    displayView(learnersWithPaging);
}
function displayView(learnersWithPaging) {
    console.log(learnersWithPaging);
    //updateStatusBar(learnersWithPaging);
    displayTableList(learnersWithPaging);
    displayPagination(learnersWithPaging);
}
async function displayTableList(learnersWithPaging) {

    let learners = learnersWithPaging?.records || [];
    if (!Array.isArray(learners)) {
        learners = [];
    }

    // 일간뷰
    if (activeTabText == 'pills-daily') {
        console.log(learners);

        let rowHtml = ``;

        if (learners.length == 0) {
            rowHtml += `<tr><td class="text-center" colspan="7">데이터가 없습니다.</td></tr>`;
        } else {
            learners.forEach(learner => {
                if (learner.learnerCourse == null) {
                    return;
                }
                let course = learner.learnerCourse;
                // 입실가능시간
                let checkInStartTimeStr = `07:00:00`;
                let checkInEndTimeStr = course.coLessonStartTime;
                // 퇴실가능시간
                let checkOutStartTimeStr = course.coLessonEndTime;
                let checkOutEndTimeStr = adjustMinutesToTimeStr(checkOutStartTimeStr, 10);

                // 교육생의 입실, 퇴실시간
                let learnerCheckInStr = '-';
                let learnerCheckOutStr = '-';


                learner.partOverview.partList.forEach(part => {

                    const partDate = new Date(part.partParticipationDate);

                    // 오늘 날짜와 동일한 데이터에 대한 처리
                    if (partDate.getDate() == currentDate.getDate()) {

                        if (part.partCheckIn != null) {
                            // null이 아니면(입실함)
                            learnerCheckInStr = part.partCheckIn.split('T')[1];

                        } else if (part.partCheckIn == null) {
                            /*null이면(미입실함),
                                '입실마감시간-10분'부터 퇴실시작시간 직전까지 이메일알림 버튼 출력
                                그 외의 시간은 초기 세팅대로 '-' 출력*/
                            let buttonStartTime = fromTimeStrToTodayTime(adjustMinutesToTimeStr(checkInEndTimeStr, -10));
                            let buttonEndTime = fromTimeStrToTodayTime(checkOutStartTimeStr);

                            if (buttonStartTime.getTime() <= currentDate.getTime() && currentDate.getTime() <= buttonEndTime.getTime()) {
                                learnerCheckInStr = `
                                    <span class="text-danger font-weight-bold">미입실</span><br>
                                    <a href="/learnerManagement/sendEmail?leId=${learner.leId}" role="button">
                                      <i class="fas fa-solid fa-envelope"></i>
                                    </a>
                                `;
                            } else {
                                learnerCheckInStr =`<span class="text-secondary">미입실</span>`
                            }
                        }

                        if (part.partCheckOut != null) {
                            // null이 아니면(퇴실함)
                            learnerCheckOutStr = part.partCheckOut.split('T')[1];
                        } else if (part.partCheckOut == null) {
                            /*null이면(미퇴실함),
                             '퇴실마감시간-10분'부터 자정까지 이메일알림 버튼 출력
                             그 외의 시간은 초기 세팅대로 '-' 출력*/
                            let buttonStartTime = fromTimeStrToTodayTime(adjustMinutesToTimeStr(checkOutEndTimeStr, -10));
                            let buttonEndTime = fromTimeStrToTodayTime('23:59:59');

                            if (buttonStartTime.getTime() <= currentDate.getTime() && currentDate.getTime() <= buttonEndTime.getTime()) {
                                learnerCheckOutStr = `
                                    <span class="text-danger font-weight-bold">미퇴실</span><br>
                                    <a href="/learnerManagement/sendEmail?leId=${learner.leId}" role="button">
                                      <i class="fas fa-solid fa-envelope"></i>
                                    </a>
                                `;
                            } else {
                                learnerCheckOutStr =`<span class="text-secondary">미퇴실</span>`
                            }
                        }
                    }
                });

                rowHtml += `
                    <tr>
                        <td class="text-center align-middle">
                            ${course.coIsInProgress ? '진행 중' : '종료'}</td>
                        <td class="title align-middle">${course.coName}</td>
                        <td class="text-center align-middle">${course.coInstructorName}</td>
                        <td class="text-center align-middle">
                            ${checkInStartTimeStr}~${checkInEndTimeStr}<br>
                            ${checkOutStartTimeStr}~${checkOutEndTimeStr}
                        </td>
                        <td class="text-center align-middle" data-id="${learner.leId}">${learner.learnerUser.userFullname}</td>
                        <td class="text-center align-middle">
                            ${learnerCheckInStr}
                        </td>
                        <td class="text-center align-middle">
                            ${learnerCheckOutStr}
                        </td>
                    </tr>
                `;
            });
        }

        let html = `
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr class="text-center">
                            <th scope="col" class="align-middle" style="width: 10%">진행상황</th>
                            <th scope="col" class="align-middle" style="width: 31%">과정명</th>
                            <th scope="col" class="align-middle" style="width: 9%">강사</th>
                            <th scope="col" class="align-middle" style="width: 15%">입퇴실 가능시간</th>
                            <th scope="col" class="align-middle" style="width: 9%">교육생</th>
                            <th scope="col" class="align-middle" style="width: 13%">입실</th>
                            <th scope="col" class="align-middle" style="width: 13%">퇴실</th>
                        </tr>
                    </thead>
                    <tbody>
                    ${rowHtml}
                    </tbody>
                </table>
            </div>
        `;
        $('#pills-daily').html(html);
    }
    // 주간뷰
    else {
        // 주간으로 탭 이동시 curruntDate는 그 주 월요일로 변경됨
        console.log(learners);
        let rowHtml = ``;

        if (learners.length == 0) {
            rowHtml += `<tr><td class="text-center" colspan="9">데이터가 없습니다.</td></tr>`;
        } else {
            learners.forEach(learner => {
                if (learner.learnerCourse == null) {
                    return;
                }

                let course = learner.learnerCourse;

                let nowOnlyDate = toYMD(new Date());

                let monOnlyDate = toYMD(currentDate);
                let tueOnlyDate = new Date(monOnlyDate);
                tueOnlyDate.setDate(tueOnlyDate.getDate() + 1);
                let wedOnlyDate= new Date(monOnlyDate);
                wedOnlyDate.setDate(wedOnlyDate.getDate() + 2);
                let thuOnlyDate= new Date(monOnlyDate);
                thuOnlyDate.setDate(thuOnlyDate.getDate() + 3);
                let friOnlyDate= new Date(monOnlyDate);
                friOnlyDate.setDate(friOnlyDate.getDate() + 4);

                const dayRecords = {
                    mon: { korStatus: '-', partId: null },
                    tue: { korStatus: '-', partId: null },
                    wed: { korStatus: '-', partId: null },
                    thu: { korStatus: '-', partId: null },
                    fri: { korStatus: '-', partId: null }
                };
                let monStatus = '-';
                let tueStatus = '-';
                let wedStatus = '-';
                let thuStatus = '-';
                let friStatus = '-';

                // 출결기록 row를 순회
                learner.partOverview.partList.forEach(part => {

                    const korStatus = getKorStatusFromPartStatus(part.partStatus);
                    const partOnlyDate = toYMD(new Date(part.partParticipationDate));

                    // 날짜 비교 함수
                    const isPastDate = (targetDate) =>
                        nowOnlyDate.getTime() > targetDate.getTime() &&
                        partOnlyDate.getTime() === targetDate.getTime();

                    // 각 요일별로 상태와 PID 업데이트
                    if (isPastDate(monOnlyDate)) {
                        dayRecords.mon = { korStatus, partId: part.partId };
                    }
                    if (isPastDate(tueOnlyDate)) {
                        dayRecords.tue = { korStatus, partId: part.partId };
                    }
                    if (isPastDate(wedOnlyDate)) {
                        dayRecords.wed = { korStatus, partId: part.partId };
                    }
                    if (isPastDate(thuOnlyDate)) {
                        dayRecords.thu = { korStatus, partId: part.partId };
                    }
                    if (isPastDate(friOnlyDate)) {
                        dayRecords.fri = { korStatus, partId: part.partId };
                    }
                });

                const year = String(currentDate.getFullYear()).padEnd(2, '0');
                const month = String(currentDate.getMonth() + 1).padStart(2, '0');
                const day = String(currentDate.getDate()).padStart(2, '0');
                const currentDateStr = `${year}-${month}-${day}`;
                let againDateObj = new Date(currentDateStr);

                rowHtml += `
                    <tr>
                        <td class="text-center align-middle">
                        ${course.coIsInProgress ? '진행 중' : '종료'}
                        </td>
                        <td class="title align-middle">${course.coName}</td>
                        <td class="text-center align-middle">${course.coInstructorName}</td>
                        <td class="text-center align-middle border-right">${learner.learnerUser.userFullname}</td>
                        ${getStatusCell(dayRecords.mon.korStatus, dayRecords.mon.partId)}
                        ${getStatusCell(dayRecords.tue.korStatus, dayRecords.tue.partId)}
                        ${getStatusCell(dayRecords.wed.korStatus, dayRecords.wed.partId)}
                        ${getStatusCell(dayRecords.thu.korStatus, dayRecords.thu.partId)}
                        ${getStatusCell(dayRecords.fri.korStatus, dayRecords.fri.partId)}
                    </tr>
                `;
            });
        }

        let html = `
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr class="text-center">
                            <th scope="col" class="align-middle" style="width: 10%">진행상황</th>
                            <th scope="col" class="align-middle" style="width: 30%">과정명</th>
                            <th scope="col" class="align-middle" style="width: 10%">강사</th>
                            <th scope="col" class="align-middle border-right" style="width: 10%">교육생</th>
                            <th scope="col" class="align-middle" style="width: 8%" id="mon-header">월</th>
                            <th scope="col" class="align-middle" style="width: 8%" id="tue-header">화</th>
                            <th scope="col" class="align-middle" style="width: 8%" id="wed-header">수</th>
                            <th scope="col" class="align-middle" style="width: 8%" id="thu-header">목</th>
                            <th scope="col" class="align-middle" style="width: 8%" id="fri-header">금</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rowHtml}
                    </tbody>
                </table>
            </div>
        `;
        $('#pills-weekly').html(html);

        // 헤더에 표시할 날짜 계산
        const monDate = new Date(currentDate);
        const tueDate = new Date(monDate);
        tueDate.setDate(tueDate.getDate() + 1);
        const wedDate = new Date(monDate);
        wedDate.setDate(wedDate.getDate() + 2);
        const thuDate = new Date(monDate);
        thuDate.setDate(thuDate.getDate() + 3);
        const friDate = new Date(monDate);
        friDate.setDate(friDate.getDate() + 4);
        // 헤더 업데이트
        $('#mon-header').html(`월(${formatDateMMDD(monDate)})`);
        $('#tue-header').html(`화(${formatDateMMDD(tueDate)})`);
        $('#wed-header').html(`수(${formatDateMMDD(wedDate)})`);
        $('#thu-header').html(`목(${formatDateMMDD(thuDate)})`);
        $('#fri-header').html(`금(${formatDateMMDD(friDate)})`);

    }
}
function displayPagination(data) {

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

    // 테이블 다음에 붙여넣기
    if (activeTabText == 'pills-daily') {
        $("#pills-daily").append(output);
    } else if (activeTabText == 'pills-weekly'){
        $("#pills-weekly").append(output);
    }
}

function adjustMinutesToTimeStr(timeStr, minutes) {
    // map(Number) : 문자 -> 숫자
    let [h, m, s] = timeStr.split(':').map(Number);

    // 오늘 날짜 객체 생성
    let date = new Date();
    // 시, 분, 초 세팅
    date.setHours(h, m, s, 0);
    // + 또는 - minutes분 세팅
    date.setMinutes(date.getMinutes() + minutes);

    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
}
function fromTimeStrToTodayTime(timeStr) {
    // map(Number) : 문자 -> 숫자
    let [h, m, s] = timeStr.split(':').map(Number);

    let date = new Date();
    date.setHours(h, m, s, 0);
    return date;
}

function handleWeeklyStatusBtnClick() {
    let partId = $(this).data('id');
    console.log("버튼클릭: ", partId);

    getReasonAndDisplay(partId);
}
async function getReasonAndDisplay(partId) {
    let partInfo = await apiGetRequestByPartId(
        `/api/management/participation/${partId}`);
    console.log(partInfo);

    let rawHtml = `
      <tr>
        <td class="text-center align-middle">${partInfo.partParticipationDate}</td>
        <td class="text-center align-middle text-primary">${getKorStatusFromPartStatus(partInfo.partStatus)}</td>
        <td class="text-center align-middle">${partInfo.partExplanation}</td>
        <td class="text-center align-middle">${partInfo.partTrainingTime}H</td>
      </tr>
    `;
    $('#tbody-reason').html(rawHtml);
}
async function apiGetRequestByPartId(endpoint) {
    try {
        const response = await axios.get(endpoint);
        return response.data.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}

// 일간, 주간 탭
function handleTabChange() {
    activeTabText = $('.nav-link.active').attr('aria-controls');
    // 일간으로 탭 이동
    if (activeTabText == 'pills-daily') {
        // 다시 오늘 날짜 보여줌
        currentDate = new Date();
        updateDateDisplay();
    }
    // 주간으로 탭 이동
    else {
        // 그 주 월요일로 이동 후 날짜 업데이트
        currentDate = adjustToMonday(currentDate);
        updateDateDisplay();
    }
}
// 이전, 다음 버튼
function handlePrevBtnClick() {
    // 일간의 경우 (전 날로 이동, 주말 건너 뜀)
    if (activeTabText == 'pills-daily') {
        currentDate.setDate(currentDate.getDate() - 1);
        if (currentDate.getDay() === 0) currentDate.setDate(currentDate.getDate() - 2);
        else if (currentDate.getDay() === 6) currentDate.setDate(currentDate.getDate() - 1);
        updateDateDisplay();
        fetchAndDisplayLearners();
    }
    // 주간의 경우 (전주 월요일로 이동)
    else {
        currentDate.setDate(currentDate.getDate() - 7);
        currentDate = adjustToMonday(currentDate);
        updateDateDisplay();
        fetchAndDisplayLearners();
    }

}
function handleNextBtnClick() {
    // 일간의 경우 (다음 날로 이동, 주말 건너 뜀)
    if (activeTabText == 'pills-daily') {
        currentDate.setDate(currentDate.getDate() + 1);
        if (currentDate.getDay() === 0) currentDate.setDate(currentDate.getDate() + 1);
        else if (currentDate.getDay() === 6) currentDate.setDate(currentDate.getDate() + 2);
        updateDateDisplay();
        fetchAndDisplayLearners();
    }
    // 주간의 경우 (그 다음 주 월요일로 이동)
    else {
        currentDate.setDate(currentDate.getDate() + 7);
        currentDate = adjustToMonday(currentDate);
        updateDateDisplay();
        fetchAndDisplayLearners();
    }
}

function adjustToMonday(date) {
    const day = date.getDay();
    // 일요일(0)일 경우 월요일로 이동
    if (day === 0) {
        date.setDate(date.getDate() + 1);
    }
    // 토요일(6)일 경우 월요일로 이동
    else if (day === 6) {
        date.setDate(date.getDate() + 2);
    }
    // 그 외 요일: 월요일로 이동
    else {
        date.setDate(date.getDate() - (day - 1));
    }
    return date;
}
// 날짜 출력
function updateDateDisplay() {
    // 주말이면 월요일로 조정
    currentDate = adjustToWeekday(currentDate);
    // 2025.06.23. 월요일 형태로 출력
    $('#date-display').text(formatDate(currentDate));
    fetchAndDisplayLearners();
}
function adjustToWeekday(date) {
    const day = date.getDay();
    if (day === 0) date.setDate(date.getDate() + 1); // 일요일이면 월요일로
    else if (day === 6) date.setDate(date.getDate() + 2); // 토요일이면 월요일로
    return date;
}
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const dayOfWeek = getKoreanDay(date.getDay());
    return `${year}. ${month}. ${day}. ${dayOfWeek}요일`;
}
function getKoreanDay(day) {
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    return days[day];
}
// 시간 출력
function updateTimeDisplay() {
    const now = new Date();
    const hours = now.getHours();
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const ampm = hours >= 12 ? '오후' : '오전';
    const formattedHour = hours % 12 || 12;
    const timeStr = `${ampm} ${formattedHour}:${minutes} 기준`;
    // 입·퇴실 현황 업데이트됨 (오후 9:54 기준)
    $('#time-check').text(timeStr);
    fetchAndDisplayLearners();
}

// 날짜 포맷팅 함수 (MM/DD)
function formatDateMMDD(date) {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${month}/${day}`;
}
function getKorStatusFromPartStatus(partStatus) {
    return {
               'ABSENCE': '결석',
               'ATTENDANCE': '출석',
               'LATE': '지각',
               'VACATION': '휴가',
               'LEAVE_EARLY': '조퇴',
               'VACATION_PENDING': '휴가(전)',
               'IN_STUDY': '수업중'
           }[partStatus] || '-';
}
function getTextColorByStatus(korStatus) {
    switch(korStatus) {
        case '출석': return 'text-info';
        case '결석': return 'text-danger';
        case '휴가': return 'text-primary';
        case '지각': return 'text-warning';
        case '조퇴': return 'text-secondary';
        default: return '';
    }
}
function getStatusCell(korStatus, partId) {

    let textColor = getTextColorByStatus(korStatus);
    let attrs = '';

    if (korStatus === '휴가') {
        attrs = `data-toggle="modal" data-target="#participationModal" style="cursor: pointer" data-id="${partId}"`;
    }
    return `<td class="weekly-status text-center align-middle font-weight-bold ${textColor}" ${attrs}>${korStatus}</td>`;
}
function toYMD(date) {
    // hms는 0으로
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}
