const loginUserId   = $("#login-user-id").val();
const loginUserType = $("#login-user-type").val();

// const todayDate = new Date();

let coursesWithPaging = [];

let baseConfig = {
    loginUserId  : loginUserId,
    loginUserType: loginUserType
};
let courseConfig = {
    pageNo: null,
    pageSize: null,
    type: "coName", // Builder.Default (coName)
    keyword: null,
    orderBy: "coStartDate", // Builder.Default (coStartDate)
    orderDirection: "ASC", // Builder.Default (ASC)
    // 필터링
    coIsInProgress: true,
    coId: null
};

const { datesYYYYMMDD, datesMD } = getRecent4WeeksWeekdays();

$(document).ready(function() {
    // ADMINISTRATOR는 상단셀렉트박스 불필요
    $('#courseSelector').hide();

    fetchAndDisplayCourses();
    updateLastExecutionTime();
    updateLastExecutionTimeForPart();


    $(document).on('click', '#trigger-scheduler', handleTriggerSchedulerClick);
    $(document).on('click', '#trigger-scheduler-for-part', handleTriggerSchedulerForPartClick);
    $(document).on("change", "#part-course-select", handlePartCourseSelectChange);
    $(document).on("click", "#part-email-button", handlePartEmailButtonClick);

    $(document).on("change", "#check-all", handleCheckAllChange);
    $(document).on('change', '.target-check', handleTargetCheckChange);
    $(document).on('click', '#sendEmailModalBtn', handleSendEmailModalBtnClick);
});
async function fetchAndDisplayIncompleteTask() {
    let countList = await apiGetRequestParams(
        '/api/coursemanagement/incompletetaskcount',
        {...baseConfig, ...courseConfig});
    displayIncompleteTask(countList);
}
function displayIncompleteTask(countList) {
    // countList에서 2개
    const courses = coursesWithPaging?.records || [];

    // esIsCounselingReceived가 false인 항목 카운트
    let falseCounselingCount = 0;

    courses.forEach(course => {
        const learners = course.courseLearnerOverview?.learnerList || [];
        learners.forEach(learner => {
            const employmentSupport = learner.learnerEmploymentSupport || {};
            // esIsCounselingReceived가 false인 경우 카운트 증가
            if (employmentSupport.esIsCounselingReceived == false) {
                falseCounselingCount++;
            }
        });
    });
    $('#inquery-count').text(countList.inquiryCount);
    $('#forum-report-count').text(countList.forumReportCount);
    $('#counseling-count').text(falseCounselingCount);
    $('#training-unsign-count').text(countList.trainingUnsignCount);
}
async function fetchAndDisplayCourses() {
    coursesWithPaging = await apiGetRequestParams(
        '/api/coursemanagement/courses',
        {...baseConfig, ...courseConfig});
    console.log(coursesWithPaging);
    displayBubleChart();
    displayCourseTable();
    displayTrainingLogChart();
    displayLearnerCard();
    loadPartCourseSelect();
    fetchAndDisplayIncompleteTask();
    fetchAndDisplayClassroomUsage();
}
async function apiGetRequestParams(endpoint, params) {
    try {
        const response = await axios.get(endpoint, {params: params});
        return response.data.data;
    } catch (error) {
        return [];
    }
}
function displayBubleChart() {
    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) courses = [];
    if(courses.length == 0) {
        console.log("진행중인 과정이 없는 상태");
        return;
    }
    // 데이터 가공: 필요한 정보 추출
    const extractedData = courses.map(course => ({
        courseName: course.courseWithAssignedInfo.coName,
        learnerCount: course.courseWithAssignedInfo.coNumberOfLearner,
        progressRate: course.scheduleOverview.courseProgressRate,
        attendanceRate: Math.floor(course.courseLearnerOverview.courseAvgAttendanceRate)
    }));

    const colors = [
        '#4e73df',
        '#1cc88a',
        '#f6c23e',
        '#e74a3b',
        '#B48EAD',
        '#8FBCBB',
        '#00b4d8',
    ];

    const series = extractedData.map((course, idx) => ({
        name: course.courseName,
        data: [{
            x: course.progressRate,
            y: course.attendanceRate,
            z: course.learnerCount
        }],
        color: colors[idx % colors.length]
    }));

    const options = {
        series,
        chart: {
            height: 400,
            type: 'bubble',
            zoom: {
                enabled: false
            },
            toolbar: {
                show: true,
                tools: {
                    pan: false,
                    zoom: false,
                    zoomin: false,
                    zoomout: false,
                    reset: false
                },
            }
        },
        dataLabels: { enabled: true, style: { fontSize: '16px', fontWeight: 400, color: '#858796' }  },
        fill: { opacity: 0.85 },
        /*title: { text: '', align: 'center', style: { fontSize: '16px' } },*/
        xaxis: {
            title: { text: '과정진행률', style: { fontSize: '16px', fontWeight: 400, color: '#858796' } },
            min: 0, max: 100, tickAmount: 10,
            labels: { style: { fontSize: '16px', fontWeight: 400, color: '#858796' } }
        },
        yaxis: {
            title: {
                text: '교육생출결률',
                style: { fontSize: '16px', fontWeight: 400, color: '#858796', fontFamily: 'Nunito, sans-serif'},
                /*rotate: 0,*/
                offsetX: 0,
                offsetY: 0,
            },
            min: 0, max: 100,
            labels: {
                style: {
                    fontSize: '16px',
                    color: '#858796',
                    fontFamily: 'Nunito, sans-serif'
                }
            }
        },
        /*grid: {
            padding: {
                left: 40
            }
        },*/
        tooltip: {
            style: { fontSize: '16px' },
            custom: function({ seriesIndex, dataPointIndex, w }) {
                const data = w.config.series[seriesIndex].data[dataPointIndex];
                return `
                    <div class="chart-tooltip">
                      <strong>${w.config.series[seriesIndex].name}</strong>
                      <div>과정진행률: ${data.x.toFixed(2)}%</div>
                      <div>교육생출결률: ${data.y.toFixed(2)}%</div>
                      <div>수강생: ${data.z}명</div>
                    </div>
                  `;
            }
        },
        plotOptions: {
            bubble: {
                minBubbleRadius: 15,
                maxBubbleRadius: 70
            }
        }
    };

    const chart = new ApexCharts(document.querySelector("#bubble-chart"), options);
    chart.render();
}
function displayCourseTable() {
    $('#course-table').empty();
    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) courses = [];
    if(courses.length == 0) {
        console.log("진행중인 과정이 없는 상태");
        return;
    }
    courses.forEach(course => {
        let html = `
            <tr class="text-center">
              <td class="align-middle"><a href="/courseManagement/courseDetail?courseId=${course.courseWithAssignedInfo.coId}">${course.courseWithAssignedInfo.coName}</a></td>
              <td class="align-middle"><a href="/operationsManagement/staffDetail?staffId=${course.courseWithAssignedInfo.coInstructorId}">${course.courseWithAssignedInfo.coInstructorName}</a></td>
              <td class="align-middle">${course.courseWithAssignedInfo.coStartDate} ~ ${course.courseWithAssignedInfo.coEndDate}</td>
              <td class="align-middle">
                <div class="progress-wrapper" style="position: relative;">
                  <div class="progress" style="height: 20px;">
                    <div class="progress-bar bg-success" role="progressbar"
                         style="width: ${course.scheduleOverview.courseProgressRate}%"
                         attr="aria-valuenow=${course.scheduleOverview.courseProgressRate}"
                         aria-valuemin="0" aria-valuemax="100">
                    </div>
                  </div>
                  <span class="progress-text text-gray-800"
                        style="position: absolute; top: 0; left: 0; right: 0; text-align: center; line-height: 20px;">
                        ${course.scheduleOverview.courseProgressRate}%
                  </span>
                </div>
              </td>
              <td class="align-middle">
                <div class="progress-wrapper" style="position: relative;">
                  <div class="progress" style="height: 20px;">
                    <div class="progress-bar bg-warning" role="progressbar"
                         style="width: ${course.courseLearnerOverview.courseAvgAttendanceRate}%"
                         attr="aria-valuenow=${course.courseLearnerOverview.courseAvgAttendanceRate}"
                         aria-valuemin="0" aria-valuemax="100">
                    </div>
                  </div>
                  <span class="progress-text text-gray-800"
                        style="position: absolute; top: 0; left: 0; right: 0; text-align: center; line-height: 20px;">
                        ${course.courseLearnerOverview.courseAvgAttendanceRate}%
                  </span>
                </div>
              </td>
            </tr>
        `;
        $('#course-table').append(html);
    });
}
function displayTrainingLogChart() {
    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) courses = [];
    if(courses.length == 0) {
        console.log("진행중인 과정이 없는 상태");
        return;
    }

    // 데이터 가공: 필요한 정보 추출
    const extractedData = courses.map(course => ({
        // name: course.courseWithAssignedInfo.coName,
        name: course.courseWithAssignedInfo.coInstructorName,
        data: generateData(datesYYYYMMDD, course.courseTrainingDates, datesMD),
    }));
    var options = {
        series: extractedData,
        chart: {
            height: 350,
            type: 'heatmap',
        },
        dataLabels: {
            enabled: false
        },
        plotOptions: {
            heatmap: {
                colorScale: {
                    ranges: [
                        {
                            from: 1, // 데이터가 있는 값
                            to: 100,
                            color: "#BFC513",
                            name: "등록"
                        },
                        {
                            from: 0, // 데이터가 없는 값 (0 또는 null)
                            to: 0,
                            color: "#eaeaea", // 회색
                            name: "미등록"
                        }
                    ]
                }
            }
        },
        colors: ["#BFC513"],
        tooltip: {
            y: {
                formatter: function(value, { series, seriesIndex, dataPointIndex, w }) {
                    const cellData = extractedData[seriesIndex].data[dataPointIndex];
                    if (cellData && cellData.link) {
                        return `<a href="${cellData.link}" target="_blank">기록 보기</a>`;
                    }
                    return value;
                }
            }
        }
    };

    var chart = new ApexCharts(document.querySelector("#heatmap-chart"), options);
    chart.render();
    // 제출했는지
    const cardData = courses.map((course) => ({
        coInstructorName: course.courseWithAssignedInfo.coInstructorName,
        isTodaySubmit: getIsTodaySubmit(course.courseTrainingDates),
        isTodayClassdate: getIsTodayClassdate(course.scheduleOverview.classdateList),
        coInstructorId: course.courseWithAssignedInfo.coInstructorId,
        coName: course.courseWithAssignedInfo.coName,
        coId: course.courseWithAssignedInfo.coId
    }));
    console.log("cardData", cardData);
    displayInstructorCard(cardData);

}
function displayLearnerCard() {
    $('#learner-box').empty();

    let currentDate = new Date();
    const cards = [];

    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) courses = [];

    // 진행중이 과정이 없는 상태
    if(courses.length == 0) {
        $('#learner-box').html(`<div class="text-center">데이터가 없습니다.</div>`);
        return;
    }

    courses.forEach(course => {
       course.courseLearnerOverview.learnerList.forEach(learner => {

           // 미배정 교육생의 경우 continue
           if (learner.learnerCourse == null) {
               return; // forEach 메서드에서는 continue 역할
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

           // 교육생의 출결기록 순회
           learner.partOverview.partList.forEach((part) => {
               const partDate = new Date(part.partParticipationDate);

               // 오늘 날짜와 동일한 데이터에 대한 처리
               if (partDate.getDate() == currentDate.getDate()) {

                   // 미입실일 때 처리
                   if (part.partCheckIn == null) {
                       /* '입실마감시간-10분'부터 퇴실시작시간 직전까지 카드에 미입실, 이메일알림 버튼 출력
                          그 외의 시간은 카드 없음 */
                       let buttonStartTime = fromTimeStrToTodayTime(
                           adjustMinutesToTimeStr(checkInEndTimeStr, -10));
                       let buttonEndTime   = fromTimeStrToTodayTime(
                           checkOutStartTimeStr);

                       // 현재시간이 입실마감시간-10분 ~ 퇴실시작시간 전인 경우 카드 작성
                       if (buttonStartTime.getTime() <= currentDate.getTime() &&
                           currentDate.getTime() <= buttonEndTime.getTime()) {
                           let colHtmlIn = `
                               <div class="col-md-4">
                                   <div class="learner-card card text-center shadow-sm h-100">
                                      <div class="card-body d-flex flex-column px-4 py-3">
                                          <div class="mb-3 not-check-in-status" data-id="${learner.leId}">
                                            <span class="badge rounded-pill border border-danger text-danger bg-transparent px-3 py-2"
                                                  style="font-size: 0.8rem;">
                                              미입실
                                            </span>
                                          </div>
                                          <div>
                                            <a href="/courseManagement/courseDetail?courseId=${course.coId}"><h6 class="card-title mb-1 text-dark fw-bold not-check-in-coname" data-id="${learner.leId}"
                                                style="font-size: 1rem;">${course.coName}</h6></a>
                                            <a href="/learnerManagement/learnerDetail?leId=${learner.leId}&coName=${course.coName}"><p class="card-text small mb-2 text-secondary not-check-in-fullname" data-id="${learner.leId}"><b>${learner.learnerUser.userFullname}</b></p></a>
                                          </div>
                                          <div class="mt-auto">
                                            <a href="/learnerManagement/sendEmail?leId=${learner.leId}" role="button">
                                              <i class="fas fa-solid fa-envelope"></i>
                                            </a>
                                            <input type="hidden" value="${learner.learnerUser.userEmail}" class="not-check-in-email">
                                          </div>
                                    </div>
                                   </div>
                               </div>
                           `;
                           cards.push(colHtmlIn);
                       }
                   }

                   // 미퇴실일 때 처리
                   if (part.partCheckOut == null) {
                       /*null이면(미퇴실함),
                        '퇴실마감시간-10분'부터 자정까지 이메일알림 버튼 출력
                        그 외의 시간은 초기 세팅대로 '-' 출력*/
                       let buttonStartTime = fromTimeStrToTodayTime(adjustMinutesToTimeStr(checkOutEndTimeStr, -10));
                       let buttonEndTime = fromTimeStrToTodayTime('23:59:59');

                       if (buttonStartTime.getTime() <= currentDate.getTime() && currentDate.getTime() <= buttonEndTime.getTime()) {
                           // 카드작성
                           let colHtmlOut = `
                               <div class="col-md-4">
                                  <div class="learner-card card text-center shadow-sm h-100">
                                    <div class="card-body d-flex flex-column px-4 py-3">
                                      <div class="mb-3 not-check-in-status" data-id="${learner.leId}">
                                        <span class="badge rounded-pill border border-warning text-warning bg-transparent px-3 py-2"
                                              style="font-size: 0.8rem;">
                                          미퇴실
                                        </span>
                                      </div>
                                      <div>
                                        <a href="/courseManagement/courseDetail?courseId=${course.coId}">
                                        <h6 class="card-title mb-1 text-dark fw-bold not-check-in-coname" data-id="${learner.leId}"
                                            style="font-size: 1rem;">${course.coName}</h6></a>
                                        <a href="/learnerManagement/learnerDetail?leId=${learner.leId}&coName=${course.coName}"><p class="card-text small mb-2 text-secondary  not-check-in-fullname" data-id="${learner.leId}"><b>${learner.learnerUser.userFullname}</b></p></a>
                                      </div>
                                      <div class="mt-auto">
                                        <a href="/learnerManagement/sendEmail?leId=${learner.leId}" role="button">
                                          <i class="fas fa-solid fa-envelope"></i>
                                        </a>
                                        <input type="hidden" value="${learner.learnerUser.userEmail}" class="not-check-in-email">
                                      </div>
                                    </div>
                                  </div>
                                </div>
                           `;
                           cards.push(colHtmlOut);
                       }
                   }
               }
           });
       });
    });

    // 카드가 하나라도 있을 경우
    if (cards.length > 0) {
        let cardCnt = 0;
        let html = `
            <!-- 왼쪽 화살표 -->
            <button class="btn mr-2" data-target="#learnerCarousel" data-slide="prev">
              <i class="fas fa-chevron-left"></i>
            </button>
            <div id="learnerCarousel" class="carousel slide w-100" style="max-width: 1000px;">
              <div class="carousel-inner" id="learner-body">
        `;
        for (let i= 0; i < cards.length/3; i++) {
            if (i == 0) {
                html += `<div class="carousel-item active"><div class="row">`;
            } else {
                html += `<div class="carousel-item"><div class="row">`;
            }
            for(let j= 0; j < 3; j++) {
                if (cardCnt < cards.length) { // 0 > 5
                    html += cards[cardCnt];
                    cardCnt++;
                } else {
                    break;
                }
            }
            html += `</div></div>`;
        }
        html += `
            </div></div>
            <!-- 오른쪽 화살표 -->
            <button class="btn ml-2" data-target="#learnerCarousel" data-slide="next">
              <i class="fas fa-chevron-right"></i>
            </button>`;
        $('#learner-box').html(html);
    } else {
        $('#learner-box').html(`<div class="text-center">데이터가 없습니다.</div>`);
    }

    $('#learner-card-count').text(`(${cards.length}명)`);

}
function handlePartEmailButtonClick() {
    $('#recipients').empty();
    $('#title').val("Goott5 입/퇴실 안내");
    $("#contents").val("안녕하세요. 금일 출석/퇴실체크를 하지 않으셔서 연락 남깁니다.");

    $('#part-card-list').empty();

    let learnerList = [];
    $('.learner-card').each(function() {

        // this는 현재 .learner-card DOM 요소
        let $card = $(this);

        // leId 추출 (data-id 속성 활용)
        let leId = $card.find('.not-check-in-status').data('id');

        // userFullname 추출 (태그 내용)
        let userFullname = $card.find('.not-check-in-fullname').text().trim();

        // coName 추출 (태그 내용)
        let coName = $card.find('.not-check-in-coname').text().trim();

        // userEmail 추출 (input hidden value)
        let userEmail = $card.find('.not-check-in-email').val();

        // 객체로 저장
        learnerList.push({
                             leId: leId,
                             userFullname: userFullname,
                             coName: coName,
                             userEmail: userEmail
                         });
        let trHtml = `
            <tr>
                <td class="align-middle text-center">
                  <div class="d-flex justify-content-center align-items-center">
                    <input class="target-check form-check-input" type="checkbox" data-id="${leId}">
                  </div>
                </td>
                <td class="text-center align-middle">${userFullname}</td>
                <td class="title align-middle">${coName}</td>
                <td class="text-center align-middle checked-email" data-id="${leId}">${userEmail}</td>
            </tr>
        `;
        $('#part-card-list').append(trHtml);
    });

    $('#check-all').prop("checked", true).trigger('change');

    console.log(learnerList);


}
function handleCheckAllChange() {
    const isChecked = $(this).prop("checked");
    $("tbody input[type=\"checkbox\"]").prop("checked", isChecked).trigger('change');
}
function handleTargetCheckChange() {

    let leId = $(this).data('id');
    let emailText = $('.checked-email[data-id="' + leId + '"]').text().trim();
    let isChecked = $(this).is(':checked');
    console.log('체크 상태:', isChecked, 'leId:', leId, 'emailText:', emailText);
    if (isChecked) {
        let html= `<span class="badge rounded-pill border border-primary text-primary bg-transparent mx-1 my-1" style="font-size: 1.0rem" id="leId-${leId}">${emailText}</span>`;
        $('#recipients').append(html);
    } else {
        $(`#leId-${leId}`).remove();
    }

}
function handleSendEmailModalBtnClick() {
    let title    = $("#title").val().trim();
    let contents = $("#contents").val().trim();
    if (title == "") {
        $("#title-msg").text("제목을 입력해주세요.");
        return;
    }
    if (contents == "") {
        $("#contents-msg").text("내용을 입력해주세요.");
        return;
    }

    let sendEmailList = [];
    $('.target-check:checked').each(function() {
        let leId = $(this).data('id');
        let emailText = $('.checked-email[data-id="' + leId + '"]').text().trim();
        sendEmailList.push(emailText);
    });
    console.log(title, contents, sendEmailList);

    $("#sendEmailModal").modal({
                                   backdrop: "static",
                                   keyboard: false
                               });

    $(".progress-modal-body").html("대상자에게 이메일을 발송 하시겠습니까?");
    $(".progress-modal-footer").html(`
          <button id="sendEmailBtn" class="btn btn-primary" type="button">발송</button>
          <button class="btn btn-secondary" type="button" data-dismiss="modal">취소</button>
        `);

    $(".modal").on("click", "#sendEmailBtn", function () {
        sendEmail(title, contents, sendEmailList);
    });
}

async function sendEmail(title, contents, emailList) {

    $(".progress-modal-body").html(`
        <div class="text-center">
          <p class="mb-2">이메일 발송 중입니다. 잠시만 기다려주세요.</p>
          <div class="progress" style="height: 20px;">
            <div class="progress-bar progress-bar-striped progress-bar-animated bg-primary"
                 role="progressbar" style="width: 100%;">
              진행 중...
            </div>
          </div>
        </div>
      `);
    $(".progress-modal-footer").html("");

    axios.post("/learnerManagement/sendEmail", {
        title    : title,
        contents : contents,
        emailList: emailList,

    })
         .then(function (response) {
             if (response.data.code === 200) {
                 $(".progress-modal-body").html("이메일 발송이 완료되었습니다.");
                 $(".progress-modal-footer").html(`
                  <button class="btn btn-secondary" type="button" data-dismiss="modal">닫기</button>
                `);
                 $("#title").val("");
                 $("#contents").val("");
             }
         })
         .catch(function (error) {
             console.log(error);
             $(".progress-modal-body").html(`<p class="text-danger">이메일 발송 중 오류가 발생했습니다.</p>`);
             $(".progress-modal-footer").html(`
                <button class="btn btn-secondary" type="button" data-dismiss="modal">닫기</button>
              `);
         });
}
function fromTimeStrToTodayTime(timeStr) {
    // map(Number) : 문자 -> 숫자
    let [h, m, s] = timeStr.split(':').map(Number);

    let date = new Date();
    date.setHours(h, m, s, 0);
    return date;
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

    return `${String(date.getHours()).padStart(2, '0')}:
            ${String(date.getMinutes()).padStart(2, '0')}:
            ${String(date.getSeconds()).padStart(2, '0')}`;
}
function displayInstructorCard(cardData) {

    $('#instructor-body').empty();

    const cards = [];

    if (cardData.length > 0) {
        cardData.forEach((item) => {
            // 오늘이 수업일인데 제출 안했을 때 카드 생성
            if(item.isTodayClassdate && !item.isTodaySubmit) {
                let colHtml = `
                    <div class="col-md-4">
                      <div class="instructor-card card text-center shadow-sm h-100">
                        <div class="card-body d-flex flex-column px-4 py-3">
                          <div class="mb-3">
                            <span class="badge rounded-pill border border-danger text-danger bg-transparent px-3 py-2"
                                  style="font-size: 0.8rem;">
                              미등록
                            </span>
                          </div>
                          <div>
                            <a href="/courseManagement/courseDetail?courseId=${item.coId}"><h6 class="card-title mb-1 text-dark fw-bold"
                                style="font-size: 1rem;">${item.coName}</h6></a>
                            <a href="/operationsManagement/staffDetail?staffId=${item.coInstructorId}"><p class="card-text small mb-2 text-secondary"><b>${item.coInstructorName}</b></p></a>
                          </div>
                          <div class="mt-auto">
                            <a onclick="callAlarm(${item.coInstructorId}, '${item.coInstructorName}', '${item.coName}');" role="button">
                              <i class="fas fa-bell fa-fw"></i>
                            </a>
                          </div>
                        </div>
                      </div>
                    </div>
                `;
                cards.push(colHtml);
            }
        });

        // 카드가 하나라도 있을 경우
        if (cards.length > 0) {
            let cardCnt = 0;
            let html = `
                <!-- 왼쪽 화살표 -->
                <button class="btn mr-2" data-target="#instructorCarousel" data-slide="prev">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <div id="instructorCarousel" class="carousel slide w-100" style="max-width: 1000px;">
                    <div class="carousel-inner" id="instructor-body">
            `;
            for (let i= 0; i < cards.length/3; i++) {
                if (i == 0) {
                    html += `<div class="carousel-item active"><div class="row">`;
                } else {
                    html += `<div class="carousel-item"><div class="row">`;
                }
                for(let j= 0; j < 3; j++) {
                    if (cardCnt < cards.length) { // 0 > 5
                        html += cards[cardCnt];
                        cardCnt++;
                    } else {
                        break;
                    }
                }
                html += `</div></div>`;
            }
            html += `
                <button class="btn ml-2" data-target="#instructorCarousel" data-slide="next">
                    <i class="fas fa-chevron-right"></i>
                </button>`;
            $('#instructor-box').html(html);
        } else {
            $('#instructor-box').html(`<div class="text-center">데이터가 없습니다.</div>`);
        }
    }
    else if (cardData.length == 0) {
        $('#instructor-box').html(`<div class="text-center">데이터가 없습니다.</div>`);
    }
}
function callAlarm(coInstructorId, coInstructorName, coName) {
    let content = `${coInstructorName}님 ${coName} 훈련일지 작성해주세요.`;
    let isWarning = true;
    let targetURI = null;

    sendNotification([coInstructorId], content, isWarning, targetURI);
    Swal.fire({
                  title: `${coInstructorName}님에게 알림 전송완료`,
                  icon: "success",
                  draggable: true
              });
}

function getIsTodaySubmit(courseTrainingDates) {
    let todayDate = new Date();
    let todayDateStr = formatDate(todayDate);
    let isValid = false;
    // 오늘날짜로 제출한 훈련일지가 있는지
    courseTrainingDates.forEach(courseTrainingDate => {
        if(courseTrainingDate == todayDateStr) {
            isValid = true;
        }
    });
    return isValid;
}
function getIsTodayClassdate(classdateList) {
    let todayDate = new Date();
    let todayDateStr = formatDate(todayDate);
    let isValid = false;
    console.log(classdateList);
    classdateList.forEach((classdate) => {
        if (classdate == todayDateStr) {
            isValid = true;
        }
    });
    return isValid;
}
// (datesYYYYMMDD, course.courseTrainingDates, datesMD)
function generateData(datesYYYYMMDD, courseTrainingDates, datesMD) {
    var i = 0;
    var series = [];

    while (i < datesMD.length) { // 20번 루프
        var x = datesMD[i]; // 'w' + (i + 1).toString(); // x축 라벨: w1, w2...
        var y = 0;
        courseTrainingDates.forEach(date => {
            if (datesYYYYMMDD[i] == date) {
                y = 100;
            }
        });
        series.push({
                        x: x,
                        y: y
                    });
        i++;
    }
    return series;
}
function loadPartCourseSelect() {
    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) {
        courses = [];
    }
    loadPartCourseSelectOption("#part-course-select", courses);
}
function loadPartCourseSelectOption(selector, records) {
    const $select = $(selector).empty();
    records.forEach(record => {
        const $option = $("<option>")
        .val(record.courseWithAssignedInfo.coId)
        .text(record.courseWithAssignedInfo.coName);
        $select.append($option);

    });
    $select.find("option:first").prop("selected", true).trigger("change");
}
function handlePartCourseSelectChange() {
    $('#bar-chart').empty();
    let coId =
        $("#part-course-select").val() === "" ? null : $("#part-course-select").val();
    displayPartBarChart(coId);
}
function displayPartBarChart(coId) {
    let courses = coursesWithPaging?.records || [];
    if (!Array.isArray(courses)) courses = [];
    if(courses.length == 0) {
        console.log("진행중인 과정이 없는 상태");
        return;
    }
    const course = courses.find(
        course => course.courseWithAssignedInfo.coId == coId
    );

    // 데이터 가공: 필요한 정보 추출
    const rowData = course.courseLearnerOverview.learnerList.map(learner => (
        {
            x: learner.learnerUser.userFullname,
            y: learner.partOverview.attendanceRate,
            goals: [
                {
                    name: '과정평균출결률',
                    value: course.courseLearnerOverview.courseAvgAttendanceRate,
                    strokeWidth: 5,
                    strokeDashArray: 3,
                    strokeColor: '#1F7F4B'
                },
                {
                    name: '수료충족출결률',
                    value: 80,
                    strokeWidth: 5,
                    strokeHeight: 300,
                    strokeColor: '#1F7F4B'
                }
            ]
        }
    ));
    const extractedData = [{
        name: "실제출결률",
        data: rowData
    }];


    var options = {
        series: extractedData,
        chart: {
            height: 350,
            type: 'bar'
        },
        xaxis: {
            min: 0,
            max: 100,
            tickAmount: 10, // 0부터 100까지 10단위로 눈금 표시
            labels: {
                formatter: function(val) {
                    return val + '%'; // 값에 % 기호 추가
                }
            }
        },
        plotOptions: {
            bar: {
                horizontal: true,
            }
        },
        colors: ['#2DB87D'],
        dataLabels: {
            formatter: function(val, opt) {
                const goals =
                          opt.w.config.series[opt.seriesIndex].data[opt.dataPointIndex]
                              .goals

                if (goals && goals.length) {
                    return `${val} / ${goals[0].value}`
                }
                return val
            }
        },
        legend: {
            show: true,
            showForSingleSeries: true,
            customLegendItems: ['교육생 출결률', '평균/목표 출결률'],
            markers: {
                fillColors: ['#2DB87D', '#1F7F4B']
            }
        }
    };
    var chart = new ApexCharts(document.querySelector("#bar-chart"), options);
    chart.render();
}
// =================================================================================================
function getRecent4WeeksWeekdays() {
    const today = new Date();
    let baseDate = new Date(today);

    const dayOfWeek = baseDate.getDay();
    if (dayOfWeek === 6) { // 토요일
        baseDate.setDate(baseDate.getDate() - 1);
    } else if (dayOfWeek === 0) { // 일요일
        baseDate.setDate(baseDate.getDate() - 2);
    }
    // baseDate는 오늘 또는 가장 가까운 금요일

    const datesYYYYMMDD = [];
    const datesMD = [];
    const startDate = new Date(baseDate);
    startDate.setDate(baseDate.getDate() - 27);

    for (let d = new Date(startDate); d <= baseDate; d.setDate(d.getDate() + 1)) {
        const wd = d.getDay();
        if (wd >= 1 && wd <= 5) { // 월~금
            // YYYY-MM-DD
            datesYYYYMMDD.push(
                d.getFullYear() + '-' +
                String(d.getMonth() + 1).padStart(2, '0') + '-' +
                String(d.getDate()).padStart(2, '0')
            );
            // M/D
            datesMD.push((d.getMonth() + 1) + '/' + d.getDate());
        }
    }

    return { datesYYYYMMDD, datesMD };
}


// =================================================================================================


function updateLastExecutionTime() {
    axios.get('/api/scheduler/last-execution')
         .then(function(response) {
             const data = response.data;
             if (data) {
                 const formattedTime = new Date(data).toLocaleString('ko-KR');
                 $('#last-execution-time').text(`마지막 실행: ${formattedTime}`);
             } else {
                 $('#last-execution-time').text('마지막 실행: 기록 없음');
             }
         })
         .catch(function() {
             $('#last-execution-time').text('마지막 실행: 조회 실패');
         });
}
function updateLastExecutionTimeForPart() {
    axios.get('/api/scheduler/last-execution-for-part')
         .then(function(response) {
             const data = response.data;
             if (data) {
                 const formattedTime = new Date(data).toLocaleString('ko-KR');
                 $('#last-execution-time-for-part').text(`마지막 실행: ${formattedTime}`);
             } else {
                 $('#last-execution-time-for-part').text('마지막 실행: 기록 없음');
             }
         })
         .catch(function() {
             $('#last-execution-time-for-part').text('마지막 실행: 조회 실패');
         });
}
function handleTriggerSchedulerClick() {
    axios.post('/api/scheduler/trigger-end-course-process')
         .then(function (response) {
             updateLastExecutionTime();
         })
         .catch(function (error) {
             console.log(error);
         });
}
function handleTriggerSchedulerForPartClick() {
    axios.post('/api/scheduler/trigger-create-daily-attendance-records')
         .then(function (response) {
             updateLastExecutionTimeForPart();
         })
         .catch(function (error) {
             console.log(error);
         });
}

async function fetchAndDisplayClassroomUsage() {
    let classroomUsageList = await apiGetRequestParams(
        '/api/operationmanagement/classroom/usage', {});
    displayTimeLineChart(classroomUsageList);
}



function displayTimeLineChart(classroomUsageList) {
    // 1. 차트 컨테이너 초기화
    $('#timeline-chart').empty();

    // 2. 유효한 강의실 목록 추출 (중복 제거)
    const classroomNames = [...new Set(
        classroomUsageList
        .map(item => item.classroomName)
        .filter(name => name !== null)
    )];

    // 3. 강의실별 데이터 그룹화
    const groupedData = {};
    classroomNames.forEach(name => groupedData[name] = []);

    classroomUsageList.forEach(item => {

        // 날짜가 있는 경우만 데이터 추가
        if (item.startDate && item.endDate) {
            const startTime = new Date(item.startDate).getTime();
            const endTime = new Date(item.endDate).getTime();

            // 유효한 날짜인 경우에만 추가
            if (!isNaN(startTime) && !isNaN(endTime)) {
                groupedData[item.classroomName].push({
                     // 과정명 대신 강의실명 사용 (x 대신 메타데이터)
                     x: item.classroomName,
                     y: [
                         startTime,
                         endTime
                     ],
                     // 툴팁용 메타데이터
                     meta: {
                         courseName: item.courseName || '미지정 과정'
                     }
                 });
            }
        }
    });


    // 5. 날짜 범위 계산 (현재 기준 3개월 전 ~ 9개월 후)
    const today = new Date();
    const minDate = new Date(today);
    minDate.setMonth(today.getMonth() - 3);

    const maxDate = new Date(today);
    maxDate.setMonth(today.getMonth() + 9);

    // 6. 오늘 날짜 계산 (세로선 표시용)
    today.setHours(0, 0, 0, 0);

    // 4. 비어있는 강의실에 더미 데이터 추가
    classroomNames.forEach(name => {
        if (groupedData[name].length === 0) {
            groupedData[name].push({
                                       x: name,
                                       y: [minDate.getTime(), minDate.getTime()], // 0길이 막대
                                       meta: { courseName: '과정 없음' },
                                       fillColor: 'rgba(200,200,200,0.2)' // 연한 회색
                                   });
        }
    });

    // 5. 단일 series로 합치기 (ApexCharts 공식 구조)
    const chartData = [];
    classroomNames.forEach(name => {
        groupedData[name].forEach(d => chartData.push(d));
    });

    const colors = [
        '#4267D6', '#C93327', '#52CC5A', '#BFC513',
        '#6B71C3', '#468DC5', '#16B891', '#EF49B5',
        '#485244', '#64810E'
    ];

    // 7. ApexCharts 옵션 설정
    const options = {
        series: [{
            name: '강의실 사용현황',
            data: chartData
        }],
        chart: {
            type: 'rangeBar',
            height: 600,
            zoom: {
                enabled: false
            },
            toolbar: {
                show: true,
                tools: {
                    pan: false,
                    zoom: false,
                    zoomin: false,
                    zoomout: false,
                    reset: false
                },
            },
        },
        plotOptions: {
            bar: {
                horizontal: true,
                barHeight: '70%',
                rangeBarGroupRows: true,
                distributed: true
            }
        },
        colors: colors,
        xaxis: {
            type: 'datetime',
            min: minDate.getTime(),  // 3개월 전
            max: maxDate.getTime(),  // 9개월 후
            labels: {
                formatter: function(val) {
                    return new Date(val).toLocaleDateString();
                }
            }
        },
        yaxis: {
            type: 'category',
            title: { text: '강의실' },
            // y축 라벨에 강의실명 직접 표시
            labels: {
                formatter: function(value) {
                    return value; // 강의실명 그대로 반환
                }
            }

        },
        legend: { show: false },
        tooltip: {
            custom: function({ seriesIndex, dataPointIndex, w }) {
                const series = w.config.series[seriesIndex];
                const data = series.data[dataPointIndex];

                // 데이터가 없는 경우
                if (!data || !data.y) {
                    return `<div class="p-2">
                        <b>${series.name}</b><br>
                        <small>사용 중인 과정 없음</small>
                    </div>`;
                }

                const start = new Date(data.y[0]).toLocaleDateString();
                const end = new Date(data.y[1]).toLocaleDateString();

                return `<div class="p-2">
                    <b>${data.meta?.courseName || '과정명 없음'}</b><br>
                    ${start} ~ ${end}<br>
                    <small>${series.name}</small>
                </div>`;
            }
        },
        annotations: {
            xaxis: [{
                x: today.getTime(),
                borderColor: '#FF4560',
                borderWidth: 2,
                label: {
                    borderColor: '#FF4560',
                    style: {
                        color: '#fff',
                        background: '#FF4560'
                    },
                    text: '오늘',
                    orientation: 'horizontal',
                    position: 'top'
                }
            }]
        },
        dataLabels: {
            enabled: false // 과정명 표시 비활성화
        }
    };

    // 8. 차트 렌더링
    try {
        const chart = new ApexCharts(
            document.querySelector("#timeline-chart"),
            options
        );
        chart.render();

    } catch (e) {
        console.error("차트 렌더링 오류:", e);
    }
}
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}