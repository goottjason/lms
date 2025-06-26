const loginUserId   = $("#login-user-id").val();
const loginUserType = $("#login-user-type").val();
const params        = new URLSearchParams(window.location.search);
const courseId      = params.get("courseId");
const courseConfig = {
    pageNo: null,
    pageSize: null,
    type: null,
    keyword: null,
    orderBy: 'is_in_progress',
    orderDirection: 'DESC',
};
/* ================================================================================ */

$(document).ready(function () {
    if (loginUserType == "INSTRUCTOR" || loginUserType == "LEARNER") {
        updateTopCourseSelector();
    } else {
        $("#courseSelector").hide();
    }
    $(document).on('change', '#courseSelector', handleCourseSelectorChange)
    $(document).on("click", "#remove-button", handleRemoveBtnClick);
});

/* ================================================================================ */

function handleCourseSelectorChange() {
    console.log('Course Selector changed.');
    let selectedCourseId = $(this).val();
    console.log(selectedCourseId);
    window.location.href = `/courseManagement/courseDetail?courseId=${selectedCourseId}`;
}
async function updateTopCourseSelector() {
    let coursesWithPagination = await apiGetRequestAboutCourses(
        '/api/management/courses',
        {
            loginUserId: loginUserId,
            loginUserType: loginUserType,
            isInProgress: null});
    let courses = coursesWithPagination?.respDTOS || [];
    if (!Array.isArray(courses)) courses = [];
    console.log(courses)
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

async function removeCourse() {
    let data             = await apiDeleteRequest(
        "/api/course",
        {
            loginUserId  : loginUserId,
            loginUserType: loginUserType,
            courseId     : courseId
        });
    // window.location.href = "courseList";
}

async function apiDeleteRequest(endpoint, data = {}) {
    try {
        const response = await axios.delete(endpoint, {
            data: data,
            headers: {
                "Content-Type": "application/json"
            }
        });
        console.log(response);
        Swal.fire({
                      title: "삭제되었습니다.",
                      text : "해당 과정이 삭제되었습니다.",
                      icon : "success"
                  });
        return response.data;
    } catch (error) {
        console.log(error);
        Swal.fire({
                      icon  : "error",
                      title : "삭제 실패",
                      text  : "error.response.data.message",
                      footer: ""
                  });
        return [];
    }
}

/* ================================================================================ */

function handleRemoveBtnClick() {
    console.log("remove button clicked");
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
}