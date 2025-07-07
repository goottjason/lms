//------------------------------------------------------------------------------
// [[전역 상태 변수]]
//------------------------------------------------------------------------------

let exitAttemptCount = 0; // 전체화면 이탈 횟수
let focusLostNotified = false; // 포커스 이탈 알림 여부
let testFinished = false; // 시험 종료 여부
let abnormalFinished = false; // 비정상 제출 여부
let focusLostCount = 0;   // 포커스 이탈 횟수

let testId;
let userId;
let currentPageNo;
let courseName;

let testInfo; // 시험 정보 객체
let quiz = new Quiz(); // 퀴즈 객체

const $questionListBox = $(".question-list-box");

//------------------------------------------------------------------------------
// [[전체화면 이탈 감지 및 부모에 메세지 전송]]
//------------------------------------------------------------------------------

window.addEventListener("message", e => {
  if (!e.data || e.data.type !== "PARENT_FULLSCREEN_CHANGE" || testFinished) {
    return;
  }

  if (!e.data.isFullscreen) {
    // 전체화면이 아닌 경우 (=> isFullscreen === true)

    exitAttemptCount++;
    if (exitAttemptCount === 1) {

      // 첫 번째 탈출을 부모에 알림
      window.parent.postMessage({ type: "CHILD_FIRST_EXIT" }, "*");
    } else {

      // 두 번째 탈출 → 비정상 종료
      collectAndSubmitAbnormal()
      .then((res) => {

        const abnormalMsg = res.data.data;

        if (abnormalMsg === "COUNT1") {

          // 첫 비정상 종료의 경우
          window.parent.postMessage({ type: "CHILD_ABORT" }, "*");
        } else if (abnormalMsg === "INVALIDATED") {

          // 두 번째 비정상 종료의 경우 => 시험 무효
          window.parent.postMessage({ type: "CHILD_INVALIDATED" }, "*");
        }
      });
    }
  } else {

    // 복귀 시 초기화
    focusLostNotified = false;
  }
});

window.addEventListener("blur", () => {
  if (testFinished) {
    return;
  }   // 시험이 끝났으면 무시

  focusLostCount++;

  if (focusLostCount === 1) {
    // ─── 1회차: 모달 + 메시지 ─────────────────────
    Swal.fire({
      icon: "warning",
      title: "포커스 이탈 감지",
      text: "시험 응시 중 창을 벗어났습니다. 계속 시험에 집중해주세요.",
      allowOutsideClick: false,
      confirmButtonText: "확인"
    });

  } else {

    collectAndSubmitAbnormal()
    .then((res) => {

      const abnormalMsg = res.data.data;

      if (abnormalMsg === "COUNT1") {

        // 첫 비정상 종료의 경우
        window.parent.postMessage({ type: "TEST_FOCUS_LOST" }, "*");
      } else if (abnormalMsg === "INVALIDATED") {

        // 두 번째 비정상 종료의 경우 => 시험 무효
        window.parent.postMessage({ type: "CHILD_INVALIDATED" }, "*");
      }
    });
  }
});

//------------------------------------------------------------------------------
// [[시험 페이지 초기화]]
//------------------------------------------------------------------------------

$(document).ready(async function () {

  lockTestPage();

  testId = UrlUtils.getQueryParam("testId");
  userId = UrlUtils.getQueryParam("userId");
  console.log(userId);
  currentPageNo = parseInt(UrlUtils.getQueryParam("currentPageNo"));
  courseName = UrlUtils.getQueryParam("courseName");

  // 시험 정보
  const quizRes = await apiCall("get", `/api/tests/${testId}`);
  console.log(quizRes);
  const data = quizRes.data.data;

  testInfo = new TestInfo(data.testTitle, data.startDate, data.endDate,
      data.testTime, data.totalScore);
  renderTestHeader(testInfo); // 시험 제목/기간/총점 등 렌더링

  buildQuiz(quiz, data.questions); // 퀴즈 객체 구성
  renderQuestions(quiz.questions); // 문제 출력

  if (UrlUtils.getQueryParam("restart")) {
    // 재응시의 경우

    // 이전 제출 불러오기
    const prevSubmissionRes = await apiCall("get", `/api/my/tests/${testId}`);

    renderPrevUserSubmission(prevSubmissionRes.data.data.questions); // 이전 제출한 답 렌더링
    setTestTimer(parseFloat(prevSubmissionRes.data.data.submissionTime)); // 남은 시간
    return;
  }

  setTestTimer(testInfo.testTime); // 새 시험의 경우 원래 제한 시간 셋팅
});

//------------------------------------------------------------------------------
// [[문제 렌더링 함수]]
//------------------------------------------------------------------------------

// 시험 기본 정보 렌더링
function renderTestHeader(testInfo) {

  const $testTitle = $("#test-title");
  const $startDate = $("#start-date");
  const $endDate = $("#end-date");
  const $totalScore = $("#total-score");

  $testTitle.val(testInfo.title);
  $startDate.val(testInfo.startDate);
  $endDate.val(testInfo.endDate);
  $totalScore.val(testInfo.totalScore);
}

// 시험 문항 렌더링
function renderQuestions(questions) {
  $questionListBox.empty();

  $.each(questions, function (index, q) {

    $questionListBox.append(makeQuestionCard(q));
  });
}

// 시험 문항 렌더링
function makeQuestionCard(q) {

  console.log(q);

  let questionCard = `
    <div class="card shadow mb-4 question-card">
      <div class="card-header py-2">
        <div class="row align-items-center">
          <div class="col-1">
            <h6 class="m-0 font-weight-bold text-primary">문항
              <span class="question-no" data-question-no="${q.questionNo}">${q.questionNo}</span></h6>
          </div>
          <div class="col-1">
            ${q.type === "MULTIPLE" ? "객관식" : "주관식"}
          </div>
          <div class="col ml-aut text-info">
            배점 : <span>${q.score}</span>점
          </div>
        </div>
      </div>
      <div class="card-body">
        <div class="text-primary font-weight-bold mb-4 pt-3">
          ${q.title}
        </div>
    `;

  if (q.type === "MULTIPLE") {

    questionCard += `
        <div class="mb-3 question-answer-section">
          <div class="row align-items-center">
            <div class="col-10">
              <label class="form-label">객관식 보기 및 정답 선택</label>
              <span><small
                  class="text-muted"> (정답에 해당하는 보기 오른쪽 원형 버튼을 선택하세요.)</small></span>
            </div>
            <div class="col-1 text-center">
              정답
            </div>
          </div>
          <div class="option-list">
        `;

    $.each(q.options, function (index, o) {

      questionCard += `
            <div class="row align-items-center mb-2 option-row">
              <div class="col-10">
                <input
                    class="form-control question-option"
                    data-option-no="${o.optionNo}"                    
                    readonly
                    type="text"
                    value="${o.content}"
                />
              </div>
              <div class="col-1 text-center">
                <input class="question-answer" name="question-${q.questionNo}" type="radio"/>
              </div>
            </div>
            `;
    });

    questionCard += `</div>\n</div>\n</div>\n</div>`;

  } else {

    questionCard += `
        <div class="mb-3 question-answer-section">
              <label class="form-label">정답</label>
              <input
                  class="form-control short-question-answer"
                  placeholder="정답은 하나의 단어 또는 단답형 문장으로만 입력하세요."
                  type="text"
              />
            </div>
          </div>
        </div>
        `;

  }

  return questionCard;
}

// 이전 제출한 답 렌더링
function renderPrevUserSubmission(questions) {

  const $questionCards = $(".question-card");

  $.each($questionCards, function (index, qCard) {

    const userAnswer = questions[index].userAnswer;
    const questionType = questions[index].questionType;

    if (questionType === "MULTIPLE") {

      $(qCard).find(`.question-option[data-option-no="${userAnswer}"]`)
              .closest(".option-row").find(".question-answer").prop("checked",
          true);
    } else {

      $(qCard).find(".short-question-answer").val(userAnswer);
    }
  });
}

//------------------------------------------------------------------------------
// [[타이머 관리 및 시간 초과 처리]]
//------------------------------------------------------------------------------

// 시험 타이머 셋팅
function setTestTimer(testTime) {
  const $testTime = $("#test-time");

  let remainingSeconds = testTime * 60;

  function updateDisplay() {
    const m = Math.floor(remainingSeconds / 60);
    const s = Math.floor(remainingSeconds % 60);

    const mm = String(m).padStart(2, "0");
    const ss = String(s).padStart(2, "0");
    $testTime.val(`${mm}:${ss}`);
  }

  updateDisplay();

  const intervalId = setInterval(() => {
    remainingSeconds--;

    if (remainingSeconds < 0) {
      clearInterval(intervalId);
      onTimeUp();
    } else {
      updateDisplay();
    }

  }, 1000);
}

//------------------------------------------------------------------------------
// [[답안 제출]]
//------------------------------------------------------------------------------

// 타이머 종료시 자동 제출
function onTimeUp() {

  Swal.fire({
    title: "제한 시간 종료",
    text: "시험 시간이 모두 소진되었습니다. 자동으로 제출됩니다.",
    icon: "info"
  }).then(() => {

    const answerObj = buildUserAnswer();

    submitAnswers(answerObj);
    window.parent.postMessage({ type: "TEST_FINISHED" }, "*");
  });
}

// 제출 버튼 클릭 시 답안 수집 → 빈 문항 확인 → 제출 호출
$(document).on("click", "#test-submit-btn", function () {

  const answerObj = buildUserAnswer();

  testFinished = true;

  const hasEmpty = answerObj.selectAnswers.some(ans => ans === "");
  if (hasEmpty) {
    Swal.fire({
      title: "빈 문항이 있습니다",
      text: "답이 비어있는 문항이 있습니다. 그래도 제출하시겠습니까?",
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "네, 제출할게요",
      cancelButtonText: "아니요, 다시 확인할게요"
    }).then(result => {
      if (result.isConfirmed) {
        submitAnswers(answerObj);
      }
    });
  } else {
    submitAnswers(answerObj);
  }
});

function buildUserAnswer() {
  // 현재 화면에서 사용자 답안 수집
  const [m, s] = $("#test-time").val().split(":").map(Number);
  const submissionTime = m * 60 + s;

  const userTestAnswer = {
    testId: parseInt(UrlUtils.getQueryParam("testId")),
    submissionTime,
    questionNums: [],
    selectAnswers: []
  };

  $(".question-card").each((_, qCard) => {
    const $card = $(qCard);
    const qNo = $card.find(".question-no").data("question-no");
    userTestAnswer.questionNums.push(qNo);

    let answer = "";
    if ($card.find(".option-list").length) {
      $card.find(".option-row").each((_, oRow) => {
        if ($(oRow).find(".question-answer").is(":checked")) {
          answer = String($(oRow).find(".question-option").data("option-no"));
          return false;
        }
      });
    } else {
      answer = $card.find(".short-question-answer").val().trim() || "";
    }
    userTestAnswer.selectAnswers.push(answer);
  });

  return userTestAnswer;
}

//------------------------------------------------------------------------------
// [[제출 후 처리 및 비정상 종료 처리]]
//------------------------------------------------------------------------------

function submitAnswers(userTestAnswer) {
  $("#test-submit-btn").prop("disabled", true);
  const testId = userTestAnswer.testId;
  console.log(testId);

  testFinished = true;
  focusLostCount = 0;

  apiCall("put", `/api/my/tests/${testId}/submission`,
      userTestAnswer, {}, { "Content-Type": "application/json" })
  .then(() => apiCall("get", `/api/my/tests/${testId}`))
  .then(res => {
    console.log(res);
    const userScore = res.data.data.userScore;
    return finishTestWithScore(userScore, testId);
  })
  .catch(err => {
    console.error(err);
    Swal.fire("오류", "제출 중 오류가 발생했습니다. 다시 시도해주세요.", "error");
    $("#test-submit-btn").prop("disabled", false);
  });
}

function collectAndSubmitAbnormal() {
  if (abnormalFinished) {
    return;
  }
  abnormalFinished = true;

  const abnormalAnswerObj = buildUserAnswer();

  return apiCall(
      "put",
      `/api/my/tests/${testId}/submission/abnormal`,
      abnormalAnswerObj,
      {},
      { "Content-Type": "application/json" }
  );
}

// 정상적인 시험 제출 후 응시자의 시험 점수 보여주기
function finishTestWithScore(userScore, testId) {
  testFinished = true;

  Swal.fire({
    title: "시험 완료!",
    html: `<p>당신의 점수는 <strong>${userScore}점</strong> 입니다.</p>`,
    icon: "success",
    confirmButtonText: "확인",
    allowOutsideClick: false
  })
      .then(() => {

        console.log(window.parent.document);

        // 부모 문서에서 fullscreenElement 확인 후 전체화면 해제
        const parentDoc = window.parent.document;
        const fsEl = parentDoc.fullscreenElement
            || parentDoc.webkitFullscreenElement
            || parentDoc.msFullscreenElement;

        if (fsEl) {
          // 표준 API
          if (parentDoc.exitFullscreen) {
            return parentDoc.exitFullscreen();
          }
          // WebKit
          if (parentDoc.webkitExitFullscreen) {
            return parentDoc.webkitExitFullscreen();
          }
          // IE11
          if (parentDoc.msExitFullscreen) {
            return parentDoc.msExitFullscreen();
          }
        }

        return Promise.resolve();
      })
      .then(() => {

        window.parent.postMessage({ type: "TEST_FINISHED" }, "*");

        // 해제 완료 후 iframe 숨기고 리다이렉트
        // $("#examFrame", window.parent.document).hide().attr("src", "");
        // window.parent.location.href = `/test/learner/testDetail/${testId}?userId=${userId}&currentPageNo=${currentPageNo}&courseName=${courseName}`;
      });
}

function lockTestPage() {
  // 히스토리 강제 고정
  history.pushState(null, null, location.href);

  // 사용자가 현재 페이지를 떠나기 직전 발생하는 브라우저 이벤트
  window.addEventListener("beforeunload", (e) => {
    if (!testFinished) {
      // 브라우저 자체 경고창
      e.preventDefault();
      e.returnValue = "";

      collectAndSubmitAbnormal();
    }
  });

  // 브라우저 히스토리 변경 시 발생하는 이벤트
  window.addEventListener("popstate", (e) => {
    if (!testFinished) {
      history.pushState(null, null, location.href);
      Swal.fire({
        icon: "warning",
        title: "시험 중 뒤로가기는 불가능합니다.",
        allowOutsideClick: false
      });
    }
  });

  document.addEventListener("keydown", (e) => {
    if (testFinished) {
      return;
    }
    if (
        e.key === "F5" ||
        (e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "r" ||
        (e.altKey && ["ArrowLeft", "ArrowRight"].includes(e.key))
    ) {
      e.preventDefault();
      Swal.fire("금지된 키 조작", "시험 중에는 키보드 조작이 제한됩니다.", "error");
    }
  });

  document.addEventListener("contextmenu", (e) => {
    if (!testFinished) {
      e.preventDefault();
    }
  });
}

