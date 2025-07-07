//------------------------------------------------------------------------------
// [[전역 상태 변수]]
//------------------------------------------------------------------------------

let examStarted = false; // 시험 시작했는지 여부
let testFinished = false; // 시험 종료되었는지 여부

// 시험 화면 보여줄 iframe 요소
const iframe = document.getElementById("examFrame");

//------------------------------------------------------------------------------
// [[전체화면 상태 변경 감지 이벤트]]
//------------------------------------------------------------------------------

// 전체화면 변경 (표준)
document.addEventListener("fullscreenchange", () => {

  const isFs = !!(
      document.fullscreenElement ||
      document.webkitFullscreenElement ||
      document.msFullscreenElement
  );

  const msg = {
    type: "PARENT_FULLSCREEN_CHANGE",
    isFullscreen: isFs
  };

  // 자식(iframe)에 전체화면 전달
  iframe.contentWindow.postMessage(msg, "*");
});

// 사파리용 별도 이벤트
document.addEventListener("webkitfullscreenchange", () => {
  const isFs = !!document.webkitFullscreenElement;
  iframe.contentWindow.postMessage({
    type: "PARENT_FULLSCREEN_CHANGE",
    isFullscreen: isFs
  }, "*");
});

//------------------------------------------------------------------------------
// [[시험 시작 버튼 클릭 이벤트]]
//------------------------------------------------------------------------------

// 상세 페이지에서는 iframe 숨김
iframe.style.display = "none";

$(document).on("click", "#test-start-btn", function (e) {
  examStarted = true; // 시험 시작

  // 재응시 여부
  const isRestart = $("#test-start-btn").data("restart-test") === true;
  const userId = UrlUtils.getQueryParam("userId");
  const testId = $(this).data("test-id");
  const currentPageNo = $(this).data("current-page-no");
  const courseName = $(this).data("course-name");

  // iframe이 로드할 url
  let src = `/test/testSubmission`
      + `?userId=${userId}&testId=${encodeURIComponent(testId)}`
      + `&currentPageNo=${currentPageNo}`
      + `&courseName=${encodeURIComponent(courseName)}`;

  // 재응시면 추가 쿼리스트링
  if (isRestart) {
    src += `&restart=true`;
  }

  // iframe에 url 설정 후 응시 창 보여주기
  iframe.src = src;
  iframe.style.display = "block";

  // 전체화면 진입
  const fs = iframe.requestFullscreen
      || iframe.webkitRequestFullscreen
      || iframe.msRequestFullscreen;
  fs.call(iframe);
});

// ──────────────────────────────────────────────────────────────────────────
// [[자식(iframe) → 부모 메시지 수신 & 처리]]
// ──────────────────────────────────────────────────────────────────────────
function preventEsc(e) {
  if (e.key === "Escape") {
    e.preventDefault();
    e.stopPropagation();
  }
}

window.addEventListener("message", (e) => {

  console.log(e.data.type);

  // 유효하지 않은 메시지 또는 시험 시작 전/종류 후의 경우 무시
  if (!e.data || !examStarted || testFinished) {
    return;
  }

  switch (e.data.type) {

      // 포커스 이탈로 인한 강제 종료
    case "TEST_FOCUS_LOST":
      if (document.fullscreenElement) {
        (document.exitFullscreen ||
            document.webkitExitFullscreen ||
            document.msExitFullscreen).call(document);
      }

      iframe.style.display = "none";
      testFinished = true;

      Swal.fire({
        icon: "error",
        title: "시험 종료",
        html: "포커스 이탈이 반복되어<br>시험이 종료됩니다.",
        allowOutsideClick: false
      }).then(() => {
        // 비정상 종료 API 호출이 필요하면 여기에서
        apiCall("get", "/user/logout")
        .finally(() => {
          window.location.href = "/";
        });
      });
      break;

      // 첫 번째 전체화면 탈출 경고
    case "CHILD_FIRST_EXIT":
      iframe.style.display = "none";

      Swal.fire({
        icon: "warning",
        title: "전체화면이 해제되었습니다.",
        text: "시험은 전체화면 모드에서만 진행됩니다.",
        confirmButtonText: "다시 전체화면으로",
        allowOutsideClick: false,
        allowEscapeKey: false,
        allowEnterKey: false,
        backdrop: true,
        didOpen: () => {
          document.addEventListener("keydown", preventEsc, true);
        },
        willClose: () => {
          document.removeEventListener("keydown", preventEsc, true);
        }
      }).then(() => {

        iframe.style.display = "block";

        const el = iframe;
        (el.requestFullscreen ||
            el.webkitRequestFullscreen ||
            el.msRequestFullscreen).call(el);
      });
      break;

      // 두 번째 탈출 → 비정상 종료
    case "CHILD_ABORT":

      iframe.style.display = "none";
      testFinished = true;
      Swal.fire({
        icon: "error",
        title: "시험 종료",
        text: "전체화면 이탈이 반복되어 시험이 종료됩니다.",
        allowOutsideClick: false
      }).then(() => {
        iframe.style.display = "none";
        iframe.src = "";
        // 필요 시 비정상 종료 API 호출 후
        window.location.reload();

        apiCall("get", "/user/logout");
        window.location.href = "/";
      });
      break;

      // 시험 무효 처리
    case "CHILD_INVALIDATED":
      iframe.style.display = "none";
      testFinished = true;

      Swal.fire({
        icon: "error",
        confirmButtonText: "확인",
        title: "시험이 무효 처리되었습니다",
        html: [
          "부정행위 또는 전체화면 이탈이 반복되어",
          "본 시험이 무효 처리되었습니다.",
          "<br>",
          "응시 기록만 저장되었으며,",
          "<strong>재응시는 불가능</strong>합니다."
        ].join("<br>"),
        allowOutsideClick: false
      }).then(() => {
        iframe.style.display = "none";
        iframe.src = "";
        // 필요 시 비정상 종료 API 호출 후

        iframe.src = "";
        window.location.reload();
      });
      break;

      // 정상 제출 완료 신호
    case "TEST_FINISHED":
      testFinished = true;
      // 전체화면 해제
      if (document.fullscreenElement) {
        (document.exitFullscreen ||
            document.webkitExitFullscreen ||
            document.msExitFullscreen).call(document);
      }
      Swal.fire({
        icon: "success",
        title: "시험 완료!",
        text: "제출이 정상 처리되었습니다.",
        allowOutsideClick: false
      }).then(() => {
        iframe.style.display = "none";
        iframe.src = "";
        window.location.reload();
      });
      break;
  }
});