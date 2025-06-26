const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();

// dayjs 확장기능
dayjs.extend(window.dayjs_plugin_customParseFormat);

// 과정 전역변수
const courseConfig = {
    pageNo: null,
    pageSize: null,
    type: null,
    keyword: null,
    orderBy: null,
    orderDirection: null,
};
let courses = null;
let isInProgressByCourse = null;
let courseIdByCourse = null;

let topStatusCourseConfig = {
    pageNo: null,
    pageSize: null,
    type: null,
    keyword: null,
    orderBy: 'is_in_progress',
    orderDirection: 'DESC',
}


const learnerConfig = {
    pageNo: 1,
    pageSize: 10,
    type: 'fullname',
    keyword: null,
    orderBy: 'fullname',
    orderDirection: 'ASC',
};
let isInProgressByLearner = null;
let courseIdByLearner = null;

let currentDate = new Date();
let activeTabText = 'pills-daily';

$(document).ready(() => {

    if (loginUserType == "INSTRUCTOR") {
        updateTopCourseSelector();
        fetchLearnersByCondition();
    }
    $(document).on('change', '#courseSelector', handleCourseSelectorChange)

    // 중앙 날짜 업데이트
    updateDateDisplay();
    // 좌측 시간 업데이트
    updateTimeDisplay();

    // 상태바 변경 핸들러 실행
    handleProgressStatusChange();

    $('#is-in-progress').on('change', handleProgressStatusChange);
    $('#course-select').on('change', handleCourseSelectChange);
    $('#search-button').on('click', handleSearchChange);
    $(document).on('keydown', '#search-input', function(e) {
        if (e.key == "Enter") {
            e.preventDefault();
            handleSearchChange();
        }
    });
    $(document).on('click', '.page-link', handlePageBtnClick);


    $('a[data-toggle="pill"]').on('shown.bs.tab', handleTabChange);

    $(document).on('click', '#update-btn', updateTimeDisplay);
    $(document).on('click', '#prev-btn', handlePrevBtnClick);
    $(document).on('click', '#next-btn', handleNextBtnClick);
    $(document).on('click', '.weekly-status', handleWeeklyStatusBtnClick);
    /*$('#pills-daily-tab').on('shown.bs.tab', function (e) {
     console.log('일간 탭 활성화됨');
     // 여기에 원하는 동작 추가
     });
     $('#pills-weekly-tab').on('shown.bs.tab', function (e) {
     console.log('주간 탭 활성화됨');
     // 여기에 원하는 동작 추가
     });*/
});
function handleCourseSelectorChange() {
    courseIdByLearner = $(this).val();
    fetchLearnersByCondition();
}

async function apiGetRequestAboutCoursesByTop(endpoint, additionalParams = {}) {
    try {
        const response = await axios.get(endpoint, {
            params: { ...topStatusCourseConfig, ...additionalParams }
        });
        console.log(response.data);
        return response.data.data;
    } catch (error) {
        console.error(`${endpoint} 요청 오류:`, error);
        return [];
    }
}

async function updateTopCourseSelector() {
    let coursesWithPagination = await apiGetRequestAboutCoursesByTop(
        '/api/management/courses',
        {
            loginUserId: loginUserId,
            loginUserType: loginUserType,
            isInProgress: null});
    let courses = coursesWithPagination?.respDTOS || [];
    if (!Array.isArray(courses)) courses = [];
    console.log(courses);
    courseId = courses[0].id;
    isInProgress = null;
    updateTopCourseSelectorOption('#courseSelector', courses);
}
function updateTopCourseSelectorOption(selector, data) {
    console.log(selector, data);
    const $select = $(selector).empty();
    data.forEach(course => {
        const $option = $('<option>').val(course.id).text(course.name);
        $select.append($option);
        console.log(course.id, courseId);
        if (course.id == courseId) {
            console.log(courseId, "가 선택됨");
            $option.prop('selected', true);
        }
    });
}



function handleWeeklyStatusBtnClick() {
    let pid = $(this).data('id');
    console.log("버튼클릭: ", pid);

    getReasonAndDisplay(pid);
}

async function getReasonAndDisplay(pid) {
    let partInfo = await apiGetRequestByPid(
        `/api/management/participation/${pid}`);
    console.log(partInfo);

    let rawHtml = `
      <tr>
        <td class="text-center align-middle">${partInfo.partParticipationDate}</td>
        <td class="text-center align-middle text-primary">${getStatusFromPStatus(partInfo.partStatus)}</td>
        <td class="text-center align-middle">${partInfo.partExplanation}</td>
        <td class="text-center align-middle">${partInfo.partTrainingTime}H</td>
      </tr>
    `;
    $('#tbody-reason').html(rawHtml);
}


async function apiGetRequestByPid(endpoint) {
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
        // 그대로 월요일 보여줌
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
        fetchLearnersByCondition();
    }
    // 주간의 경우 (전주 월요일로 이동)
    else {
        currentDate.setDate(currentDate.getDate() - 7);
        currentDate = adjustToMonday(currentDate);
        updateDateDisplay();
        fetchLearnersByCondition();
    }

}
function handleNextBtnClick() {
    // 일간의 경우 (다음 날로 이동, 주말 건너 뜀)
    if (activeTabText == 'pills-daily') {
        currentDate.setDate(currentDate.getDate() + 1);
        if (currentDate.getDay() === 0) currentDate.setDate(currentDate.getDate() + 1);
        else if (currentDate.getDay() === 6) currentDate.setDate(currentDate.getDate() + 2);
        updateDateDisplay();
        fetchLearnersByCondition();
    }
    // 주간의 경우 (그 다음 주 월요일로 이동)
    else {
        currentDate.setDate(currentDate.getDate() + 7);
        currentDate = adjustToMonday(currentDate);
        updateDateDisplay();
        fetchLearnersByCondition();
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
    fetchLearnersByCondition();
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
    fetchLearnersByCondition();
}
async function initCourseSelect() {
    let coursesWithPagination = await apiGetRequestAboutCourses(
        '/api/management/courses',
        {
            loginUserId: loginUserId,
            loginUserType: loginUserType,
            isInProgress: isInProgressByCourse});
    courses = coursesWithPagination?.respDTOS || [];
    if (!Array.isArray(courses)) courses = [];
    updateSelectBox('#course-select', courses);
}



function updateSelectBox(selector, data) {
    const $select = $(selector).empty().append('<option value="">전체</option>');
    data.forEach(course => {
        $select.append($('<option>').val(course.id).text(course.name));
    });
    $("#course-select").trigger("change");
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

async function apiGetRequestAboutLearners(endpoint, additionalParams = {}) {
    try {
        const response = await axios.get(endpoint, {
            params: { ...learnerConfig, ...additionalParams }
        });
        console.log(response.data);
        return response.data;
    } catch (error) {
        console.error(`${endpoint} 요청 오류:`, error);
        return [];
    }
}
// 진행 상태 변경 핸들러
async function handleProgressStatusChange() {
    // 기존 검색 기록 삭제
    learnerConfig.keyword = null;
    $('search-input').val('');
    learnerConfig.pageNo = 1;

    isInProgressByCourse = $('#is-in-progress').val() === '' ? null : $('#is-in-progress').val();
    isInProgressByLearner = isInProgressByCourse;
    // saveListState();

    let coursesWithPagination = await apiGetRequestAboutCourses(
        '/api/management/courses',
        {
            loginUserId: loginUserId,
            loginUserType: loginUserType,
            isInProgress: isInProgressByCourse});
    courses = coursesWithPagination?.respDTOS || [];
    if (!Array.isArray(courses)) courses = [];

    updateSelectBox('#course-select', courses);
}

async function handleCourseSelectChange() {
    // 기존 검색 기록 삭제
    learnerConfig.keyword = null;
    $('search-input').val('');
    learnerConfig.pageNo = 1;

    // 진행상태는 진행상태 핸들러에서 선택한 옵션으로 저장되어있음
    // 과정상태 불러옴
    courseIdByLearner = $('#course-select').val() === '' ? null : $('#course-select').val();
    fetchLearnersByCondition();
}

async function fetchLearnersByCondition() {
    let learnersWithPagination = await apiGetRequestAboutLearners(
        '/api/learners/all',
        {
            loginUserId: loginUserId,
            loginUserType: loginUserType,
            courseId: courseIdByLearner,
            isInProgress: isInProgressByLearner });

    let learners = Array.isArray(learnersWithPagination.respDTOS) ?
                   learnersWithPagination.respDTOS : [learnersWithPagination.respDTOS];
    //saveListState();
    displayLearners(learners);
    displayPagination(learnersWithPagination);
}

async function handleSearchChange() {

    isInProgressByLearner = null;
    $('#is-in-progress').val('');
    courseIdByLearner = null;
    $('#course-select').val('');

    // initCourseSelect();

    learnerConfig.pageNo = 1;
    learnerConfig.pageSize = 10;
    learnerConfig.keyword = $("#search-input").val();
    fetchLearnersByCondition();
}

function handlePageBtnClick() {
    learnerConfig.pageNo = $(this).data('page');
    fetchLearnersByCondition();
    // saveListState();
    // displayCardView();
}

// 날짜 포맷팅 함수 (MM/DD)
function formatDateMMDD(date) {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${month}/${day}`;
}

function addMinutesToTime(timeStr, minutes) {
    let [h, m, s] = timeStr.split(':').map(Number);
    let date = new Date();
    date.setHours(h, m, s, 0);
    date.setMinutes(date.getMinutes() + minutes);
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
}

async function displayLearners(learners) {
    // 일간뷰
    if (activeTabText == 'pills-daily') {
        let rowHtml = `
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
              <tbody>`;
        if (learners.length == 0) {
            rowHtml += `<tr><td class="text-center" colspan="7">데이터가 없습니다.</td></tr>`;
        } else {
            learners.forEach(learner => {
                // 입퇴실가능시간
                let checkInStartTimeStr = `07:00:00`;
                let checkInEndTimeStr = learner.courseLessonStartTime; // .split(':').slice(0, 2).join(':');

                let checkOutStartTimeStr = learner.courseLessonEndTime; //.split(':').slice(0, 2).join(':');
                let checkOutEndTimeStr = addMinutesToTime(checkOutStartTimeStr, 10);

                // 교육생의 입실, 퇴실시간
                let currentDateCheckInTime = `<span>-</span>`;
                let currentDateCheckOutTime = `<span>-</span>`;

                learner.pageParticipationRespDTO.respDTOS.forEach(partDate => {
                    const partDateObj = new Date(partDate.pparticipationDate);
                    // for문 순회하면서 currentDate와 일치하는 row에서 처리
                    if (partDateObj.getDate() == currentDate.getDate()) {
                        // null이면 미입실, null이 아니면, checkInTime 출력
                        if (partDate.pcheckIn != null) {
                            const currentDateCheckInTimeObj = new Date(partDate.pcheckIn);
                            const hours = String(currentDateCheckInTimeObj.getHours()).padStart(2, '0');
                            const minutes = String(currentDateCheckInTimeObj.getMinutes()).padStart(2, '0');
                            const seconds = String(currentDateCheckInTimeObj.getSeconds()).padStart(2, '0');
                            const currentDateCheckInTimeStr = `${hours}:${minutes}:${seconds}`;
                            currentDateCheckInTime = `${currentDateCheckInTimeStr}`;
                        } else {
                            // 입실시간 10분전부터 입실시간까지 알림 버튼 출력
                            const nowTime = dayjs().format('HH:mm:ss');
                            const checkInEndTime = dayjs(checkInEndTimeStr, 'HH:mm:ss');
                            const nowTimeDayjs = dayjs(nowTime, 'HH:mm:ss');
                            const checkInEndTimeDayjs = dayjs(checkInEndTime.format('HH:mm:ss'), 'HH:mm:ss');
                            const checkInEndTimeMinus10Dayjs = checkInEndTimeDayjs.subtract(10, 'minute');

                            if (nowTimeDayjs > checkInEndTimeMinus10Dayjs && nowTimeDayjs < checkInEndTimeDayjs) {
                                currentDateCheckInTime = `
                                <span class="text-danger font-weight-bold">미입실</span><br>
                                <button class="btn btn-danger btn-icon-split btn-sm">
                                  <span class="text">알림</span>
                                </button>
                                `;
                            } else {
                                currentDateCheckInTime = `<span>미입실</span>`;
                            }
                        }
                        if (partDate.pcheckOut != null) {
                            const currentDateCheckOutTimeObj = new Date(partDate.pcheckOut);
                            const hours = String(currentDateCheckOutTimeObj.getHours()).padStart(2, '0');
                            const minutes = String(currentDateCheckOutTimeObj.getMinutes()).padStart(2, '0');
                            const seconds = String(currentDateCheckOutTimeObj.getSeconds()).padStart(2, '0');
                            const currentDateCheckOutTimeStr = `${hours}:${minutes}:${seconds}`;
                            currentDateCheckOutTime = `${currentDateCheckOutTimeStr}`;
                        } else {
                            // 퇴실시간가능시간에 알림버튼 출력
                            const nowTime = dayjs().format('HH:mm:ss');
                            const checkOutEndTime = dayjs(checkOutEndTimeStr, 'HH:mm:ss');

                            const nowTimeDayjs = dayjs(nowTime, 'HH:mm:ss');
                            const checkOutEndTimeDayjs = dayjs(checkOutEndTime.format('HH:mm:ss'), 'HH:mm:ss');
                            const checkOutEndTimeMinus10Dayjs = checkOutEndTimeDayjs.subtract(10, 'minute');

                            if (nowTimeDayjs > checkOutEndTimeMinus10Dayjs && nowTimeDayjs < checkOutEndTimeDayjs) {
                                currentDateCheckOutTime = `
                                <span class="text-danger font-weight-bold">미퇴실</span><br>
                                <button class="btn btn-danger btn-icon-split btn-sm">
                                  <span class="text">알림</span>
                                </button>
                                `;
                            } else {
                                currentDateCheckOutTime = `<span>미퇴실</span>`;
                            }
                        }
                    }
                });


                rowHtml += `
                    <tr>
                        <td class="text-center align-middle">
                        ${learner.courseIsInProgress ? '진행 중' : '종료'}</td>
                        <td class="title align-middle">${learner.courseName}</td>
                        <td class="text-center align-middle">${learner.courseFulltimeInstructor}</td>
                        <td class="text-center align-middle">
                        ${checkInStartTimeStr}~${checkInEndTimeStr}<br>
                        ${checkOutStartTimeStr}~${checkOutEndTimeStr}</td>
                        <td class="text-center align-middle">${learner.userFullname}</td>
                        <td class="text-center align-middle">
                          ${currentDateCheckInTime}
                        </td>
                        <td class="text-center align-middle">
                          ${currentDateCheckOutTime}
                        </td>
                      </tr>
                `;
            });
        }
        rowHtml += `</tbody></table></div>`;
        $('#pills-daily').html(rowHtml);

    }
    // 주간뷰
    else {
        console.log("주간뷰일 때");
        let rowHtml = `
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
        `;
        if (learners.length == 0) {
            rowHtml += `<tr><td class="text-center" colspan="9">데이터가 없습니다.</td></tr>`;
        } else {
            learners.forEach(learner => {
                let nowDateObj = toYMD(new Date());

                let monDateObj = toYMD(currentDate);
                let tueDateObj = new Date(monDateObj);
                tueDateObj.setDate(tueDateObj.getDate() + 1);
                let wedDateObj= new Date(monDateObj);
                wedDateObj.setDate(wedDateObj.getDate() + 2);
                let thuDateObj= new Date(monDateObj);
                thuDateObj.setDate(thuDateObj.getDate() + 3);
                let friDateObj= new Date(monDateObj);
                friDateObj.setDate(friDateObj.getDate() + 4);

                const dayRecords = {
                    mon: { status: '-', pid: null },
                    tue: { status: '-', pid: null },
                    wed: { status: '-', pid: null },
                    thu: { status: '-', pid: null },
                    fri: { status: '-', pid: null }
                };
                let monStatus = '-';
                let tueStatus = '-';
                let wedStatus = '-';
                let thuStatus = '-';
                let friStatus = '-';

                // 출결기록 row를 순회
                learner.pageParticipationRespDTO.respDTOS.forEach(partDate => {
                    const status = getStatusFromPStatus(partDate.pstatus);
                    const partDateObj = toYMD(new Date(partDate.pparticipationDate));

                    // 날짜 비교 함수
                    const isPastDate = (targetDate) =>
                        nowDateObj.getTime() > targetDate.getTime() &&
                        partDateObj.getTime() === targetDate.getTime();

                    // 각 요일별로 상태와 PID 업데이트
                    if (isPastDate(monDateObj)) {
                        dayRecords.mon = { status, pid: partDate.pid };
                    }
                    if (isPastDate(tueDateObj)) {
                        dayRecords.tue = { status, pid: partDate.pid };
                    }
                    if (isPastDate(wedDateObj)) {
                        dayRecords.wed = { status, pid: partDate.pid };
                    }
                    if (isPastDate(thuDateObj)) {
                        dayRecords.thu = { status, pid: partDate.pid };
                    }
                    if (isPastDate(friDateObj)) {
                        dayRecords.fri = { status, pid: partDate.pid };
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
                    ${learner.courseIsInProgress ? '진행 중' : '종료'}
                    </td>
                    <td class="title align-middle">${learner.courseName}</td>
                    <td class="text-center align-middle">${learner.courseFulltimeInstructor}</td>
                    <td class="text-center align-middle border-right">${learner.userFullname}</td>
                   
                    ${getStatusCell(dayRecords.mon.status, dayRecords.mon.pid, 'mon')}
                    ${getStatusCell(dayRecords.tue.status, dayRecords.tue.pid, 'tue')}
                    ${getStatusCell(dayRecords.wed.status, dayRecords.wed.pid, 'wed')}
                    ${getStatusCell(dayRecords.thu.status, dayRecords.thu.pid, 'thu')}
                    ${getStatusCell(dayRecords.fri.status, dayRecords.fri.pid, 'fri')}
                  </tr>
                `;
            });
        }
        rowHtml += `</tbody></table>`;
        $('#pills-weekly').html(rowHtml);

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

function getStatusFromPStatus(pstatus) {
    return {
               'ABSENCE': '결석',
               'ATTENDANCE': '출석',
               'LATE': '지각',
               'VACATION': '휴가',
               'LEAVE_EARLY': '조퇴',
               'VACATION_PENDING': '휴가(전)',
               'IN_STUDY': '수업중'
           }[pstatus] || '-';
}

function getStatusClass(status) {
    switch(status) {
        case '출석': return 'text-info';
        case '결석': return 'text-danger';
        case '휴가': return 'text-primary';
        case '지각': return 'text-warning';
        case '조퇴': return 'text-secondary';
        default: return '';
    }
}
function getStatusCell(status, pid, day) {
    let className = getStatusClass(status);
    let attrs = '';

    if (status === '휴가') {
        attrs = `data-toggle="modal" data-target="#participationModal" style="cursor: pointer"`;
        if (pid) {
            attrs += ` data-id="${pid}"`;
        }
    }
    return `<td class="weekly-status text-center align-middle font-weight-bold ${className}" ${attrs}>${status}</td>`;
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
    if (activeTabText == 'pills-daily') {
        $("#pills-daily").append(output);
    } else {
        $("#pills-weekly").append(output);
    }
}

function toYMD(date) {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}