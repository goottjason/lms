const loginUserId   = $("#login-user-id").val();
const loginUserType = $("#login-user-type").val();
const params        = new URLSearchParams(window.location.search);
const courseId      = params.get("courseId");

/* ================================================================================ */

$(document).ready(function () {
    $(document).on("click", "#remove-button", handleRemoveBtnClick);
});

/* ================================================================================ */

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