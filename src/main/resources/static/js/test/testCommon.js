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

function handleValidationErrors(errorData) {
  const errorMsgObj = errorData.data;
  console.log(errorMsgObj);

  // 공통 필드
  const fieldSelectors = {
    testTitle: ".test-title-err-msg",
    startDate: ".test-start-date-err-msg",
    endDate: ".test-end-date-err-msg",
    testTime: ".test-time-err-msg",
    totalScore: ".total-score-err-msg"
  };

  // 공통 필드 메시지 초기화
  for (const selector of Object.values(fieldSelectors)) {
    $(selector).text("");
  }

  // 공통 필드 오류 출력 (prefix 매칭)
  Object.entries(errorMsgObj).forEach(([key, msg]) => {
    for (const [fieldPrefix, selector] of Object.entries(fieldSelectors)) {
      if (key.startsWith(fieldPrefix)) {
        $(selector).text(msg);
        break; // 첫 매칭 후 중단
      }
    }
  });

  // 문항 관련 오류 처리
  const questionErrMsgObjArr = [];

  Object.entries(errorMsgObj).forEach(([key, msg]) => {
    const match = key.match(
        /^questions\[(\d+)\](?:\.options\[(\d+)]\.(\w+)|\.(\w+))$/);
    if (!match) {
      return;
    }

    const questionIndex = Number(match[1]);
    if (!questionErrMsgObjArr[questionIndex]) {
      questionErrMsgObjArr[questionIndex] = { options: [] };
    }

    if (match[2] !== undefined) {
      const optionIndex = Number(match[2]);
      const optionField = match[3];
      if (!questionErrMsgObjArr[questionIndex].options[optionIndex]) {
        questionErrMsgObjArr[questionIndex].options[optionIndex] = {};
      }
      questionErrMsgObjArr[questionIndex].options[optionIndex][optionField] = msg;
    } else {
      const field = match[4];
      questionErrMsgObjArr[questionIndex][field] = msg;
    }
  });

  // 문항별 DOM 에러 표시
  questionErrMsgObjArr.forEach((questionErrMsgObj, i) => {
    const $questionCard = $(".question-card").eq(i);
    if (!$questionCard.length) {
      return;
    }

    // 문항 메시지들 초기화
    $questionCard.find(".question-title-err-msg").text("");
    $questionCard.find(".question-score-err-msg").text("");
    $questionCard.find(".question-options-err-msg").text("");
    $questionCard.find(".short-answer-err-msg").text("");

    if (questionErrMsgObj.questionTitle) {
      $questionCard.find(".question-title-err-msg").text(
          questionErrMsgObj.questionTitle);
    }

    if (questionErrMsgObj.questionScore) {
      $questionCard.find(".question-score-err-msg").text(
          questionErrMsgObj.questionScore);
    }

    console.log(questionErrMsgObj);
    if (questionErrMsgObj.options.length > 0 ||
        questionErrMsgObj.multipleAnswerValid) {

      if (questionErrMsgObj.multipleAnswerValid) {
        $questionCard.find(".question-options-err-msg")
                     .text(questionErrMsgObj.multipleAnswerValid);

      } else {
        $questionCard.find(".question-options-err-msg").text(
            "선택지를 빠짐없이 입력해 주세요.");
      }
    }

    if ((questionErrMsgObj.options.length === 0 &&
            questionErrMsgObj.multipleAnswerValid)
        || questionErrMsgObj.shortAnswerValid) {
      $questionCard.find(".short-answer-err-msg").text(
          questionErrMsgObj.shortAnswerValid || "");
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