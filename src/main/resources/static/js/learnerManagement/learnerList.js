const loginUserId   = $("#login-user-id").val();
const loginUserType = $("#login-user-type").val();

let baseConfig = {
    loginUserId  : loginUserId,
    loginUserType: loginUserType
};
let learnerConfig = {
    pageNo        : 1,
    pageSize      : 8,
    type          : "userFullname",
    keyword       : null,
    orderBy       : "userFullname",
    orderDirection: "ASC",
    // 필터링
    coIsInProgress: null,
    leCourseId    : null,
    leId          : null,
    // 라디오필터 (all: 전체, onlyNotEnrolled, onleDropped, onlyInProgress)
    radioFilter   : "onlyInProgress",
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
    coIsInProgress: null,
    coId          : null
};

$(document).ready(() => {

    // 세션 관리
    window.addEventListener("beforeunload", function (e) {
        // 외부로 이동 시 config 데이터 삭제
        if (!sessionStorage.getItem("isEnteringDetail")) {
            sessionStorage.removeItem("courseTopConfig");
            sessionStorage.removeItem("courseConfig");
            sessionStorage.removeItem("learnerConfig");
        }
        // 내부로 이동 시 플래그만 삭제 (목록으로 돌아와도 config 정보는 남아있음)
        else {
            sessionStorage.removeItem("isEnteringDetail");
        }
    });
    // 세션 정보 불러오기
    getStatus();

    // 페이지 로드시, 라디오버튼, 셀렉트박스 및 검색창 유지
    $('#courseSelector').val(courseTopConfig.coId);
    let radioFilterStatus = learnerConfig.radioFilter == null ? '' : learnerConfig.radioFilter;
    $(`input[name="learner-filter"][value="${radioFilterStatus}"]`).prop('checked', true);
    $('#is-in-progress').val(courseConfig.coIsInProgress);
    $('#course-select').val(courseConfig.coId);
    $('#search-input').val(learnerConfig.keyword);

    // 강사 -> 상단셀렉트박스O, 중간셀렉트박스X (교육생 -> 접근 불가)
    if (loginUserType == "INSTRUCTOR") {
        // 상단셀렉트박스 로드
        fetchAndLoadTopCourseSelector();
        // 교육생 리스트 로드
        fetchAndDisplayLearners();
        // 이벤트 핸들러 (only INSTRUCTOR)
        $(document).on("change", "#courseSelector", handleCourseTopSelectorChange);
    }
    // 관리자 -> 상단셀렉트박스X, 중간셀렉트박스O
    else {
        // 상단셀렉트박스 삭제
        $("#courseSelector").hide();
        // 중간셀렉트박스 로드
        fetchAndLoadCourseSelect();
        // 교육생 리스트 로드
        fetchAndDisplayLearners();
        // 이벤트 핸들러 (only ADMINISTRATOR)
        $(document).on("change", "#is-in-progress", handleIsInProgressSelectChange);
        $(document).on("change", "#course-select", handleCourseSelectChange);
    }

    // 이벤트 핸들러 (공통)
    $(document).on("change", "input[name='learner-filter']", handleLearnerFilterChange);
    $(document).on("click", "#search-button", handleSearchButtonClick);
    $(document).on("keydown", "#search-input", function (e) {
        if (e.key == "Enter") {
            e.preventDefault();
            handleSearchButtonClick();
        }
    });
    $(document).on("click", ".page-link", handlePageButtonClick);
});

function getStatus() {

    let courseTopStatusByUser = sessionStorage.getItem("courseTopConfig");

    if (courseTopStatusByUser) {
        const parsedCourseTopConfig = JSON.parse(
            sessionStorage.getItem("courseTopConfig"));
        Object.assign(courseTopConfig, parsedCourseTopConfig);
    }

    let courseStatusByUser = sessionStorage.getItem("courseConfig");

    if (courseStatusByUser) {
        const parsedCourseConfig = JSON.parse(
            sessionStorage.getItem("courseConfig"));
        Object.assign(courseConfig, parsedCourseConfig);
    }

    let learnerStatusByUser = sessionStorage.getItem("learnerConfig");

    if (learnerStatusByUser) {
        const parsedLearnerConfig = JSON.parse(
            sessionStorage.getItem("learnerConfig"));
        Object.assign(learnerConfig, parsedLearnerConfig);
    }
}
function setStatus() {
    sessionStorage.setItem(
        "courseTopConfig", JSON.stringify(courseTopConfig));
    sessionStorage.setItem(
        "courseConfig", JSON.stringify(courseConfig));
    sessionStorage.setItem(
        "learnerConfig", JSON.stringify(learnerConfig));
}
function setFlag() {
    sessionStorage.setItem(
        'isEnteringDetail', 'true');
}

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
}
async function fetchAndDisplayLearners() {
    let learnersWithPaging = await apiGetRequestParams(
        "/api/learnermanagement/learners",
        {...baseConfig, ...learnerConfig});
    displayView(learnersWithPaging);
}
function displayView(learnersWithPaging) {
    updateStatusBar(learnersWithPaging);
    displayCardList(learnersWithPaging);
    displayPagination(learnersWithPaging, $("#learner-pagination"));
}
function updateStatusBar(learnersWithPagination) {
    let radioFilterStatus = ' (전체)';

    if (learnerConfig.radioFilter == 'onlyInProgress') {
        radioFilterStatus = ' (교육진행생만)';
    } else if (learnerConfig.radioFilter == 'onlyNotEnrolled') {
        radioFilterStatus = ' (미배정만)';
    } else if (learnerConfig.radioFilter == 'onlyCompleted') {
        radioFilterStatus = ' (수료생만)';
    } else if (learnerConfig.radioFilter == 'onlyDropped') {
        radioFilterStatus = ' (중퇴생만)';
    }
    if (learnerConfig.keyword == null || learnerConfig.keyword == "") {
        if (learnerConfig.coIsInProgress == null) {
            $("#course-name-date").text(`진행중 및 종료된 교육생${radioFilterStatus}`);
        } else if (learnerConfig.coIsInProgress == "1") {
            $("#course-name-date").text(`진행중인 과정의 교육생${radioFilterStatus}`);
        } else if (learnerConfig.coIsInProgress == "0") {
            $("#course-name-date").text(`종료된 과정의 교육생${radioFilterStatus}`);
        }
        if (learnerConfig.leCourseId != null) {
            $("#course-name-date").text(`선택한 과정의 교육생${radioFilterStatus}`);
        }
    } else {
        $("#course-name-date").text(`'${learnerConfig.keyword}' 검색결과`);
    }
    $("#course-number-of-learner")
    .text(`총 인원: ${learnersWithPagination.totalRecords}명`);

}
function displayCardList(learnersWithPaging) {
    $("#card-list").empty();

    let learners = learnersWithPaging?.records || [];
    if (!Array.isArray(learners)) {
        learners = [];
    }

    if (learners.length == 0) {
        $("#card-list").removeClass("row-cols-md-4");
        $("#card-list").html(
            "<p class='text-center w-100 pt-3'>교육생이 없습니다.</p>");
        return;
    } else {
        let baseImg = "https://joon-s3upload.s3.ap-northeast-2.amazonaws.com/upload/user/avatar.png";
        $("#card-list").addClass("row-cols-md-4");
        learners.forEach(function (learner) {
            let rowHtml = ``;
            if (learner.leId == null) {
                let user   = learner.learnerUser;
                rowHtml += `
                    <div class="col mb-4" style="min-height: 500px">
                        <div class="card text-center card-null"
                        style="border-top: 5px solid #f6c23e;">
                            <img src="${user.userProfileImg != null ? user.userProfileImg: baseImg}" 
                                 class="rounded-circle mt-3 mx-auto d-block" 
                                 style="width: 150px; height: 150px; object-fit: cover;">
                            <div class="card-body">
                                <h5 class="card-title mb-1">${user.userFullname}</h5>
                                <p class="card-text mb-1">${user.userMobile || "-"}</p>
                                <p class="card-text" style="min-height: 48px;">${user.userEmail}</p>
                                <p class="card-text" style="min-height: 48px;">해당 과정에 배정 요망</p>
                                <p class="card-text">${
                                    learner.leCompletionStatus == null ? "미배정" :
                                    learner.leCompletionStatus === "IN_PROGRESS" ? "교육생" :
                                    learner.leCompletionStatus === "COMPLETED" ? "수료생" :
                                    learner.leCompletionStatus === "DROPPED" ? "중퇴생" : "-"}</p>
                                <p class="card-text">-</p>
                                <a href="/courseManagement/learnerAssignment" class="btn btn-warning w-100" 
                                   aria-disabled="true">교육생배정 이동</a>
                            </div>
                        </div>
                    </div>
                `;
            } else {
                let user   = learner.learnerUser;
                let course = learner.learnerCourse;
                let part   = learner.partOverview;
                const encodedCoName = encodeURIComponent(course.coName);
                const colorStatus =
                          learner.leCompletionStatus === 'IN_PROGRESS' ? '#4e73df' :
                          learner.leCompletionStatus === 'COMPLETED' ? '#858796' :
                          learner.leCompletionStatus === 'DROPPED' ? '#e74a3b' : '';
                const buttonStatus =
                          learner.leCompletionStatus === 'IN_PROGRESS' ? 'btn-primary' :
                          learner.leCompletionStatus === 'COMPLETED' ? 'btn-secondary' :
                          learner.leCompletionStatus === 'DROPPED' ? 'btn-danger' : '';
                rowHtml += `
                    <div class="col mb-4"">
                        <div class="card text-center ${learner.leCompletionStatus === 'DROPPED' ? 'card-dropped' : ''}"  style="border-top: 5px solid ${colorStatus};">
                            <img src="${user.userProfileImg != null ? user.userProfileImg: baseImg}" 
                                 class="rounded-circle mt-3 mx-auto d-block" 
                                 style="width: 150px; height: 150px; object-fit: cover;">
                            <div class="card-body">
                                <h5 class="card-title mb-1">${user.userFullname}</h5>
                                <p class="card-text mb-1">${user.userMobile || "-"}</p>
                                <p class="card-text" style="min-height: 48px;">${user.userEmail}</p>
                                <p class="card-text" style="min-height: 48px;">${course.coName || "-"}</p>
                                <p class="card-text">${
                                    learner.leCompletionStatus == null ? "미배정" :
                                    learner.leCompletionStatus === "IN_PROGRESS" ? "교육생" :
                                    learner.leCompletionStatus === "COMPLETED" ? "수료생" :
                                    learner.leCompletionStatus === "DROPPED" ? "중퇴생" : "-"}</p>
                                <p class="card-text">출석률: ${part.attendanceRate}%</p>
                                <a href="learnerDetail?leId=${learner.leId}&coName=${encodedCoName}" 
                                   class="btn ${buttonStatus} w-100" onclick="setFlag();">상세보기</a>
                            </div>
                        </div>
                    </div>
                `;
            }
            $("#card-list").append(rowHtml);
        });
    }

}
function displayPagination(data, $selector) {

    // 기록이 없을 때, 페이지네이션도 표시되지 않음
    if (data.totalRecords == 0) {
        $selector.html("");
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

    $selector.html(output);
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

function handleLearnerFilterChange() {
    var selectedValue = $('input[name="learner-filter"]:checked').val();
    learnerConfig.radioFilter = selectedValue == '' ? null : selectedValue;
    learnerConfig.pageNo     = 1;
    learnerConfig.pageSize   = 8;
    setStatus();

    fetchAndDisplayLearners();
}
function handleCourseTopSelectorChange() {
    // 기존 검색과 페이징 초기화
    learnerConfig.keyword = null;
    $("#search-input").val("");
    learnerConfig.pageNo     = 1;
    learnerConfig.pageSize   = 8;
    learnerConfig.leCourseId = $(this).val();

    setStatus();
    fetchAndDisplayLearners();
}
function handleIsInProgressSelectChange() {
    // 기존 검색과 페이징 초기화
    learnerConfig.keyword = null;
    $("#search-input").val("");
    learnerConfig.pageNo     = 1;
    learnerConfig.pageSize   = 8;

    // is-in-progress 옵션 변경
    learnerConfig.coIsInProgress =
        $("#is-in-progress").val() === "" ? null : $("#is-in-progress").val();

    // course-select 옵션 로드 후 변경
    courseConfig.coIsInProgress =
        $("#is-in-progress").val() === "" ? null : $("#is-in-progress").val();
    fetchAndLoadCourseSelect();

    setStatus();
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
    setStatus();
    fetchAndDisplayLearners();
}
function handleSearchButtonClick() {

    // 페이징 초기화 및 검색한 키워드로 검색
    learnerConfig.pageNo   = 1;
    learnerConfig.pageSize = 8;
    learnerConfig.keyword  = $("#search-input").val();

    if (loginUserType == "ADMINISTRATOR") {
        // 관리자의 경우 전체에서 검색
        learnerConfig.coIsInProgress = null;
        $("#is-in-progress").val("");
        learnerConfig.leCourseId = null;
        $("#course-select").val("");
    } else if (loginUserType == "INSTRUCTOR") {
        // 강사의 경우 해당 과정에서 검색 유지
    }
    setStatus();
    fetchAndDisplayLearners();
}
function handlePageButtonClick() {
    learnerConfig.pageNo = $(this).data("page");

    setStatus();
    fetchAndDisplayLearners();
}

