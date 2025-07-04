$(function(){

    getCourseLists();
    getHomeData();

    $("#courseSelector").change(function(){
        getHomeData();
    });

    $(".participation-box").click(function(){
        location.href = "/learnerManagement/participationList";
    });

    $("#not-submit-test-learner-count-box").click(function(){
        location.href = "/test/testList";
    });

    $("#not-submit-homework-learner-count-box").click(function(){
       location.href = "/homework/homeworkList";
    });

    $("#training-log-card").click(function(){
        location.href = "/training/trainingList";
    });

    $("#not-eval-homework-card").click(function(){
        location.href = "/homework/homeworkList";
    });

    $("#not-approve-vacation-card").click(function(){
       location.href = "/learnerManagement/vacationApproval";
    });

});

function getCourseLists(){

    $.ajax({
               url: "/home/instructorHome/getCourseLists",
               type    : "GET",
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               // data       : JSON.stringify(courseId),
               // contentType: "application/json; charset=utf-8",
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   makeAndShowCourseSelector(data);

               },
               error   : function () {
               },
               complete: function () {
               }
           });

}

function makeAndShowCourseSelector(data){

    let output = ``;
    $(data).each(function(index, item){

        output += `<option value="${item.id}">${item.name}</option>`;

    });

    $("#courseSelector").html(output);

}

function getHomeData(){

    $.ajax({
               url: "/home/instructorHome/getHomeData",
               type    : "GET",
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               data       : {
                   courseId : $("#courseSelector").val()
                                           },
               // contentType: "application/json; charset=utf-8",
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   console.log(data);
                   renderHomeData(data);

               },
               error   : function () {
               },
               complete: function () {
               }
           });
}

function renderHomeData(data){

    $("#course-name").text(data.course.name);
    $("#course-period").text(data.course.startDate + " ~ " + data.course.endDate);
    $("#training-days").text(data.countOfCompletedDays + "일 /" + data.course.totalDays + "일");
    $("#number-of-learner").text(data.course.numberOfLearner + "명");
    $("#lesson-time").text(data.course.lessonStartTime + " ~ " + data.course.lessonEndTime);
    renderProgress(data);
    $("#absence-learner-count").text(data.absenceLearnerCount + "명");
    $("#in-study-learner-count").text(data.inStudyLearnerCount + "명");
    $("#not-submit-test-learner-count").text(data.notSubmitTestLearnerCount + "명");
    $("#not-submit-homework-learner-count").text(data.notSubmitHomeworkLearnerCount + "명");
    renderSchedule(data);
    renderTrainingLog(data);
    renderNotEvalHomework(data);
    renderNotApproveVacation(data);
    renderTestChartForInstructor(data);
    renderInquiriesForInstructor(data);
    renderQnAForInstructor(data);
    renderNoticeForInstructor(data);
    renderForumForInstructor(data);


}

function renderProgress(data){

    const progress = data.progress;

    const options = {
        chart: {
            type: 'donut',
            width: '100%',
            height: '100%'
        },
        series: [progress, 100 - progress],
        labels: ['진행률', '미진행'],
        colors: ['#00BFFF', '#aaaaaa'],
        plotOptions: {
            pie: {
                donut: {
                    size: '70%',
                    labels: {
                        show: true,
                        total: {
                            show: true,
                            label: '진행률',
                            fontSize: '16px',
                            fontWeight: 600,
                            formatter: () => `${progress}%`
                        },
                        name: {
                            show: true,
                            offsetY: -10
                        },
                        value: {
                            show: true,
                            offsetY: 10,
                            formatter: function (val) {
                                return `${val}%`;
                            }
                        }
                    }
                },
                states: {
                    hover: {
                        filter: {
                            type: 'none'
                        }
                    }
                }
            }
        },
        dataLabels: {
            enabled: false
        },
        legend: {
            show: false
        },
        tooltip: {
            enabled: false
        }
    };

    const chart = new ApexCharts(document.querySelector("#progress-chart"), options);
    chart.render();

}

function renderSchedule(data){

    const today = new Date();

    // 0: 일요일, 1: 월요일, ..., 6: 토요일
    const day = today.getDay();

    // 일요일이면 -6, 월요일이면 0, ... 토요일이면 -5
    const diffToMonday = day === 0 ? -6 : 1 - day;


    const weekdayNames = ['일', '월', '화', '수', '목', '금', '토'];

    for (let i = 0; i < 5; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + diffToMonday + i);

        const month = date.getMonth() + 1;
        const dayNum = date.getDate();
        const dayName = weekdayNames[date.getDay()];

        const formatted = `${month}/${dayNum}(${dayName})`;

        // yyyy-MM-dd 형식으로 만들기
        const yyyy = date.getFullYear();
        const mm = String(month).padStart(2, '0');
        const dd = String(dayNum).padStart(2, '0');
        const isoDate = `${yyyy}-${mm}-${dd}`;

        $("#schedule-date-box").find("div").eq(i).text(formatted);
        $("#schedule-date-box").find("div").eq(i).attr("data-date", isoDate);
        $("#schedule-box").find("div").eq(i).attr("data-date", isoDate);
    }

    $(data.courseScheduleVOS).each(function(index, item){

        for(let i = 0; i < 5; i++){
            if($("#schedule-box").find("div").eq(i).data("date") == item.classDate){
                $("#schedule-box").find("div").eq(i).text(item.subjectNames);
            }
        }

    });

    for(let i = 0; i < 5; i++){
        if($("#schedule-box").find("div").eq(i).text() == ""){
            $("#schedule-box").find("div").eq(i).addClass("text-danger").text("휴강");
        }
    }

}

function renderTrainingLog(data){
    if(!data.course.inProgress){
        $("#training-log").addClass("text-success").text("완료");
    }

    if(data.writeTrainingLogToday){
        $("#training-log").addClass("text-success").text("완료");
    } else {
        $("#training-log").addClass("text-danger").text("미완료");
    }
}

function renderNotEvalHomework(data){
    if(data.notEvalHomeworkCount > 0){
        $("#not-eval-homework").addClass("text-danger").text(data.notEvalHomeworkCount + "건");
    } else {
        $("#not-eval-homework").addClass("text-success").text("없음");
    }
}

function renderNotApproveVacation(data){
    if(data.notApproveVacationCount > 0){
        $("#not-approve-vacation").addClass("text-danger").text(data.notApproveVacationCount + "건");
    } else {
        $("#not-approve-vacation").addClass("text-success").text("없음");
    }
}

function renderTestChartForInstructor(data){

    let studentArr = [];
    let studentScoreArr = [];
    let averageSeries = [];
    let stdDevSeries = [];
    let categories = [];

    data.enrolledLearnerVOS.forEach(learner => {
       studentArr.push(learner.fullname);
       studentScoreArr.push([]);
    });

    $(data.customTestDTOS).each(function(i, test) {
       categories.push(test.title);
       averageSeries.push(test.average);
       stdDevSeries.push(test.standardDeviation);

       $(test.customTestSubmissionDTOS).each(function(j, submission) {
           studentScoreArr[j][i] = submission.score;
       });
    });

    let series = [];

    for(let i = 0; i < studentArr.length; i++){
        let tmpObj = {
            name: studentArr[i],
            type: 'column',
            data: studentScoreArr[i]
        }
        series.push(tmpObj);
    }

    series.push({
        name: '평균',
        type: 'line',
        color: '#FF9800',
        data: averageSeries
                });
    series.push({
        name: '표준편차',
        type: 'line',
        color: '#F44336',
        data: stdDevSeries
                });

    let yaxis = [];

    for(let i = 0; i < studentArr.length; i++){

        let isShow = (i === 0);
        yaxis.push({
                       seriesName: studentArr[i],
                       axisTicks: {
                           show: isShow,
                       },
                       axisBorder: {
                           show: isShow,
                           color: '#008FFB'
                       },
                       labels: {
                           show: isShow,
                           style: {
                               colors: '#008FFB',
                           }
                       },
                       title: {
                           text: isShow ? "성적" : "",
                           style: {
                               color: '#008FFB',
                           }
                       },
                       tooltip: {
                           enabled: true
                       }
                   });
    }
    yaxis.push({
                   seriesName: "평균",
                   opposite: true,
                   axisTicks: {
                       show: true,
                   },
                   axisBorder: {
                       show: true,
                       color: '#FF9800'
                   },
                   labels: {
                       style: {
                           colors: '#FF9800',
                       }
                   },
                   title: {
                       text: "평균",
                       style: {
                           color: '#FF9800',
                       }
                   },
                   tooltip: {
                       enabled: true
                   }
               });
    yaxis.push({
                   seriesName: "표준편차",
                   opposite: true,
                   axisTicks: {
                       show: true,
                   },
                   axisBorder: {
                       show: true,
                       color: '#F44336'
                   },
                   labels: {
                       style: {
                           colors: '#F44336',
                       }
                   },
                   title: {
                       text: "표준편차",
                       style: {
                           color: '#F44336',
                       }
                   },
                   tooltip: {
                       enabled: true
                   }
               })



    var options = {
        series: series,
        chart: {
            width: '100%',
            height: '100%',
            type: 'line',
            stacked: false
        },
        dataLabels: {
            enabled: false
        },
        stroke: {
            width: [1, 1, 4]
        },
        title: {
            text: '시험별 성적 현황',
            align: 'left',
            offsetX: 110
        },
        xaxis: {
            categories: categories,
        },
        yaxis: yaxis,
        tooltip: {
            fixed: {
                enabled: true,
                position: 'topLeft', // topRight, topLeft, bottomRight, bottomLeft
                offsetY: 30,
                offsetX: 60
            },
        },
        legend: {
            horizontalAlign: 'left',
            offsetX: 40
        }
    };

    var chart = new ApexCharts(document.querySelector("#test-chart"), options);
    chart.render();
}

function renderInquiriesForInstructor(data){
    let output = ``;

    if(data.customInquiryVOS.length != 0){
        data.customInquiryVOS.forEach(inquiry => {
           output += `
                  <tr>
                    <td class="align-middle"><a href="/communityInquiry/inquiryDetail?id=${inquiry.id}">${inquiry.title}</a></td>
                    <td class="text-center align-middle ${inquiry.answered ? 'text-info' : 'text-danger'}">${inquiry.answered ? '답변완료' : '답변대기'}</td>
                    <td class="text-center align-middle">${inquiry.createdAt}</td>
                  </tr>
           `;
        });
    } else {
        output += `<tr><td colspan="4" class="text-center">현재 문의중인 글이 없습니다.</td></tr>`;
    }

    $("#inquiry-body").html(output);
}

function renderQnAForInstructor(data){

    let output = ``;

    if(data.customCourseQnAVOS.length != 0){
        data.customCourseQnAVOS.forEach(courseQnA => {
            output += `
                  <tr>
                    <td class="align-middle"><a href="/courseBoardQnA/detail/${courseQnA.id}">${courseQnA.title}</a></td>
                    <td class="text-center align-middle">${courseQnA.writerName}</td>
                    <td class="text-center align-middle">${courseQnA.createdAt}</td>
                  </tr>
           `;
        });
    } else {
        output += `<tr><td colspan="4" class="text-center">미답변 질문글이 없습니다.</td></tr>`;
    }

    $("#qna-body").html(output);

}

function renderNoticeForInstructor(data){

    let output = ``;

    if(data.customCourseNoticeVOS.length != 0){
        data.customCourseNoticeVOS.forEach(courseNotice => {
                output += `
                  <tr ${courseNotice.fixed ? 'class="pinned-post"' : ''}>
                    <td class="align-middle"><a href="/courseBoardMaterials/materialsDetail?id=${courseNotice.id}">${courseNotice.title}</a></td>
                    <td class="text-center align-middle">${courseNotice.writerName}</td>
                    <td class="text-center align-middle">${courseNotice.createdAt}</td>
                  </tr>
           `;
            });
    } else {
        output += `<tr><td colspan="4" class="text-center">등록된 공지글이 없습니다.</td></tr>`;
    }

    $("#notice-body").html(output);
}

function renderForumForInstructor(data){

    let output = ``;

    if(data.customCourseForumVOS.length != 0){
        data.customCourseForumVOS.forEach(courseForum => {
                output += `
                  <tr>
                    <td class="align-middle"><a href="/courseBoardDebate/debateDetail?id=${courseForum.id}">${courseForum.title}</a><span class="badge rounded-pill bg-light text-secondary border border-secondary ms-2"><i class="fa fa-comment me-1" aria-hidden="true">${courseForum.commentCount}</i></span></td>
                    <td class="text-center align-middle">${courseForum.writerName}</td>
                    <td class="text-center align-middle">${courseForum.forumLike}</td>
                    <td class="text-center align-middle">${courseForum.createdAt}</td>
                  </tr>
           `;
            });
    } else {
        output += `<tr><td colspan="4" class="text-center">등록된 인기글이 없습니다.</td></tr>`;
    }

    $("#forum-body").html(output);
}