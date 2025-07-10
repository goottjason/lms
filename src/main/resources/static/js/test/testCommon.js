/**
 * 공통 API 호출
 * @param {"get"|"post"|"put"|"delete"} method
 * @param {string} url
 * @param {object} [data]    // POST/PUT 바디
 * @param {object} [params]  // GET 쿼리스트링
 * @param {object} [headers] // 커스텀 헤더
 * @returns {Promise} axios Promise
 */
function apiCall(method, url, data = null, params = {}, headers = {}) {
  return axios({
    method,
    url,
    data,
    params,
    headers
  });
}

/**
 * URL 파라미터 유틸
 */
const UrlUtils = {
  /**
   * 쿼리스트링에서 값 꺼내기
   * @param {string} name 키 이름
   * @returns {string|null}
   */
  getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
  },

  /**
   * 경로 템플릿에 맞춰 세그먼트 파싱하기
   * @param {string} template e.g. '/test/testDetail/:testId/:foo'
   * @returns {Object} { testId: '8029', foo: '...' }
   */
  getPathParams(template) {
    const pathSegs = window.location.pathname.split("/").filter(Boolean);
    const templateSegs = template.split("/").filter(Boolean);
    const params = {};
    templateSegs.forEach((seg, i) => {
      if (seg.startsWith(":")) {
        const key = seg.slice(1);
        params[key] = pathSegs[i] || null;
      }
    });
    return params;
  },

  /**
   * 경로 세그먼트 인덱스로 가져오기
   * @param {number} index 0부터 시작 (첫 번째 유효 세그먼트)
   * @returns {string|null}
   */
  getPathSegment(index) {
    const segs = window.location.pathname.split("/").filter(Boolean);
    return segs[index] || null;
  }
};

//------------------------------------------------------------------------------

function renderTestHeader(testInfo) {

  const $testTitle = $("#test-title");
  const $startDate = $("#start-date");
  const $endDate = $("#end-date");
  const $testTime = $("#test-time");
  const $totalScore = $("#total-score");

  $testTitle.val(testInfo.title);
  $startDate.val(testInfo.startDate);
  $endDate.val(testInfo.endDate);
  $testTime.val(testInfo.testTime);
  $totalScore.val(testInfo.totalScore);

}

function buildQuiz(quiz, questionInfo) {

  $.each(questionInfo, function (index, item) {
    // console.log(item);
    const {
      questionNo,
      questionType,
      questionTitle,
      questionScore,
      options,
      questionAnswer,
    } = item;
    let question = new Question(questionNo, questionType, questionTitle,
        questionScore);
    // console.log(questionAnswer);
    question.setShortAnswer(questionAnswer);
    // console.log(question);

    while (question.options.length < options.length) {
      question.addOption();
    }

    // console.log(question.options);

    // 선택지 넣기
    $.each(question.options, function (index, item) {
      let optionData = options[index];

      item.setContent(optionData.optionContent);
      item.setCorrect(optionData.isCorrect);
    });

    // console.log(options);
    quiz.addQuestion(question);
    // console.log(question);
  });
  // console.log(quiz);
}

function buildUserAnswer(questions, score) {
  // console.log(questions);
  $(".user-score").text(score);

  $.each(questions, function (index, q) {

    const userAnswerObj = {
      userAnswer: q.userAnswer,
      userIsCorrect: q.userIsCorrect
    };

    userAnswer.push(userAnswerObj);
  });
}

const $testDetailBody = $(".test-detail-body");

function renderTestDetailPageForLearnerBySubmissionStatus(data) {
  // console.log(data);
  const submissionStatus = data.submissionStatus;
  $testDetailBody.empty();
  $testDetailBody.append(makeTestDetailPageBodyForLearner(submissionStatus));
  renderTestStartBtn(submissionStatus);

}

const $testStartBtn = $("#test-start-btn");

function renderTestStartBtn(submissionStatus) {
  if (submissionStatus === "IN_PROGRESS") {
    $testStartBtn.attr("data-restart-test", true).data("restart-test", true);
    $testStartBtn.find(".text").text("시험 재응시");
  } else if (submissionStatus === "COMPLETED") {
    $testStartBtn.hide();
  }
}

function handleValidationErrors(errorData) {
  const errorMsgObj = errorData.data;

  // 1) 모든 공통 필드 에러 메시지 초기화
  const fieldSelectors = {
    testTitle: ".test-title-err-msg",
    startDate: ".test-start-date-err-msg",
    endDate: ".test-end-date-err-msg",
    testTime: ".test-time-err-msg",
    totalScore: ".total-score-err-msg"
  };
  Object.values(fieldSelectors).forEach(sel => $(sel).text(""));

  // 2) 모든 문항 카드의 에러 메시지 초기화
  $(".question-card").each((_, card) => {
    const $c = $(card);
    $c.find(".question-title-err-msg").text("");
    $c.find(".question-score-err-msg").text("");
    $c.find(".question-options-err-msg").text("");
    $c.find(".short-answer-err-msg").text("");
  });

  // 3) 공통 필드 오류 뿌리기 (prefix 매칭)
  Object.entries(errorMsgObj).forEach(([key, msg]) => {
    for (const [fieldPrefix, selector] of Object.entries(fieldSelectors)) {
      if (key.startsWith(fieldPrefix)) {
        $(selector).text(msg);
        break;
      }
    }
  });

  // 4) 문항 관련 오류 파싱 (기존 로직)
  const questionErrMsgObjArr = [];
  Object.entries(errorMsgObj).forEach(([key, msg]) => {
    const match = key.match(
        /^questions\[(\d+)\](?:\.options\[(\d+)]\.(\w+)|\.(\w+))$/
    );
    if (!match) {
      return;
    }

    const qIdx = +match[1];
    if (!questionErrMsgObjArr[qIdx]) {
      questionErrMsgObjArr[qIdx] = { options: [] };
    }

    // 옵션 에러
    if (match[2] != null) {
      const optIdx = +match[2], field = match[3];
      questionErrMsgObjArr[qIdx].options[optIdx] = {
        ...questionErrMsgObjArr[qIdx].options[optIdx],
        [field]: msg
      };
    }
    // 문항 단위 에러
    else {
      const field = match[4];
      questionErrMsgObjArr[qIdx][field] = msg;
    }
  });

  // 5) 문항별 DOM에 에러 표시 (기존 로직)
  questionErrMsgObjArr.forEach((errObj, i) => {
    const $card = $(".question-card").eq(i);
    if (!$card.length) {
      return;
    }

    if (errObj.questionTitle) {
      $card.find(".question-title-err-msg").text(errObj.questionTitle);
    }
    if (errObj.questionScore) {
      $card.find(".question-score-err-msg").text(errObj.questionScore);
    }
    if (errObj.options.length || errObj.multipleAnswerValid) {
      $card.find(".question-options-err-msg")
           .text(errObj.multipleAnswerValid || "선택지를 빠짐없이 입력해 주세요.");
    }
    if (errObj.shortAnswerValid) {
      $card.find(".short-answer-err-msg").text(errObj.shortAnswerValid);
    }
  });
}

function formatDateTime(date) {

  // 값이 없거나 undefined거나 이상한 형식일 경우
  if (!date || date === "undefined:00" || date.includes("undefined")) {
    return ""; // 또는 null 로도 가능
  }

  let dateArr = date.split("T");

  // T가 없거나 시간 정보가 빠졌을 경우
  if (dateArr.length < 2 || !dateArr[1]) {
    return "";
  }

  return dateArr[0] + " " + dateArr[1] + ":00";
}

