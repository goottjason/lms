let testInfo = null;
let quiz     = new Quiz();
let userAnswer;
let userScore;

$(document).ready(async function () {

    try {

        const testId = parseInt(UrlUtils.getPathSegment(2));
        $("#courseSelector").prop("disabled", true);
        console.log(testId);

        const coursesRes = await apiCall("get", "/api/courses");
        renderCourseFilterOptionsForAdminForUser(coursesRes.data.data);

        const testRes = await apiCall("get", `/api/tests/${testId}`);
        const data    = testRes.data.data;
        testInfo      = new TestInfo(data.testTitle, data.startDate,
                                     data.endDate, data.testTime,
                                     data.totalScore);
        renderTestHeader(testInfo);

        const courseName = UrlUtils.getQueryParam("courseName");
        console.log(courseName);

        const submissionRes    = await apiCall("get",
                                               `/api/my/tests/${testId}/submission`);
        const submissionStatus = submissionRes.data.data.submissionStatus;
        renderTestDetailPageForLearnerBySubmissionStatus(
            submissionRes.data.data);

        if (submissionStatus === "COMPLETED") {

            const quizRes = await apiCall("get", `/api/my/tests/${testId}`);
            console.log(quizRes);
            buildQuiz(quiz, quizRes.data.data.questions);
            buildUserAnswer(quizRes.data.data.questions,
                            quizRes.data.data.userScore);
            renderQuestionResult(".question-container", quiz, userAnswer);

        }

    } catch (err) {
        console.log(err);
    }

});

function buildUserAnswer(questions, score) {
    console.log(questions);
    userAnswer = [];
    $(".user-score").text(score);

    $.each(questions, function (index, q) {

        const userAnswerObj = {
            userAnswer   : q.userAnswer,
            userIsCorrect: q.userIsCorrect
        };

        userAnswer.push(userAnswerObj);
    });
}

function renderQuestionResult(selector, quiz, userAnswer) {
    const $questionContainer = $(selector);
    $questionContainer.empty();

    $.each(quiz.questions, function (index, q) {

        $questionContainer.append(makeQuestionCard(q, userAnswer[index]));
    });
}

function makeQuestionCard(q, ua) {
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

        const correctOpt    = q.options.find(o => o.isCorrect) || {};
        const userSelectOpt = q.options.find(
            o => o.optionNo === parseInt(ua.userAnswer)) || {};

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

function makeTestDetailPageBodyForLearner(submissionStatus) {
    const testDetailBody = {
        "NOT_STARTED": `
    <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
      <h6 class="m-0 font-weight-bold text-primary">시험 유의사항</h6>
    </div>
    <div class="card-body text-gray-800 pl-5 py-5">
      <p>시험은 한 번만 응시할 수 있습니다.</p>
      <p>제한 시간이 지나면 자동으로 제출됩니다.</p>
      <p>응시 중 브라우저를 종료하거나 새로고침하면 시험이 무효 처리될 수 있습니다.</p>
      <p>답안은 자동 저장되지 않으니 주기적으로 저장 버튼을 눌러주세요.</p>
      <p>부정행위가 적발될 경우 불이익이 있을 수 있습니다.</p>
    </div>
    <div class="card-footer">
      <div class="form-check">
        <input class="form-check-input" type="checkbox" id="a">
        <label class="form-check-label" for="a">
          (필수) 유의사항에 동의합니다.
        </label>
      </div>
    </div>     
    `,
        "IN_PROGRESS": `
    <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
      <h6 class="m-0 font-weight-bold text-primary">시험 유의사항</h6>
    </div>
    <div class="card-body text-gray-800 pl-5 py-5">
      <p>시험은 한 번만 응시할 수 있습니다.</p>
      <p>제한 시간이 지나면 자동으로 제출됩니다.</p>
      <p>응시 중 브라우저를 종료하거나 새로고침하면 시험이 무효 처리될 수 있습니다.</p>
      <p>답안은 자동 저장되지 않으니 주기적으로 저장 버튼을 눌러주세요.</p>
      <p>부정행위가 적발될 경우 불이익이 있을 수 있습니다.</p>
    </div>
    <div class="card-footer">
      <div class="form-check">
        <input class="form-check-input" type="checkbox" id="a">
        <label class="form-check-label" for="a">
          (필수) 유의사항에 동의합니다.
        </label>
      </div>
    </div>
    `,
        "COMPLETED"  : `    
    <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
      <div class="row align-items-center">
        <div class="col">
          <h6 class="m-0 font-weight-bold text-primary">제출 답안</h6>
        </div>
        <div class="col-auto mr-auto">
          시험점수 : <span class="text-info user-score">70</span>점
        </div>
      </div>
    </div>
    <div class="card-body question-container py-2">
      
    </div>
    `

    };

    return testDetailBody[submissionStatus];
}

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