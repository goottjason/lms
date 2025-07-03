const Toast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.onmouseenter = Swal.stopTimer;
    toast.onmouseleave = Swal.resumeTimer;
  }
});

$(document).ready(function () {

  const courseName = new URLSearchParams(window.location.search).get(
      "courseName");
  console.log(courseName);
  const pageNo = new URLSearchParams(window.location.search).get(
      "currentPageNo");
  $("#prev-page").data("page-no", pageNo);

  $("#courseSelector").prop("disabled", true);

  axios.get(`/api/courses`)
       .then(function (response) {
         console.log(response);
         renderCourseFilterOptionsForAdminForUser(response.data.data);
       })
       .catch(function (error) {
       });

  $("#test-register-form").on("submit", function (e) {
    e.preventDefault();
  });

  checkQuestionCnt(); // 최소 1문제, 최대 15문제
});

// 보기 최소 2개, 최대 4개
function checkOptionCnt(target) {
  let $questionAnswerSection = $(target); // ".question-answer-section" 요소

  let $addOptionBtn = $questionAnswerSection.find(".add-option-btn");
  let $optionRow = $questionAnswerSection.find(".option-list")
                                         .find(".option-row"); // 선택지"들"
  let $delOptionBtn = $questionAnswerSection.find(".option-list")
                                            .find(".del-option-btn"); // "보기
                                                                      // 삭제"
                                                                      // 버튼"들"

  if ($optionRow.length === 2) {
    // 보기 삭제 X
    $.each($delOptionBtn, function (i, item) {
      $(item).prop("disabled", true);
    });
  } else if ($optionRow.length === 4) {
    // 보기 추가 X
    $addOptionBtn.prop("disabled", true);
  } else {
    // 보기 삭제, 추가 O
    $.each($delOptionBtn, function (i, item) {
      $(item).prop("disabled", false);
    });
    $addOptionBtn.prop("disabled", false);
  }

}

// 문제는 최소 1문제, 최대 15문제
function checkQuestionCnt() {

  let $questionCard = $(".question-card");
  let questionCardCnt = $questionCard.length;

  if (questionCardCnt === 1) {
    // "문항 삭제" 버튼 X
    $("#del-question-btn").prop("disabled", true);
  } else if (questionCardCnt === 15) {
    // "문항 추가" 버튼 X
    $("#add-question-btn").prop("disabled", true);
  } else {
    // "문항 삭제", "문항 추가" 버튼 O
    $("#del-question-btn").prop("disabled", false);
    $("#add-question-btn").prop("disabled", false);
  }

  return questionCardCnt;
}

// 보기 번호 정렬
function reIndexOptionNumber(target) {
  let $target = $(target);
  let $optionList = $target.find(".option-list").find(".option-row");

  $.each($optionList, function (index, item) {
    let newOptionNum = index + 1;
    let $questionOptionInput = $(item).find(".question-option");
    $questionOptionInput.data("option-no", newOptionNum)
                        .attr("data-option-no", newOptionNum)
                        .attr("placeholder", `보기 ${newOptionNum}`);
  });

}

// 문항 번호 정렬
function reIndexQuestionNumber() {
  $(".question-card").each(function (index, item) {
    let newQuestionNum = index + 1;

    let $questionCard = $(item);
    $questionCard.data("question-no", newQuestionNum)
                 .attr("data-question-no", newQuestionNum);

    let $questionNo = $questionCard.find(".question-no");
    $questionNo.data("question-no", newQuestionNum)
               .attr("data-question-no", newQuestionNum)
               .text(newQuestionNum);

    let $questionType = $questionCard.find(".question-type");
    $.each($questionType, function (i, item) {
      console.log(item);
      $(item).attr("name", `question-type-${newQuestionNum}`);
    });

    let $questionScore = $questionCard.find(".question-score");
    $questionScore.data("question-no", newQuestionNum)
                  .attr("data-question-no", newQuestionNum);

    let $questionOptionRadioBtn = $questionCard.find(".question-answer");
    console.log($questionOptionRadioBtn);
    $.each($questionOptionRadioBtn, function (i, item) {
      console.log($(item));
      $(item).attr("name", `question-${newQuestionNum}`);
    });

  });
}

// 문제 유형 변경
$(document).on("click", ".question-type", function (e) {

  let questionType = $(e.target).val();

  let $questionCard = $(e.target).closest(".question-card");
  let questionNo = $questionCard.data("question-no");

  let $questionAnswerSection = $questionCard.find(".question-answer-section");
  $questionAnswerSection.empty();

  if (questionType === "MULTIPLE") {

    let multipleAnswerContainer = `
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
        `;

    $questionAnswerSection.append(multipleAnswerContainer);

  } else if (questionType === "SHORT") {

    let shortAnswerContainer = `
        <label class="form-label">정답</label>
          <span><small class="text-muted"> (정답은 하나의 단어 또는 단답형 문장으로만 입력하세요.)</small></span>  
          <span class="short-answer-err-msg"></span>
            <input            
                type="text"
                class="form-control short-answer"
                placeholder="예: 정답"
            />
        `;

    $questionAnswerSection.append(shortAnswerContainer);

  }

});

// 문항 배점과 총 배점 연동
$(document).on("keyup", ".question-score", function (e) {
  let total = 0;

  $(".question-score").each(function () {
    let score = parseInt($(this).val());
    if (!isNaN(score)) {
      total += score;
    }
  });

  $("#total-score").val(total);
});

// "문항 삭제" 버튼
$(document).on("click", ".del-question-btn", function () {
  $(this).closest(".question-card").remove();
  checkQuestionCnt();
  reIndexQuestionNumber();
});

// "보기 삭제" 버튼
$(document).on("click", ".del-option-btn", function (e) {

  let $questionAnswerSection = $(e.target)
  .closest(".question-answer-section");

  let $delOptionRow = $(e.target).closest(".option-row");

  $delOptionRow.remove();

  reIndexOptionNumber($questionAnswerSection);
  checkOptionCnt($questionAnswerSection);
});

// "보기 추가" 버튼
$(document).on("click", ".add-option-btn", function (e) {
  let $questionAnswerSection = $(e.target)
  .closest(".question-answer-section");
  let $optionList = $questionAnswerSection.find(".option-list");
  console.log($questionAnswerSection);

  let optionCnt = $questionAnswerSection.find(".question-option").length + 1;
  console.log(optionCnt);

  let questionOption = `
    <div class="row align-items-center mb-2 option-row">
      <div class="col-10">
        <input
            type="text"
            class="form-control question-option"
            data-option-no="${optionCnt}"
            placeholder="보기 ${optionCnt}"
        />
      </div>
      <div class="col-1 text-center">
        <input class="question-answer" type="radio" name="question-1"/>
      </div>
      <div class="col-1">
        <button type="button" class="btn btn-danger btn-icon-split btn-sm del-option-btn">
          <span class="text">삭제</span>
        </button>
      </div>
    </div>
    `;

  $optionList.append(questionOption);
  reIndexQuestionNumber();
  checkOptionCnt($questionAnswerSection);
});

// "문항 추가" 버튼
$(document).on("click", "#add-question-btn", function () {
  let $questionContainer = $(".question-list-box");
  let questionCardCnt = checkQuestionCnt() + 1;

  let questionCard = `
    <div class="card shadow mb-4 question-card" data-question-no="${questionCardCnt}">
      <div class="card-header py-2">
        <div class="row align-items-center">
          <div class="col-1">
            <h6 class="m-0 font-weight-bold text-primary">문항<span class="question-no" data-question-no="${questionCardCnt}">${questionCardCnt}</span></h6>
          </div>
          <div class="col-5">
            <div class="form-check form-check-inline">
              <input
                  class="form-check-input question-type"
                  type="radio"
                  name="question-type-${questionCardCnt}"
                  value="MULTIPLE"
                  checked
              />
              <label class="form-check-label">객관식</label>
            </div>
            <div class="form-check form-check-inline">
              <input
                  class="form-check-input question-type"
                  type="radio"
                  name="question-type-${questionCardCnt}"
                  value="SHORT"
              />
              <label class="form-check-label">주관식</label>
            </div>
          </div>
          <div class="col-auto ml-auto">
            <button id="del-question-btn" class="btn btn-sm btn-danger btn-icon-split del-question-btn">
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
                placeholder="예: 자바의 다형성에 대해 설명하시오"
            />
          </div>
          <div class="col-md-2">
            <label class="form-label">배점</label>
            <span class="question-score-err-msg"></span>
            <input
                type="number"
                class="form-control question-score"
                placeholder="예: 10"
                min="1"
            />
          </div>
        </div>
        <div class="mb-3 question-answer-section">
          <label class="form-label">객관식 보기 및 정답 선택</label>
          <span><small class="text-muted"> (정답에 해당하는 보기 오른쪽 원형 버튼을 선택하세요.)</small></span>
          <span class="question-options-err-msg"></span>

          <div class="option-list">
            <div class="row align-items-center mb-2 option-row">
              <div class="col-10">
                <input
                    type="text"
                    class="form-control question-option"
                    data-option-no="1"
                    placeholder="보기 1"
                />
              </div>
              <div class="col-1 text-center">
                <input class="question-answer" type="radio" name="question-${questionCardCnt}"/>
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
                    data-option-no="2"
                    placeholder="보기 2"
                />
              </div>
              <div class="col-1 text-center">
                <input class="question-answer" type="radio" name="question-${questionCardCnt}"/>
              </div>
              <div class="col-1">
                <button type="button" class="btn btn-danger btn-icon-split btn-sm del-option-btn" disabled>
                  <span class="text">삭제</span>
                </button>
              </div>
            </div>
          </div>

          <div class="row align-items-center mb-2">
            <div class="col-10">
              <button type="button" id="add-option-btn" class="btn btn-sm btn-secondary btn-icon-split add-option-btn">
                <span class="icon text-white-50">
                  <i class="fas fa-plus"></i>
                </span>
                <span class="text">보기 추가</span>
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>    
    `;

  $questionContainer.append(questionCard);

  checkQuestionCnt();
});

// 시험 등록 버튼
$("#test-register-btn").on("click", function () {
  let $testInfoContainer = $("#test-info-container");

  let questionArr = [];
  let testInfo = {
    "instructorId": "", // 강사 ID
    "courseName": $("#courseSelector").val(), // 강좌 ID
    "testTitle": $testInfoContainer.find("#test-title").val(), // 시험 제목
    "startDate": formatDateTime(
        $testInfoContainer.find("#start-date").val()), // 시험 시작일시
    "endDate": formatDateTime(
        $testInfoContainer.find("#end-date").val()), // 시험 종료일시
    "testTime": parseInt($testInfoContainer.find("#test-time").val()), // 시험
                                                                       // 시간
    "totalScore": parseInt($testInfoContainer.find("#total-score").val()), // 총
                                                                           // 배점
    "questions": questionArr // 시험 문항
  };

  // 시험 문항 가져오기
  let $questionListBox = $(".question-list-box");
  let $questionCard = $questionListBox.find(".question-card");
  $.each($questionCard, function (index, item) {

    let questionAnswer = "";

    let questionOptions = [];
    let question = {
      "questionNo": $(item).data("question-no"),
      "questionTitle": $(item).find(".question-title").val(),
      "questionScore": $(item).find(".question-score").val(),
      "questionType": $(item).find(".question-type:checked").val(),
      "questionAnswer": questionAnswer,
      "options": questionOptions,
    };

    // 문항번호
    let questionNo = $(item).data("question-no");
    console.log(questionNo);
    // 문항 제목
    let questionTitle = $(item).find(".question-title").val();
    console.log(questionTitle);
    // 문항 배점
    let questionScore = $(item).find(".question-score").val();
    console.log(questionScore);
    // 문항 유형
    let questionType = $(item).find(".question-type:checked").val();
    console.log(questionType);

    if (questionType === "SHORT") {
      // 단답형 주관식
      question.questionAnswer = $(item).find(".short-answer").val();
      console.log(questionAnswer);
      questionArr.push(question);
      return true;
    }

    if (questionType === "MULTIPLE") {
      // 객관식
      let $optionRow = $(item).find(".option-row");
      $.each($optionRow, function (index, item) {

        let option = {
          "optionNo": $(item).find(".question-option")
                             .data("option-no"),
          "optionContent": $(item).find(".question-option").val(),
          "isCorrect": $(item).find(".question-answer")
                              .is(":checked"),
        };
        // 선택지 번호
        let optionNo = $(item).find(".question-option")
                              .data("option-no");
        console.log(optionNo);
        // 선택지 내용
        let optionContent = $(item).find(".question-option").val();
        console.log(optionContent);
        // 선택지 정답 여부
        let isCorrect = $(item).find(".question-answer").is(":checked");
        console.log(isCorrect);

        console.log(option);
        questionOptions.push(option);
      });
    }
    console.log(questionOptions);

    questionArr.push(question);
    console.log(JSON.stringify(questionArr));

  });

  console.log(JSON.stringify(testInfo));

  axios.post(`/api/tests`, testInfo,
      { headers: { "Content-Type": "application/json" } })
       .then(function (response) {
         console.log(response);
         window.location.href = "/test/testList?saved=true";
       })
       .catch(function (error) {
         console.dir(error);
         console.log((error.response.data));
         // 에러 메시지 초기화 필요

         Toast.fire({
           icon: "error",
           title: "시험 등록에 실패했습니다.\n입력값을 확인해주세요."
         });

         handleValidationErrors(error.response.data);
       });
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

$("#prev-page").on("click", function () {

  let prevPageNo = $(this).data("page-no");
  console.log(prevPageNo);

  location.href = `/test/testList?currentPageNo=${prevPageNo}`;
});

