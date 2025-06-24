let exitAttemptCount  = 0;
let examStarted       = false;
let testFinished      = false;
let focusLostNotified = false;

const iframe = document.getElementById("examFrame");

// SweetAlert2 Toast mixin
const Toast = Swal.mixin({
                             toast            : true,
                             position         : "bottom-end",
                             showConfirmButton: false,
                             timer            : 2000,
                             timerProgressBar : true,
                             didOpen          : (toast) => {
                                 toast.addEventListener("mouseenter",
                                                        Swal.stopTimer);
                                 toast.addEventListener("mouseleave",
                                                        Swal.resumeTimer);
                             }
                         });

// ① 시험 시작 버튼 클릭 → iframe src 설정 + 전체화면 진입
$(document).on("click", "#test-start-btn", function () {
    examStarted         = true;
    const testId        = $(this).data("test-id");
    const currentPageNo = $(this).data("current-page-no");
    const courseName    = $(this).data("course-name");

    iframe.src           = `/test/testSubmission?testId=${encodeURIComponent(
        testId)}&currentPageNo=${currentPageNo}&courseName=${encodeURIComponent(
        courseName)}`;
    iframe.style.display = "block";

    // Fullscreen API 호출
    const fs = iframe.requestFullscreen
               || iframe.webkitRequestFullscreen
               || iframe.msRequestFullscreen;
    fs.call(iframe);
});

// ② 전체화면 이탈 감지 → 1회차 경고, 2회차 시험 종료
document.addEventListener("fullscreenchange", () => {
    if (!examStarted || testFinished) {
        return;
    }

    if (!document.fullscreenElement) {
        exitAttemptCount++;
        if (exitAttemptCount === 1) {
            Swal.fire({
                          icon             : "warning",
                          title            : "전체화면이 해제되었습니다.",
                          text             : "시험은 전체화면 모드에서만 진행됩니다.",
                          confirmButtonText: "전체화면으로 돌아가기",
                          allowOutsideClick: false,
                      }).then(() => {
                // 다시 전체화면 복귀 유도
                const fs = iframe.requestFullscreen
                           || iframe.webkitRequestFullscreen
                           || iframe.msRequestFullscreen;
                fs.call(iframe);
            });
        } else {
            Swal.fire({
                          icon             : "error",
                          title            : "시험 종료",
                          text             : "전체화면 이탈이 반복되어 시험이 종료됩니다.",
                          confirmButtonText: "확인",
                          allowOutsideClick: false,
                      }).then(() => {
                testFinished         = true;
                // iframe 숨기고, 상세페이지로 리다이렉션
                iframe.style.display = "none";
                window.location.href = window.location.href; // 페이지 새로고침 혹은 원래
                                                             // 상세 URL로
            });
        }
    }
});

// ③ 탭·창 포커스 이탈 감지: 첫 경고만 toast, 이후 시험 종료 유사 처리
function handleFocusLoss() {
    if (!examStarted || testFinished) {
        return;
    }
    if (!focusLostNotified) {
        focusLostNotified = true;
        Toast.fire({
                       icon : "warning",
                       title: "포커스가 벗어났습니다!"
                   });
    } else {
        Swal.fire({
                      icon             : "error",
                      title            : "시험 종료",
                      text             : "포커스 이탈이 반복되어 시험이 종료됩니다.",
                      confirmButtonText: "확인",
                      allowOutsideClick: false,
                  }).then(() => {
            testFinished         = true;
            iframe.style.display = "none";
            window.location.href = window.location.href;

            apiCall("put", `/api/my/tests/${parseInt(
                UrlUtils.getQueryParam("testId"))}/submission/abnormal`);
        });
    }
}

document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "hidden") {
        handleFocusLoss();
    }
});
window.addEventListener("blur", () => {
    handleFocusLoss();
});
window.addEventListener("focus", () => {
    if (examStarted) {
        focusLostNotified = false;
    }
});