let originalTestInfo = null;
let newTestInfo = null;

let originalQuiz = new Quiz();
let copyQuiz = null;
let newQuiz = null;

let userQuiz = new Quiz();
let userAnswer = [];

const $testTitle = $("#test-title");
const $startDate = $("#start-date");
const $endDate = $("#end-date");
const $testTime = $("#test-time");
const $totalScore = $("#total-score");

const $questionListBox = $(".question-list-box");
const $modifyTestBtn = $("#modify-test-btn");
let $learnerTableBody = $("#learner-table-body");

$(document).ready(function () {

  if (UrlUtils.getQueryParam("modified") === "true") {
    Swal.fire({
      position: "center",
      icon: "success",
      title: "수정 완료",
      showConfirmButton: false,
      timer: 1500
    });
  }

  const pageNo = UrlUtils.getQueryParam("currentPageNo");

  $("#prev-page").data("page-no", pageNo);

  console.log(UrlUtils.getQueryParam("courseName"));

  axios.get(`/api/courses`)
       .then(function (response) {

         renderCourseFilterOptionsForAdminForUser(response.data.data);

         const param = UrlUtils.getQueryParam("courseName");
         if (param) {
           $("#courseSelector").val(param);
         }

         $("#courseSelector").prop("disabled", true);

         renderTestListPage(param, UrlUtils.getQueryParam("currentPageNo"));
       })
       .catch(console.error);

  const parts = window.location.pathname.split("/");
  const testId = parseInt(UrlUtils.getPathSegment(2));
  console.log(testId);

  apiCall("get", `/api/tests/${testId}`)
  .then((res) => {

    console.log(res);
    const data = res.data.data;
    originalTestInfo = new TestInfo(data.testTitle, data.startDate,
        data.endDate, data.testTime,
        data.totalScore);

    // console.log(testInfo);

    renderTestHeader(originalTestInfo);
    buildQuiz(originalQuiz, data.questions);
    renderQuestions(originalQuiz.questions);
    hideAllBtn(true);
    renderTestModifyBtn(data.startDate, res.data.message);
  })
  .catch(error => console.log(error));

  apiCall("get", `/api/tests/${testId}/learners`)
  .then((res) => {
    console.log(res);
    renderLearnerList(res.data.data);
    renderTestDelBtn(res.data.data);
  })
  .catch(error => console.log(error));

});

function makeQuestionCard(question) {
  console.log(question);
  let questionCard = `
    <div class="card mb-4 question-card" data-question-no="${question.questionNo}">
      <div class="card-header py-2">
        <div class="row align-items-center">
          <div class="col-1">
            <h6 class="m-0 font-weight-bold text-primary">문항
              <span class="question-no" data-question-no="${question.questionNo}">${question.questionNo}</span></h6>
          </div>
          <div class="col-5">
            <div class="form-check form-check-inline">
              <input
                  class="form-check-input question-type"
                  type="radio"
                  name="question-type-${question.questionNo}"
                  value="MULTIPLE"                  
                  ${question.type === "MULTIPLE" ? "checked" : ""}
                  disabled
              />
              <label class="form-check-label">객관식</label>
            </div>
            <div class="form-check form-check-inline">
              <input
                  class="form-check-input question-type"
                  type="radio"
                  name="question-type-${question.questionNo}"
                  value="SHORT"
                  ${question.type === "SHORT" ? "checked" : ""}
                  disabled
              />
              <label class="form-check-label">주관식</label>
            </div>
          </div>
          <div class="col-auto ml-auto">
            <button
                id="del-question-btn" type="button"
                class="btn btn-sm btn-danger btn-icon-split del-question-btn">
              <span class="icon text-white-50">
                <i class="fas fa-times"></i>
              </span>
              <span class="text">문항 삭제</span>
            </button>
           </div>

        </div>
      </div>
      <div class="card-body">
        <div class="row mb-3">
          <div class="col-md-10">
            <label class="form-label">문항 설명</label>
            <span class="question-title-err-msg"></span>
            <input
                type="text"
                class="form-control question-title"
                value="${question.title}"
                placeholder="예: 자바의 다형성에 대해 설명하시오"
                disabled
            />
          </div>
          <div class="col-md-2">
            <label class="form-label">배점</label>
            <span class="question-score-err-msg"></span>
            <input
                type="number"
                class="form-control question-score"
                value="${question.score}"
                placeholder="예: 10"
                min="1"
                disabled
            />
          </div>
        </div>
    `;

  let questionAnswerSection = makeQuestionAnswerSection(question);

  return questionCard + questionAnswerSection;
}

function makeQuestionAnswerSection(question) {

  let questionAnswerSection = "";
  if (question.type === "MULTIPLE") {

    let optionRow = "";
    $.each(question.options, function (index, item) {

      optionRow += `            
        <div class="row align-items-center mb-2 option-row">
          <div class="col-10">
            <input
                type="text"
                class="form-control question-option"
                data-option-no="${item.optionNo}"
                placeholder="보기 ${item.optionNo}"
                value="${item.content}"
                disabled
            />
          </div>
          <div class="col-1 text-center">
            <input class="question-answer" type="radio" name="question-${question.questionNo}"
             ${item.isCorrect ? "checked" : ""} disabled/>
          </div>
          <div class="col-1">
            <button
                type="button"
                class="btn btn-danger btn-icon-split btn-sm del-option-btn">
              <span class="text" ${question.options.length
      === 2 ? "disabled" : ""}>삭제</span>
            </button>
          </div>
        </div>
        `;
    });

    let optionList = `        
          <div class="option-list">
            ${optionRow}        
          </div>
          `;

    questionAnswerSection = `
        <div class="mb-3 question-answer-section">
          <label class="form-label">객관식 보기 및 정답 선택</label>
          <span><small class="text-muted"> (정답에 해당하는 보기 오른쪽 원형 버튼을 선택하세요.)</small></span>
          <span class="question-options-err-msg"></span>

          ${optionList}

          <div class="row align-items-center mb-2">
            <div class="col-10">
              <button
                  type="button" id="add-option-btn"
                  class="btn btn-sm btn-secondary btn-icon-split add-option-btn"
                  data-question-no="${question.questionNo}">
                <span class="icon text-white-50">
                  <i class="fas fa-plus"></i>
                </span>
                <span class="text" ${question.options.length === 4 ? "disable"
        : ""}>보기 추가</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    `;

  } else if (question.type === "SHORT") {

    questionAnswerSection = `
        <div class="mb-3 question-answer-section">
          <label class="form-label">정답</label>
          <span><small class="text-muted"> (정답은 하나의 단어 또는 단답형 문장으로만 입력하세요.)</small></span>
          <span class="short-answer-err-msg"></span>
            <input
                type="text"
                class="form-control short-answer"
                value="${question.answer}"
                placeholder="예: 어쩌구저쩌구"
                disabled
            />
        </div>
        `;

  }
  return questionAnswerSection;
}

function renderQuestions(questions) {
  $questionListBox.empty();

  $.each(questions, function (index, item) {
    // console.log(item);

    $questionListBox.append(makeQuestionCard(item));
  });
}

function hideAllBtn(isDisabled) {

  if (isDisabled) {
    $(".del-question-btn").hide();
    $(".del-option-btn").hide();
    $(".add-option-btn").hide();
    $("#add-question-btn").hide();
  } else {
    $(".del-question-btn").show();
    $(".del-option-btn").show();
    $(".add-option-btn").show();
    $("#add-question-btn").show();
  }

}

function renderTestModifyBtn(startDate, userType) {

  if (userType === "ADMINISTRATOR") {
    $modifyTestBtn.remove();
    $("#del-test-btn").hide();
  }

  const now = new Date();
  const start = new Date(startDate);

  if (now > start) {
    $modifyTestBtn.remove();
  }

}

function renderLearnerList(learnerArray) {
  console.log(learnerArray);
  $learnerTableBody.empty();

  $.each(learnerArray, function (index, item) {
    console.log(item);

    const learnerInfo = {
      learnerId: item.learnerId,
      fullName: item.fullName,
      loginId: maskId(item.loginId),
      submissionStatus: assignValueBySubmissionStatus(
          item.submissionStatus),
      submissionRegDate: item.submissionRegDate ? localDateTimeFormatter(
              item.submissionRegDate)
          : "-",
      score: getStudentScoreDisplay(item.isInvalidated,
          item.submissionStatus,
          item.score)
    };
    console.log(learnerInfo);

    $learnerTableBody.append(makeLearnerListTr(learnerInfo));

  });
}

function localDateTimeFormatter(date) {

  const dateArr = date.split("T");

  return dateArr[0] + " " + dateArr[1];
}

function getStudentScoreDisplay(isInvalidated, submissionStatus, score) {
  console.log(isInvalidated, submissionStatus, score);

  if (isInvalidated) {
    return "시험 무효";
  }

  switch (submissionStatus) {
    case "COMPLETED":
      return `${score}점`;
    default:
      return "-";
  }

}

function maskId(id) {
  if (4 >= id.length) {
    return id;
  }

  const visiblePart = id.slice(0, 4);
  const maskedPart = "*".repeat(id.length - 4);

  return visiblePart + maskedPart;
}

function assignValueBySubmissionStatus(status) {
  const statusValue = {
    "NOT_STARTED": "미응시",
    "IN_PROGRESS": "응시중",
    "COMPLETED": "응시 완료"
  };

  return statusValue[status];
}

function makeLearnerListTr(data) {
  console.log(data);

  return `
    <tr class="learner-row">
      <td class="text-center align-middle fullname">${data.fullName}</td>
      <td class="text-center align-middle">${data.loginId}</td>
      <td class="text-center align-middle">${data.submissionStatus}</td>
      <td class="text-center align-middle">${data.submissionRegDate}</td>
      <td class="text-center align-middle">${data.score}</td>
      <td class="text-center align-middle">
        <button
                type="button"
            class="btn btn-sm btn-info btn-icon-split show-learner-answer-btn"
            data-learner-id="${data.learnerId}"
            data-target="#answerModal"
            data-toggle="modal"
            id=""
        ${data.score.includes("점") ? "" : "disabled"}
            >
          <span class="icon text-white-50">
            <i class="fas fa-folder-open"></i>
          </span>
          <span class="text">답안보기</span>
        </button>
      </td>
    </tr>
    `;
}

function renderTestDelBtn(data) {
  console.log(data);
  $.each(data, function (index, item) {
    if (item.submissionStatus === "COMPLETED") {
      $("#del-test-btn").hide();
      return;
    }
  });
}

function checkAllQuestionBtn(quiz) {
  if (quiz.questions.length === 15) {
    // 문항 추가 disabled
    $("#add-question-btn").prop("disabled", true);
  } else if (quiz.questions.length === 1) {
    // 문항 삭제 disabled
    $(".del-question-btn").prop("disabled", true);
  } else {
    // 문항 추가 | 문항 삭제 able
    $("#add-question-btn").prop("disabled", false);
    $(".del-question-btn").prop("disabled", false);
  }
  let $questionCard = $(".question-card");
  console.log($questionCard);
  $.each(quiz.questions, function (index, q) {
    if (q.options.length === 4) {
      // 보기 추가 disabled
      $questionCard.eq(q.questionNo - 1).find(".add-option-btn").prop(
          "disabled", true);
    } else if (q.options.length === 2) {
      // 보기 삭제 disabled
      $.each($questionCard.eq(q.questionNo - 1).find(".del-option-btn"),
          function (index, item) {
            $(item).prop("disabled", true);
          });
    } else {
      // 보기 추가 | 삭제 able
      $questionCard.eq(q.questionNo - 1).find(".add-option-btn").prop(
          "disabled", false);
      $.each($questionCard.eq(q.questionNo - 1).find(".del-option-btn"),
          function (index, item) {
            $(item).prop("disabled", false);
          });
    }
  });

}

function setTestInfoFieldsDisabled(isDisabled) {

  $testTitle.prop("disabled", isDisabled);
  $startDate.prop("disabled", isDisabled);
  $endDate.prop("disabled", isDisabled);
  $testTime.prop("disabled", isDisabled);
  // $totalScore.prop("disabled", isDisabled);
}

function setQuestionFieldsDisabled(isDisabled) {

  $(".question-type").prop("disabled", isDisabled);
  $(".question-title").prop("disabled", isDisabled);
  $(".question-score").prop("disabled", isDisabled);
  $(".question-option").prop("disabled", isDisabled);
  $(".question-answer").prop("disabled", isDisabled);
  $(".short-answer").prop("disabled", isDisabled);
}

//------------------------------------------------------------------------------
//[[이벤트 핸들러]]
//------------------------------------------------------------------------------

let $delTestBtn = $("#del-test-btn");

$(document).on("click", "#modify-test-btn", function (e) {

  if ($(e.target).text() === "시험수정") {
    newTestInfo = originalTestInfo.clone();
    copyQuiz = originalQuiz.clone();
    newQuiz = originalQuiz.clone();

    $(".learner-tab").addClass("disabled");

    $(e.target).text("저장");
    $modifyTestBtn.attr("id", "confirm-modify");

    $delTestBtn.attr("id", "cancel-modify");
    $delTestBtn.find(".text").text("취소");

    setTestInfoFieldsDisabled(false);
    setQuestionFieldsDisabled(false);
    hideAllBtn(false);
    checkAllQuestionBtn(newQuiz);
  }

});

$(document).on("click", "#confirm-modify", function (e) {

  console.log(newQuiz);
  console.log(newTestInfo);

  console.log(JSON.stringify(newTestInfo));
  console.log(JSON.stringify(newQuiz));

  if ((JSON.stringify(originalQuiz) === JSON.stringify(newQuiz))
      && (JSON.stringify(originalTestInfo) === JSON.stringify(newTestInfo))) {

    Swal.fire({
      icon: "error",
      title: "변경사항 없음",
      text: "수정된 내용이 없습니다. 하나 이상의 항목을 변경해주세요."
    });

  } else {

    const questionArr = [];
    const testInfo = {
      "instructorId": "",
      "courseName": "",
      "testTitle": newTestInfo.title,
      "startDate": newTestInfo.startDate,
      "endDate": newTestInfo.endDate,
      "testTime": newTestInfo.testTime,
      "totalScore": newTestInfo.totalScore,
      "questions": questionArr,
    };

    $.each(newQuiz.questions, function (index, q) {

      const questionOptions = [];
      const question = {
        "questionNo": q.questionNo,
        "questionTitle": q.title,
        "questionScore": q.score,
        "questionType": q.type,
        "questionAnswer": q.answer ? q.answer : "",
        "options": questionOptions,
      };

      if (q.type === "MULTIPLE") {

        $.each(q.options, function (index, o) {

          let option = {
            "optionNo": o.optionNo,
            "optionContent": o.content,
            "isCorrect": o.isCorrect,
          };

          question.options.push(option);
        });
      }

      testInfo.questions.push(question);
    });

    console.log(JSON.stringify(testInfo));

    const testId = parseInt(UrlUtils.getPathSegment(2));
    apiCall("put", `/api/tests/${testId}`, testInfo, {},
        { "Content-Type": "application/json" })
    .then((res) => {
      console.log(res);

      const pageNo = UrlUtils.getQueryParam("currentPageNo");
      window.location.href = `/test/testDetail/${testId}?currentPageNo=${pageNo}&modified=true`;

    })
    .catch((err) => {
      console.log(err);

      handleValidationErrors(err.response.data);
    });
  }
});

$(document).on("click", "#cancel-modify", function (e) {
  console.log("수정 취소");
  $(".learner-tab").removeClass("disabled");
  newTestInfo = null;
  copyQuiz = null;
  newQuiz = null;

  const $confirmModify = $("#confirm-modify");
  const $cancelModify = $("#cancel-modify");

  $confirmModify.find(".text").text("시험수정");
  $confirmModify.attr("id", "modify-test-btn");

  $cancelModify.find(".text").text("시험삭제");
  $cancelModify.attr("id", "del-test-btn");

  setTestInfoFieldsDisabled(true);
  setQuestionFieldsDisabled(true);
  renderQuestions(originalQuiz.questions);
  hideAllBtn(true);

});

$(document).on("click", "#del-test-btn", function (e) {
  Swal.fire({
    title: "시험을 정말 삭제하시겠습니까?",
    text: "삭제된 시험은 복구할 수 없습니다.",
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#3085d6",
    cancelButtonColor: "#d33",
    confirmButtonText: "네, 삭제하겠습니다.",
    cancelButtonText: "아니요, 취소합니다."
  }).then((result) => {
    if (result.isConfirmed) {

      const testId = parseInt(UrlUtils.getPathSegment(2));
      const currentPageNo = UrlUtils.getQueryParam("currentPageNo");
      apiCall("delete", `/api/tests/${testId}`, null, {}, {})
      .then((res) => {
        Swal.fire({
          title: "삭제 완료",
          text: "시험이 성공적으로 삭제되었습니다.",
          icon: "success"
        }).then((result) => {
          if (result.isConfirmed) {

            window.location.href = `/test/testList?currentPageNo=${currentPageNo}&removed=true`;
          }
        });
      })
      .catch(err => console.log(err));

    }
  });
});

$(document).on("click", ".question-type", function (e) {

  // 해당 문항의 question-card 요소의 question-no 값 가져오기
  let $questionCard = $(e.target).closest(".question-card");
  const questionNo = $questionCard.data("question-no");

  // 클릭한 라디오 버튼의 값 가져오기
  const questionType = $(e.target).val();
  newQuiz.questions[questionNo - 1].setType(questionType);
  const type = copyQuiz.questions[questionNo - 1].type;
  const newType = newQuiz.questions[questionNo - 1].type;

  if (type === newType) {

    $questionCard.find(".question-answer-section").remove();

    // 가지고 있는 정보를 기반으로 뿌려주기
    $questionCard.find(".card-body").append(
        makeQuestionAnswerSection(copyQuiz.questions[questionNo - 1]));
    setQuestionFieldsDisabled(false);
    checkAllQuestionBtn(newQuiz);
  } else {

    $questionCard.find(".question-answer-section").empty();
    if (newType === "MULTIPLE") {

      $questionCard.find(".question-answer-section").append(`
        <label class="form-label">객관식 보기 및 정답 선택</label>
    <span><small class="text-muted"> (정답에 해당하는 보기 오른쪽 원형 버튼을 선택하세요.)</small></span>
    <span class="question-options-err-msg"></span>
    <div class="option-list">
      <div class="row align-items-center mb-2 option-row">
        <div class="col-10">
          <input
              type="text"
              class="form-control question-option"
              data-option-no="${questionNo}"
              placeholder="보기 1"
          />
        </div>
        <div class="col-1 text-center">
          <input class="question-answer" type="radio" name="question-${questionNo}"/>
        </div>
        <div class="col-1">
          <button type="button" class="btn btn-danger btn-icon-split btn-sm del-option-btn" disabled>
            <span class="text">삭제</span>
          </button>
        </div>
      </div>
      <div class="row align-items-center mb-2 option-row">
        <div class="col-10">
          <input
              type="text"
              class="form-control question-option"
              data-option-no="${questionNo}"
              placeholder="보기 2"
          />
        </div>
        <div class="col-1 text-center">
          <input class="question-answer" type="radio" name="question-${questionNo}"/>
        </div>
        <div class="col-1">
          <button
              type="button"
              class="btn btn-danger btn-icon-split btn-sm del-option-btn"
              disabled
          >
            <span class="text">삭제</span>
          </button>
        </div>
      </div>
    </div>
    <div class="row align-items-center mb-2">
      <div class="col-10">
        <button
            type="button"
            id="add-option-btn"
            class="btn btn-sm btn-secondary btn-icon-split add-option-btn"
            data-question-no="${questionNo}"
        >
          <span class="icon text-white-50">
            <i class="fas fa-plus"></i>
          </span>
          <span class="text">보기 추가</span>
        </button>
      </div>
    </div>
      `);

    } else if (newType === "SHORT") {

      $questionCard.find(".question-answer-section").append(`
      <label class="form-label">정답</label>
      <span><small class="text-muted"> (정답은 하나의 단어 또는 단답형 문장으로만 입력하세요.)</small></span>
      <span class="short-answer-err-msg"></span>
        <input
            type="text"
            class="form-control short-answer"
            placeholder="예: 정답"
        />
      `);

    }
  }
});

$(document).on("click", ".del-question-btn", function (e) {
  console.log($(e.target));
  const $delQuestionBtn = $(e.target).closest(".del-question-btn");
  if ($delQuestionBtn) {
    const $questionCard = $delQuestionBtn.closest(".question-card");
    const questionNo = $questionCard.data("question-no");
    copyQuiz.removeQuestion(questionNo);
    newQuiz.removeQuestion(questionNo);
    renderQuestions(newQuiz.questions);
    setQuestionFieldsDisabled(false);
    checkAllQuestionBtn(newQuiz);
  }
});

$(document).on("click", "#add-question-btn", function (e) {
  console.log(newQuiz);
  copyQuiz.addQuestion();
  newQuiz.addQuestion();
  console.log(newQuiz);
  renderQuestions(newQuiz.questions);
  setQuestionFieldsDisabled(false);
  checkAllQuestionBtn(newQuiz);
});

$(document).on("click", ".add-option-btn", function (e) {
  const $addOptionBtn = $(e.target).closest(".add-option-btn");
  const questionNo = $addOptionBtn.data("question-no");
  copyQuiz.questions.find(q => q.questionno === questionNo).addOption();
  newQuiz.questions.find(q => q.questionNo === questionNo).addOption();
  renderQuestions(newQuiz.questions);
  setQuestionFieldsDisabled(false);
  checkAllQuestionBtn(newQuiz);
});

$(document).on("click", ".del-option-btn", function (e) {
  const $delOptionBtn = $(e.target).closest(".del-option-btn");
  if ($delOptionBtn && $delOptionBtn.closest(".option-list")) {
    const $questionOption = $delOptionBtn.closest(".option-row").find(
        ".question-option");

    const questionNo = $delOptionBtn.closest(".question-card").data(
        "question-no");
    const optionNo = $questionOption.data("option-no");

    console.log(newQuiz);
    copyQuiz.questions.find(q => q.questionNo === questionNo).removeOption(
        optionNo);
    newQuiz.questions.find(q => q.questionNo === questionNo).removeOption(
        optionNo);

    // const question = newQuiz.questions.find(q => q.questionNo ===
    // questionNo); question.removeOption(optionNo);
    console.log(newQuiz);
    renderQuestions(newQuiz.questions);
    setQuestionFieldsDisabled(false);
    checkAllQuestionBtn(newQuiz);
  }
});

$(document).on("click", ".show-learner-answer-btn", async function (e) {
  console.log($(e.target).closest(".show-learner-answer-btn"));
  const testId = parseInt(UrlUtils.getPathSegment(2));
  const learnerId = $(e.target).closest(".show-learner-answer-btn")
                               .data("learner-id");
  const learnerName = $(e.target).closest(".learner-row").find(".fullname")
                                 .text();
  $(".learner-name").text(learnerName);

  const quizRes = await apiCall("get",
      `/api/my/tests/${testId}/learner/${learnerId}`);

  buildQuiz(userQuiz, quizRes.data.data.questions);
  console.log(userQuiz);
  buildUserAnswer(quizRes.data.data.questions,
      quizRes.data.data.userScore);
  console.log(userAnswer);
  renderQuestionResult(".answer-container", userQuiz, userAnswer);
});

$("#answerModal").on("hidden.bs.modal", () => {
  userQuiz = new Quiz();
  userAnswer = [];

  $(".answer-container").empty();
});

//------------------------------------------------------------------------------
// [[시험 수정]]
//------------------------------------------------------------------------------

$(document).on("blur", "#test-title", function (e) {
  newTestInfo.setTitle($(e.target).val().trim());
});

$(document).on("blur", "#start-date", function (e) {
  const startDateArr = $(e.target).val().split("T");
  newTestInfo.setStartDate(startDateArr[0] + " " + startDateArr[1] + ":00");
});

$(document).on("blur", "#end-date", function (e) {
  const endDateArr = $(e.target).val().split("T");
  newTestInfo.setEndDate(endDateArr[0] + " " + endDateArr[1] + ":00");
});

$(document).on("blur", "#test-time", function (e) {
  newTestInfo.setTestTime(parseInt($(e.target).val()));
});

$(document).on("blur", ".question-title", function (e) {
  const questionNo = $(e.target)
  .closest(".question-card").data("question-no");
  newQuiz.questions[questionNo - 1].title = $(e.target).val().trim();
});

// 문항 배점과 총 배점 연동
$(document).on("keyup", ".question-score", function (e) {
  const questionNo = $(e.target).closest(".question-card")
                                .data("question-no");
  let total = 0;

  $(".question-score").each(function () {
    let score = parseInt($(this).val());
    if (!isNaN(score)) {
      total += score;
    }
  });
  newQuiz.questions[questionNo - 1].score = parseInt(
      $(e.target).val().trim());
  newTestInfo.setTotalScore(newQuiz.totalScore);
  $("#total-score").val(total);
});

$(document).on("blur", ".question-option", function (e) {
  const questionNo = $(e.target).closest(".question-card")
                                .data("question-no");
  const optionNo = $(e.target).data("option-no");
  newQuiz.questions[questionNo - 1].options[optionNo - 1].setContent(
      $(e.target).val().trim());
});

$(document).on("change", ".question-answer", function (e) {
  const questionNo = $(e.target).closest(".question-card")
                                .data("question-no");
  const optionNo = $(e.target).closest(".option-row").find(
      ".question-option").data("option-no");
  newQuiz.questions[questionNo - 1].setCorrectOption(optionNo);
});

$(document).on("blur", ".short-answer", function (e) {
  const questionNo = $(e.target)
  .closest(".question-card").data("question-no");
  newQuiz.questions[questionNo - 1].answer = $(e.target).val().trim();
});

function renderCourseFilterOptionsForAdminForUser(data) {
  let $courseFilter = $("#courseSelector");
  $courseFilter.empty();
  let defaultCourse;

  $.each(data, function (index, el) {

    if (el.inProgress) {
      defaultCourse = el.courseName;
    }

    let courseOption = `<option value="${el.courseName}">${el.courseName}</option>`;
    $courseFilter.append(courseOption);
  });

  $courseFilter.val(defaultCourse);
}

const $courseSelector = $("#courseSelector");

$("#prev-page").on("click", function () {

  let prevPageNo = $(this).data("page-no");
  console.log(prevPageNo);

  let courseName = $courseSelector.val();
  console.log(courseName);

  location.href = `/test/testList?currentPageNo=${prevPageNo}&courseName=${courseName}`;
});

function renderQuestionResult(selector, quiz, userAnswer) {
  const $questionContainer = $(selector);
  $questionContainer.empty();

  $.each(quiz.questions, function (index, q) {

    $questionContainer.append(
        makeLearnerQuestionCard(q, userAnswer[index]));
  });
}

function makeLearnerQuestionCard(q, ua) {
  console.log(q);
  console.log(ua);

  const cardHeader = `
    <div class="card mb-4">
      <div class="card-header py-2">
        <div class="row align-items-center">
          <div class="col-1">
            <h6 class="m-0 font-weight-bold text-primary">문항<span>${q.questionNo}</span></h6>
          </div>
          <div class="col-2">
            <h6 class="m-0 font-weight-bold text-primary">
                ${q.type === "MULTIPLE" ? "객관식" : "주관식"}
            </h6>
          </div>
          <div class="col-auto ml-auto">
                  <span class="btn btn-${ua.userIsCorrect ? "info" : "danger"} btn-circle btn-sm">
                    <i class="fas fa-${ua.userIsCorrect ? "check" : "times"}"></i>
                  </span>
            <span class="text-${ua.userIsCorrect ? "info" : "danger"} text-md-center">
                ${ua.userIsCorrect ? "정답" : "오답"}
            </span>
          </div>
        </div>
      </div>
    `;

  let cardBody = `
    <div class="card-body">
      <div class="row mb-3">
        <div class="col-md-10">
          <label class="form-label">문항 설명</label>
          <input
              class="form-control"
              readonly
              type="text"
              value="${q.title}"
          />
        </div>
        <div class="col-md-2">
          <label class="form-label">배점</label>
          <input
              class="form-control"
              min="1"
              readonly
              type="number"
              value="${q.score}"
          />
        </div>
      </div> 
    `;

  console.log(q.options);
  if (q.type === "MULTIPLE") {

    cardBody += `<div class="mb-3">`;

    $.each(q.options, function (index, o) {

      console.log(o);
      console.log(ua);
      cardBody += `
            <div class="align-items-center mb-2 option-row ${parseInt(
          ua.userAnswer) === o.optionNo ? "text-danger" : ""}" >
            ${o.content}
            </div>
            `;
    });
    cardBody += `</div>`;

    const correctOpt = q.options.find(o => o.isCorrect) || {};
    const userSelectOpt = q.options.find(
        o => o.optionNo === parseInt(ua.userAnswer));

    cardBody += `
         <div class="row mb-3">
           <div class="col-md-6">
             <label class="form-label">제출 답</label>
             <input
               class="form-control"
               readonly
               type="text"
               value="${userSelectOpt.content || ""}"
             />
           </div>
           <div class="col-md-6">
             <label class="form-label">정답</label>
             <input
               class="form-control"
               readonly
               type="text"
               value="${correctOpt.content || ""}"
             />
           </div>
         </div>
        `;
  } else {

    cardBody += `
        <div class="row mb-2">
          <div class="col-md-12">
            <label class="form-label">제출 답</label>
            <input
              class="form-control"
              readonly
              type="text"
              value="${ua.userAnswer || ""}"
            />
          </div>
        </div>
        <div class="row mb-2">
          <div class="col-md-12">
            <label class="form-label">정답</label>
            <input
              class="form-control"
              readonly
              type="text"
              value="${q.answer || ""}"
            />
          </div>
        </div>       
        `;
  }

  cardBody += `</div>\n</div>`;

  return cardHeader + cardBody;
}

//------------------------------------------------------------------------------
// [[Web Socket]]
//------------------------------------------------------------------------------

let stompClient = null;

const testId = parseInt(UrlUtils.getPathSegment(2));
console.log(testId);

function connectStomp(testId) {
  const socket = new SockJS(`${location.origin}/ws`);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function () {
    console.log("STOMP 연결됨");

    // 시험 ID에 따라 특정 채널 구독
    stompClient.subscribe(`/topic/submissions/${testId}`,
        function (message) {
          const updatedLearner = JSON.parse(
              message.body);
          console.log(message);
          console.log(message.body);
          console.log(updatedLearner);

          console.log(testId);

          apiCall("get",
              `/api/tests/${testId}/learners`)
          .then((res) => {
            console.log(res);
            renderLearnerList(res.data.data);
            renderTestDelBtn(res.data.data);
          })
          .catch(error => console.log(error));
        });
  });
}

// 연결 실행
connectStomp(testId);

// '수강생 목록' 탭이 활성화되면 수정 버튼 숨기기
$("a[data-target=\"#learner-list\"]").on("shown.bs.tab", function () {
  $("#modify-test-btn").hide();
});

// '시험 내용' 탭이 활성화되면 수정 버튼 다시 보이기
$("a[data-target=\"#test-content\"]").on("shown.bs.tab", function () {
  $("#modify-test-btn").show();
});