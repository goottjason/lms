const loginUserId   = $("#login-user-id").val();
const loginUserType = $("#login-user-type").val();

const queryStrings        = new URLSearchParams(window.location.search);

// 상단셀렉트박스에 사용됨
let baseConfig = {
    loginUserId: loginUserId,
    loginUserType: loginUserType
}
let courseConfig = {
    pageNo: null,
    pageSize: null,
    type: "coName", // Builder.Default (coName)
    keyword: null,
    orderBy: "coStartDate", // Builder.Default (coStartDate)
    orderDirection: "ASC", // Builder.Default (ASC)
    // 필터링
    coIsInProgress: null,
    coId: null
};

/* ================================================================================ */

$(document).ready(function () {

    // 강사, 교육생은 상단셀렉트박스 필요, 선택시 핸들러 필요
    if (loginUserType == "INSTRUCTOR" || loginUserType == "LEARNER") {
        fetchAndLoadTopCourseSelector();
        $(document).on('change', '#courseSelector', handleCourseSelectorChange);
    } else if (loginUserType == "ADMINISTRATOR") {
        $("#courseSelector").hide();
        $(document).on('click', '#modify-button', handleModifyButtonClick);
        $(document).on("click", "#remove-button", handleRemoveButtonClick);
    }
});

/* ================================================================================ */

async function fetchAndLoadTopCourseSelector() {
    let coursesWithPaging= await apiGetRequestParams(
        '/api/coursemanagement/courses',
        {...baseConfig, ...courseConfig});
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
    if (!Array.isArray(courses)) courses = [];
    if(courses.length == 0) {
        $("#courseSelector").append(`<option value="">해당하는 과정이 없습니다.`);
        return;
    }
    LoadTopCourseSelectorOption('#courseSelector', courses);
}
function LoadTopCourseSelectorOption(selector, records) {
    const $select = $(selector).empty();
    records.forEach(record => {
        const $option = $('<option>')
            .val(record.courseWithAssignedInfo.coId)
            .text(record.courseWithAssignedInfo.coName);
        $select.append($option);
        if (record.courseWithAssignedInfo.coId == queryStrings.get("courseId")) {
            $option.prop('selected', true);
        }
    });
}

async function removeCourse() {
    courseConfig.coId = queryStrings.get("courseId");
    await apiDeleteRequestParams(
        `/api/coursemanagement/courses`,
        {...baseConfig, ...courseConfig});
}

async function apiDeleteRequestParams(endpoint, params) {
    try {
        // 삭제 성공시
        const response = await axios.delete(endpoint, {params: params});
        // { "code": 200, "message": "삭제 성공", "data": null}
        await sweetAlertDeleteSuccess();
        window.location.href = `/courseManagement/courseList`;
    } catch (error) {
        // 삭제 실패시
        Swal.fire({
                      icon  : "error",
                      title : "삭제 실패",
                      text  : error.message,
                      footer: ""
                  });
        return [];
    }
}

async function sweetAlertDeleteSuccess() {
    await Swal.fire({
                      title: "삭제되었습니다.",
                      text : "해당 과정이 삭제되었습니다.",
                      icon : "success"
                  });
}

/* ================================================================================ */
function handleCourseSelectorChange() {
    let selectedCourseId = $(this).val();
    window.location.href = `/courseManagement/courseDetail?courseId=${selectedCourseId}`;
}
function handleModifyButtonClick(e) {
    e.preventDefault();
    let coStartDate = $(this).data('startdate');
    let coStartDateObj = toYMD(new Date(coStartDate));
    let todayObj = toYMD(new Date());
    if (coStartDateObj > todayObj) {
        window.location.href = $('#modify-link').attr('href');
    } else {
        Swal.fire({
                      icon: "error",
                      title: "수정불가능!",
                      text: "과정시작일 전일까지만 수정 가능합니다.",
                      footer: ''
                  });
    }
}
function handleRemoveButtonClick() {

    let coStartDate = $(this).data('startdate');
    let coStartDateObj = toYMD(new Date(coStartDate));
    let todayObj = toYMD(new Date());
    if (coStartDateObj > todayObj) {
        Swal.fire({
                      title             : "정말 삭제하시겠습니까?",
                      text              : "삭제하시면 되돌릴 수 없습니다.",
                      icon              : "warning",
                      showCancelButton  : true,
                      confirmButtonColor: "#3085d6",
                      cancelButtonColor : "#d33",
                      confirmButtonText : "삭제"
                  }).then((result) => {
            if (result.isConfirmed) {
                removeCourse();
            }
        });
    } else {
        Swal.fire({
                      icon: "error",
                      title: "삭제불가능!",
                      text: "과정시작일 전일까지만 삭제 가능합니다.",
                      footer: ''
                  });
    }



}

/* ================================================================================ */
function toYMD(dateObj) {
    return new Date(dateObj.getFullYear(), dateObj.getMonth(), dateObj.getDate());
}