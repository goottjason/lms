const $courseSelect = $("#courseSelector");

const $progressRate   = $("#progress-rate");
const $attendanceRate = $("#attendance-rate");
const $avgTestScore   = $("#avg-test-score");
const $hwScore        = $("#hw-score");

let selectedCourse;
let inProg;

let progressSummary;

let userId;

$(document).ready(async () => {

    userId = $("#login-user-id").val();

    await getUserCourses();               // select 옵션 셋업
    selectedCourse = $courseSelect.val(); // 기본값 설정
    await loadCourseData(selectedCourse); // 첫 렌더
});

//------------------------------------------------------------------------------
// [[Progress Summary render]]
//------------------------------------------------------------------------------

function renderProgressSummary(summary) {
    // 값 파싱
    const progVal  = summary.progressRate;
    // %와 한글 문자를 제거하고 숫자만 파싱
    const attVal   = parseFloat(summary.attendanceRate.replace(/[^\d.]/g, ""));
    const testVal  = parseFloat(summary.avgTestScore.replace(/[^\d.]/g, ""));
    const [hwDone] = summary.hwScore.split("/").map(s => parseInt(s, 10));

    // 기준 충족 여부 (progress 제외)
    const meets = {
        att : attVal >= 60,
        test: testVal >= 60,
        hw  : hwDone >= 1
    };

    // 각 항목 설정
    const items = [
        {
            key         : "prog",
            selector    : "#progress-rate",
            color       : "primary",
            alwaysFilled: true
        },
        {key: "att", selector: "#attendance-rate", color: "info"},
        {key: "test", selector: "#avg-test-score", color: "secondary"},
        {key: "hw", selector: "#hw-score", color: "warning"}
    ];

    items.forEach(({key, selector, color, alwaysFilled}) => {
        const $num = $(selector);
        const $box = $num.closest(".circle-box");

        if (alwaysFilled) {
            // 과정진행률: 항상 꽉 찬 배경
            $box
            .removeClass(
                `border border-${color} border-3 bg-transparent text-dark`)
            .addClass(`bg-${color} text-white`);
            $num
            .removeClass("text-dark")
            .addClass("text-white")
            .text(progVal);
        } else {
            // 기준 충족 시 꽉 찬 원, 미충족 시 두꺼운 테두리 원
            const ok = meets[key];
            if (ok) {
                $box
                .removeClass(`border border-${color} border-3 bg-transparent`)
                .addClass(`bg-${color} text-white`);
                $num
                .removeClass("text-dark")
                .addClass("text-white");
            } else {
                $box
                .removeClass(`bg-${color} text-white`)
                .addClass(`border border-${color} border-4 bg-transparent`);
                $num
                .removeClass("text-white")
                .addClass("text-dark");
            }
            // 텍스트 업데이트
            if (key === "att") {
                $num.text(summary.attendanceRate);
            }
            if (key === "test") {
                $num.text(summary.avgTestScore);
            }
            if (key === "hw") {
                $num.text(summary.hwScore);
            }
        }
    });
}

//------------------------------------------------------------------------------
// [[Deadlines render]]
//------------------------------------------------------------------------------

function renderDeadlineCarousel(data) {
    const $inner = $("#deadlineCarousel .carousel-inner");
    $inner.empty();

    if (!Array.isArray(data) || data.length === 0) {
        const $emptyItem = $("<div>")
        .addClass("carousel-item active");
        const $row       = $("<div>").addClass("row");
        const $col       = $("<div>")
        .addClass("col-12 text-center py-5 text-muted")
        .text("등록된 과제나 시험이 없습니다.");
        $row.append($col);
        $emptyItem.append($row);
        $inner.append($emptyItem);
        return;
    }

    const itemsPerSlide = 3;
    const slideCount    = Math.ceil(data.length / itemsPerSlide);

    const testStatusMap = {
        NOT_STARTED: {text: "응시 전", class: "text-danger"},
        IN_PROGRESS: {text: "응시 중", class: "text-warning"},
        COMPLETED  : {text: "완료", class: "text-success"}
    };

    function formatDate(dateStr) {
        const d  = new Date(dateStr);
        const mm = String(d.getMonth() + 1).padStart(2, "0");
        const dd = String(d.getDate()).padStart(2, "0");
        return `${d.getFullYear()}-${mm}-${dd}`;
    }

    for (let i = 0; i < slideCount; i++) {
        const slice = data.slice(i * itemsPerSlide,
                                 i * itemsPerSlide + itemsPerSlide);
        const $item = $("<div>")
        .addClass("carousel-item")
        .toggleClass("active", i === 0);
        const $row  = $("<div>").addClass("row");

        slice.forEach(card => {
            const isHw     = card.contentType === "homework";
            const typeText = isHw ? "과제" : "시험";

            // 카드 색상 구분
            const borderClass = isHw ? "border-info" : "border-primary";
            const badgeClass  = isHw ? "bg-info" : "bg-primary";
            const typeColor   = isHw ? "text-info" : "text-primary";

            // 상태 텍스트
            let statusText,
                statusClass;
            if (isHw) {
                if (card.submissionStatus === "제출") {
                    statusText  = "제출";
                    statusClass = "text-info";
                } else {
                    statusText  = "미제출";
                    statusClass = "text-danger";
                }
            } else {
                const m     = testStatusMap[card.submissionStatus]
                              || {
                        text: card.submissionStatus,
                        class: "text-secondary"
                    };
                statusText  = m.text;
                statusClass = m.class;
            }

            const $col  = $("<div>").addClass("col-md-4");
            const $card = $("<div>").addClass(
                `deadline-card card text-center shadow-sm h-100 ${borderClass}`
            );
            const $body = $("<div>").addClass(
                "card-body d-flex flex-column px-4 py-3"
            );

            // 상단 타입 배지
            $body.append(
                $("<div>").addClass("mb-3").append(
                    $("<span>")
                    .addClass(
                        `badge ${badgeClass} text-white px-3 py-1 rounded-pill`)
                    .text(typeText)
                )
            );

            // 제목 + (과제 평가 상태)
            const $titleLine = $("<div>").append(
                $("<h6>")
                .addClass("card-title mb-1 fw-bold")
                .css("font-size", "1rem")
                .text(card.title)
                .addClass(typeColor)
            );
            if (isHw) {
                const evalBadge = card.score === 1
                                  ? $("<span>").addClass(
                        "badge border-success text-success bg-transparent ms-2")
                                               .text(
                                                   "평가완료")
                                  : $("<span>").addClass(
                        "badge border-warning text-warning bg-transparent ms-2")
                                               .text(
                                                   "평가대기");
                $titleLine.append(evalBadge);
            }
            $body.append($titleLine);

            // 상태 라인
            $body.append(
                $("<p>")
                .addClass(`small mb-2 ${statusClass}`)
                .text(statusText)
            );

            // 상세보기 버튼
            const $btn = $("<button>")
            .addClass("btn btn-sm btn-block btn-primary mt-auto")
            .attr("data-id", card.id)
            .attr("data-type", card.contentType)
            .attr("data-enddate", card.endDate)
            .text("상세보기");
            if (isHw && card.submissionStatus !== "제출") {
                $btn.prop("disabled", true).removeClass("btn-primary").addClass(
                    "btn-secondary");
            }
            $body.append($btn);

            $card.append($body);
            $col.append($card);
            $row.append($col);
        });

        $item.append($row);
        $inner.append($item);
    }

    // 클릭 핸들러
    $("#deadlineCarousel")
    .off("click", "button[data-id]")
    .on("click", "button[data-id]", function () {
        const id   = $(this).data("id");
        const type = $(this).data("type");

        if (type === "homework") {
            location.href = `/homework/submissionDetail?submissionId=${id}`;
            return;
        }

        // test일 때
        let url = `/test/learner/testDetail/${id}`
                  + `?userId=${userId}`
                  + `&currentPageNo=1`
                  + `&courseName=${selectedCourse}`;

        // endDate 파싱해서 지나갔으면 쿼리스트링 추가
        const endDate = new Date($(this).data("enddate"));
        if (endDate < new Date()) {
            url += `&testStatus=종료`;
        }

        location.href = url;
    });
}

//------------------------------------------------------------------------------
// [[Attendance render]]
//------------------------------------------------------------------------------

function renderAttendance(data, inProg) {
    const $card       = $(".card-header:contains(\"Attendance\")").closest(".card");
    const $theadRow   = $card.find("table thead tr");
    const $tbody      = $card.find("table tbody");
    const $countSpans = $card.find(".mt-2 span > span");

    // 1) 헤더 날짜 렌더링
    $theadRow.empty();
    data.forEach(item => {
        // "2025-07-07T00:00:00" → "2025-07-07"
        const [datePart] = item.participationDate.split("T");
        const [y, m, d]  = datePart.split("-").map(Number);
        const weekdayMap = ["일","월","화","수","목","금","토"];
        const weekday    = weekdayMap[new Date(y, m-1, d).getDay()];
        $theadRow.append(
            $("<th>")
            .addClass("text-center")
            .text(`${m}/${d}(${weekday})`)
        );
    });

    // 2) 기존 초기화 로직
    $tbody.empty();
    $countSpans.text("-");
    if (!inProg) {
        const $emptyRow = $("<tr>").append(
            $("<td>")
            .attr("colspan", 5)
            .addClass("text-center py-4 text-muted")
            .text("현재 진행중이지 않은 과정입니다.")
        );
        $tbody.append($emptyRow);
        return;
    }

    // 진행중인 과정일 때만 기존 로직 수행
    const stats = {attendance: 0, absence: 0, vacation: 0, late: 0, early: 0};
    const $row  = $("<tr>");

    data.forEach(item => {
        let iconHtml = "-";
        switch (item.status) {
            case "ATTENDANCE":
                iconHtml = "<i class=\"fa fa-check text-info\"></i>";
                stats.attendance++;
                break;
            case "ABSENCE":
                if (item.checkIn || item.checkOut) {
                    iconHtml = "<i class=\"fa fa-times text-danger\"></i>";
                    stats.absence++;
                }
                break;
            case "IN_STUDY":
                iconHtml = "<i class=\"fa fa-sign-in-alt text-primary\"></i>";
                break;
            case "LATE":
                iconHtml = "<i class=\"fa fa-clock text-warning\"></i>";
                stats.late++;
                break;
            case "VACATION_PENDING":
            case "VACATION":
                iconHtml = "<i class=\"fa fa-plane text-primary\"></i>";
                stats.vacation++;
                break;
            // default: 미등록 등은 '-'
        }
        $row.append(
            $("<td>").addClass("text-center align-middle").html(iconHtml));
    });

    $tbody.append($row);

    // 하단 통계 반영
    $countSpans.eq(0).text(stats.attendance || "-");
    $countSpans.eq(1).text(stats.absence || "-");
    $countSpans.eq(2).text(stats.vacation || "-");
    $countSpans.eq(3).text(stats.late || "-");
    $countSpans.eq(4).text(stats.early || "-");
}

//------------------------------------------------------------------------------
// [[Schedule render]]
//------------------------------------------------------------------------------

function renderSchedule(data, inProg) {
    const $card      = $(".card-header:contains(\"Schedule\")")
    .closest(".card");
    const $body      = $card.find(".card-body");
    const $headerRow = $body.find(".row").first();
    const $bodyRow   = $body.find(".row").eq(1);

    // 진행중이지 않은 과정일 때
    if (!inProg) {
        // 기존 스케줄 영역 비우고
        $body.empty();
        // 안내 메시지 한 줄만 추가
        $body.append(
            $("<div>").addClass("row").append(
                $("<div>")
                .addClass("col-12 text-center py-4 text-muted")
                .text("현재 진행중이지 않은 과정입니다.")
            )
        );
        return;
    }

    // 진행중인 과정일 때: 기존 렌더링 로직

    // 초기화
    $headerRow.empty();
    $bodyRow.empty();

    // 날짜 → 스케줄 데이터 순회
    data.forEach(item => {
        const dt         = new Date(item.classDate);
        const month      = dt.getMonth() + 1;
        const day        = dt.getDate();
        const weekdayMap = ["일", "월", "화", "수", "목", "금", "토"];
        const weekday    = weekdayMap[dt.getDay()];
        const dateText   = `${month}/${day}(${weekday})`;

        // 헤더 셀
        $headerRow.append(
            $("<div>")
            .addClass("col border p-2 bg-gray-100 text-center font-weight-bold")
            .text(dateText)
        );

        // 바디 셀
        const $cell = $("<div>").addClass("col border p-2 text-center");
        if (!item.subjectNames) {
            $cell.addClass("text-danger").text("휴강");
        } else {
            item.subjectNames.split(",").forEach((subj, idx) => {
                if (idx) {
                    $cell.append("<br>");
                }
                $cell.append(document.createTextNode(subj.trim()));
            });
        }
        $bodyRow.append($cell);
    });
}

//------------------------------------------------------------------------------
// [[게시판 조회 render]]
//------------------------------------------------------------------------------

function renderInquiries(data) {
    const $tbody = $("#inquiry .inquiry-table tbody");
    $tbody.empty();

    if (!Array.isArray(data) || data.length === 0) {
        $tbody.append(
            $("<tr>").append(
                $("<td>")
                .attr("colspan", 3)
                .addClass("text-center py-4 text-muted")
                .text("등록된 문의가 없습니다.")
            )
        );
        return;
    }

    data.forEach(item => {
        const $tr = $("<tr>").addClass("text-center");

        // 1) 제목: 링크로 감싸기
        const $titleLink = $("<a>")
        .attr("href", `/communityInquiry/inquiryDetail?id=${item.id}`)
        .text(item.title);
        const $titleTd   = $("<td>")
        .addClass("align-middle text-start") // 왼쪽 정렬
        .append($titleLink);

        // 답변 여부 배지
        const $statusTd   = $("<td>").addClass("align-middle");
        const statusBadge = item.isAnswered
                            ? $("<span>")
                            .addClass(
                                "badge rounded-pill border border-info text-info bg-transparent")
                            .text("답변완료")
                            : $("<span>")
                            .addClass(
                                "badge rounded-pill border border-warning text-warning bg-transparent")
                            .text("답변대기");
        $statusTd.append(statusBadge);

        // 작성일
        const $dateTd = $("<td>")
        .addClass("align-middle")
        .text(item.createdAt);

        $tr.append($titleTd, $statusTd, $dateTd);
        $tbody.append($tr);
    });
}

//------------------------------------------------------------------------------

function renderQna(data) {
    const $tbody = $("#qna .table tbody");
    $tbody.empty();

    // 데이터 없을 경우 안내
    if (!Array.isArray(data) || data.length === 0) {
        $tbody.append(
            $("<tr>").append(
                $("<td>")
                .attr("colspan", 3)
                .addClass("text-center py-4 text-muted")
                .text("등록된 질문이 없습니다.")
            )
        );
        return;
    }

    data.forEach(item => {
        const $tr = $("<tr>").addClass("text-center");

        // 1) 제목 (링크)
        const $titleLink = $("<a>")
        .attr("href", `/courseBoardQnA/detail/${item.id}`)
        .text(item.title);
        const $titleTd   = $("<td>")
        .addClass("align-middle text-start")
        .append($titleLink);

        // 2) 답변여부 배지
        const $statusTd   = $("<td>").addClass("align-middle");
        const statusBadge = item.isAnswer
                            ? $("<span>")
                            .addClass(
                                "badge rounded-pill border border-info text-info bg-transparent")
                            .text("답변완료")
                            : $("<span>")
                            .addClass(
                                "badge rounded-pill border border-warning text-warning bg-transparent")
                            .text("답변대기");
        $statusTd.append(statusBadge);

        // 3) 작성일
        const $dateTd = $("<td>")
        .addClass("align-middle")
        .text(item.createdAt);

        $tr.append($titleTd, $statusTd, $dateTd);
        $tbody.append($tr);
    });
}

//------------------------------------------------------------------------------

function renderMaterials(data) {
    const $tbody = $("#materials .table tbody");
    $tbody.empty();

    // 데이터 없으면 안내
    if (!Array.isArray(data) || data.length === 0) {
        $tbody.append(
            $("<tr>").append(
                $("<td>")
                .attr("colspan", 3)
                .addClass("text-center py-4 text-muted")
                .text("등록된 자료가 없습니다.")
            )
        );
        return;
    }

    data.forEach(item => {
        // isFixed 가 true 이면 pinned-post 클래스 추가
        const $tr = $("<tr>")
        .addClass("text-center")
        .toggleClass("pinned-post", item.isFixed);

        // 제목 (링크)
        const $titleLink = $("<a>")
        .attr("href", `/courseBoardMaterials/materialsDetail?id=${item.id}`)
        .text(item.title);
        const $titleTd   = $("<td>")
        .addClass("align-middle text-start")
        .append($titleLink);

        // 작성자
        const $writerTd = $("<td>")
        .addClass("align-middle")
        .text(item.fullname || "-");

        // 작성일 (YYYY-MM-DD)
        const dateOnly = item.createdAt.split("T")[0];
        const $dateTd  = $("<td>")
        .addClass("align-middle")
        .text(dateOnly);

        $tr.append($titleTd, $writerTd, $dateTd);
        $tbody.append($tr);
    });
}

//------------------------------------------------------------------------------

function renderDebate(data) {
    const $tbody = $("#debate .table tbody");
    $tbody.empty();

    // 데이터 없을 경우 안내
    if (!Array.isArray(data) || data.length === 0) {
        $tbody.append(
            $("<tr>").append(
                $("<td>")
                .attr("colspan", 4)
                .addClass("text-center py-4 text-muted")
                .text("등록된 토론글이 없습니다.")
            )
        );
        return;
    }

    data.forEach(item => {
        const $tr = $("<tr>").addClass("text-center");

        // 제목 (링크) + 댓글 수 뱃지
        const $titleTd = $("<td>").addClass("align-middle text-start");
        $titleTd.append(
            $("<a>")
            .attr("href", `/courseBoardDebate/debateDetail?id=${item.id}`)
            .text(item.title)
        );
        $titleTd.append(
            $("<span>")
            .addClass(
                "badge rounded-pill bg-light text-secondary border border-secondary ms-2")
            .append(
                $("<i>").addClass("fa fa-comment me-1")
                        .attr("aria-hidden", "true"),
                document.createTextNode(item.commentCount)
            )
        );

        // 작성자
        const $writerTd = $("<td>")
        .addClass("align-middle")
        .text(item.fullname || "-");

        // 좋아요
        const $likeTd = $("<td>")
        .addClass("align-middle")
        .text(item.forumLike);

        // 작성일 (YYYY-MM-DD)
        const dateOnly = item.createdAt.split("T")[0];
        const $dateTd  = $("<td>")
        .addClass("align-middle")
        .text(dateOnly);

        $tr.append($titleTd, $writerTd, $likeTd, $dateTd);
        $tbody.append($tr);
    });
}

//------------------------------------------------------------------------------
// [[Test Chart Render]]
//------------------------------------------------------------------------------

function renderTestChart(data) {
    const $container = $(".test-chart");
    // 이전 차트나 메시지 초기화
    $container.empty();

    // 데이터가 없으면 안내 메시지 표시
    if (!Array.isArray(data) || data.length === 0) {
        $container
        .append(
            $("<div>")
            .addClass("text-center text-muted py-5")
            .text("시험 통계 데이터가 없습니다.")
        );
        return;
    }

    // 데이터가 있으면, 차트를 넣을 <div> 생성
    const chartDiv = $("<div>").get(0);
    $container.append(chartDiv);

    // 카테고리·시리즈 추출
    const categories  = data.map(d => d.title);
    const myScores    = data.map(d => d.myScore);
    const avgScores   = data.map(d => d.avgScore);
    const percentiles = data.map(d => d.percentile);

    // 옵션 구성
    const options = {
        series    : [
            {name: "내 점수", type: "column", data: myScores},
            {name: "반 평균 점수", type: "column", data: avgScores},
            {name: "백분위", type: "line", data: percentiles}
        ],
        chart     : {height: 350, type: "line", stacked: false},
        dataLabels: {enabled: false},
        stroke    : {width: [1, 1, 4]},
        title     : {text: "시험 성적 분석", align: "left"},
        xaxis     : {categories},
        yaxis     : [
            {
                seriesName: "내 점수",
                axisTicks : {show: true},
                axisBorder: {show: true, color: "#008FFB"},
                labels    : {style: {colors: "#008FFB"}},
                title     : {text: "내 점수", style: {color: "#008FFB"}},
                tooltip   : {enabled: true}
            },
            {
                seriesName: "반 평균 점수",
                opposite  : true,
                axisTicks : {show: true},
                axisBorder: {show: true, color: "#00E396"},
                labels    : {style: {colors: "#00E396"}},
                title     : {text: "반 평균 점수", style: {color: "#00E396"}}
            },
            {
                seriesName: "백분위",
                opposite  : true,
                axisTicks : {show: true},
                axisBorder: {show: true, color: "#FEB019"},
                labels    : {style: {colors: "#FEB019"}},
                title     : {text: "백분위", style: {color: "#FEB019"}}
            }
        ],
        tooltip   : {
            fixed: {
                enabled : true,
                position: "topLeft",
                offsetY : 30,
                offsetX : 60
            }
        },
        legend    : {horizontalAlign: "left", offsetX: 40}
    };

    // 렌더링
    const chart = new ApexCharts(chartDiv, options);
    chart.render();
}

//------------------------------------------------------------------------------
// [[공통 render 함수]]
//------------------------------------------------------------------------------

async function loadCourseData(courseName) {
    // 1) inProg 갱신
    inProg          = $courseSelect.find("option:selected")
                                   .data("is-in-progress");
    progressSummary = {
        "progressRate"  : "",
        "attendanceRate": "",
        "avgTestScore"  : "",
        "hwScore"       : ""
    };
    // 진행률/출석률/시험·과제 점수 불러오기
    const rateRes   = await fetchAttendanceRateCourseProgressRate(courseName);
    console.log(rateRes);
    const testHwScoreRes = await fetchTestHwScore(courseName);
    console.log(testHwScoreRes);
    progressSummary.progressRate   = rateRes.data.data.progressRate + "%";
    progressSummary.attendanceRate = rateRes.data.data.attendanceRate + "%";
    progressSummary.avgTestScore   = testHwScoreRes.data.data.testAvgScore +
                                     "점";
    progressSummary.hwScore        = testHwScoreRes.data.data.learner_homework_cnt
                                     + "/"
                                     +
                                     testHwScoreRes.data.data.homework_total_cnt;
    renderProgressSummary(progressSummary);

    // 마감 일정
    const scheduleRes = await fetchTestHwSchedule(courseName);
    console.log(scheduleRes);
    renderDeadlineCarousel(scheduleRes.data.data);

    // 출석표
    const attendanceRes = await fetchAttendanceStatus(courseName);
    console.log(attendanceRes);
    renderAttendance(attendanceRes.data.data, inProg);

    // 수업 스케줄
    const courseScheduleRes = await fetchCourseSchedule(courseName);
    console.log(courseScheduleRes);
    renderSchedule(courseScheduleRes.data.data, inProg);

    // Inquiry / QnA / 자료 / 토론
    const [inqRes, qnaRes, matRes, forumRes] = await Promise.all([
                                                                     fetchInquiry(),
                                                                     fetchQnA(),
                                                                     fetchNotice(
                                                                         courseName),
                                                                     fetchForum(
                                                                         courseName)
                                                                 ]);

    console.log(qnaRes);
    renderInquiries(inqRes.data.data);
    renderQna(qnaRes.data.data);
    renderMaterials(matRes.data.data);
    renderDebate(forumRes.data.data);

    // 시험 통계 차트
    const statRes = await fetchTestStatistic(courseName);
    console.log(statRes);
    renderTestChart(statRes.data.data);
}

//------------------------------------------------------------------------------
// [[과정 변경 이벤트]]
//------------------------------------------------------------------------------

$courseSelect.on("change", async function () {

    const $opt   = $(this).find("option:selected");
    const inProg = $opt.attr("data-is-in-progress");
    console.log(inProg);
    console.log($(this).val());
    selectedCourse = $(this).val();
    await loadCourseData(selectedCourse);

});

//------------------------------------------------------------------------------
// [[필터]]
//------------------------------------------------------------------------------

// 사용자(수강생/강사) 필터 호출
function getUserCourses() {

    return fetchUserCourses()
    .then((res) => {
        console.log(res);
        renderUserCourseOptions("#courseSelector", res.data.data);
    })
    .catch((err) => console.log(err));
}

// 사용자(수강생/강사) 필터 생성
function renderUserCourseOptions(selector, data) {
    let $adminCourseSelect = $(selector);
    $adminCourseSelect.empty();

    // 진행중인 강좌 요소 찾기
    const defaultEl          = data.find(el => el.inProgress) || data[0];
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

