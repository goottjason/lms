const $courseSelect = $("#courseSelector");

const $writer = $("#qna-writer");
const $qnaRegDate = $("#qna-regdate");
const $qnaCommentStatus = $("#qna-comment-status");
const $commentRegDate = $("#comment-regdate");
const $qnaTitle = $("#qna-title");
const $qnaContent = $("#qna-content");

const $qnaModifyBtn = $("#qna-modify-btn");
const $qnaDeleteBtn = $("#qna-delete-btn");

const $commentRegisterBtn = $(".js-comment-register");
const $commentModifyBtn = $(".js-comment-modify");
const $commentDeleteBtn = $(".js-comment-delete");

const $commentTextarea = $("#qna-comment-text");
const $qnaCommentContainer = $(".qna-comment-container");
const $commentBody = $qnaCommentContainer.find(".card-body");

let writerId;
let userId;

let userType;
let boardNo;
let existingComment;

$(document).ready(async function () {
  userId = $("#login-user-id").val();

  await getUserCourses();
  $courseSelect.prop("disabled", true);
  const courseName = new URLSearchParams(window.location.search).get(
      "searchOptions.courseName");
  $courseSelect.val(courseName);

  const queryString = window.location.search || "";
  $("#prev-page").attr("href", `/courseBoardQnA/list` + queryString);

  boardNo = UrlUtils.getPathSegment(2);

  const detailRes = await fetchQnADetail(boardNo);
  console.log(detailRes);

  writerId = detailRes.data.data.loginId;

  if (userId !== writerId) {
    $qnaModifyBtn.remove();
    $qnaDeleteBtn.remove();
  }

  userType = detailRes.data.message;
  renderQnADetailPageByUserType(userType, detailRes.data.data);
  renderQnAUpdateBtn(detailRes.data.data);
  renderDetailData(detailRes.data.data);
  renderAttachedFiles(detailRes.data.data.uploadFiles);

  $(".js-comment-confirm, .js-comment-cancel").hide();

  connectStomp(boardNo);
});

//------------------------------------------------------------------------------
// [[상세 페이지 render]]
//------------------------------------------------------------------------------

function renderQnADetailPageByUserType(userType, data) {

  const isAnswer = data.isAnswer;

  if (userType === "INSTRUCTOR") {
    // 강사는 원래 폼 그대로 보여주기
    $commentBody.show();
    $commentTextarea.show();
    $commentRegisterBtn.show();
    $commentModifyBtn.show();
    $commentDeleteBtn.show();
    return;
  }

  // 비강사: 입력 폼·버튼 전부 숨기기
  $commentTextarea.hide();
  $commentRegisterBtn.hide();
  $commentModifyBtn.hide();
  $commentDeleteBtn.hide();

  // 답변 전/후 내용만 보여주기
  if (!isAnswer) {
    $commentBody.html(
        `<div class="text-center text-muted py-4">
         아직 답변이 등록되지 않았습니다.
       </div>`
    );
  } else {
    $commentBody.html(`
      <div class="p-3">
        <p class="mb-2">${data.comment}</p>
        <small class="text-muted">답변일: ${data.commentCreatedAt}</small>
      </div>
    `);
  }
}

function renderQnAUpdateBtn(data) {

  const isAnswer = data.isAnswer;

  if (userType !== "LEARNER") {
    $qnaModifyBtn.remove();
    $qnaDeleteBtn.remove();
  }

  if (isAnswer) {
    $qnaModifyBtn.remove();
    $qnaDeleteBtn.remove();

    $(".js-comment-register").remove();
  } else {
    $(".js-comment-modify").remove();
    $(".js-comment-delete").remove();
  }
}

function renderDetailData(data) {

  let commentStatus = data.isAnswer === false ? "답변 전" : "답변완료";

  $writer.text(data.fullName);
  $qnaRegDate.text(data.createdAt);
  $qnaCommentStatus.text(commentStatus);
  $commentRegDate.text(commentStatus === "답변 전" ? "-" : data.commentCreatedAt);

  $qnaTitle.text(data.title);
  $qnaContent.text(data.content);

  if (data.isAnswer) {
    existingComment = data.comment || "";
    $commentTextarea.val(data.comment).prop("disabled", true);
  }

}

function renderAttachedFiles(files) {
  const $row = $("#qna-attachment-row");
  $row.empty();

  if (!files || !files.length) {
    return $row.append(`
      <div class="col-12 text-muted">
        첨부파일이 없습니다.
      </div>
    `);
  }

  files.forEach(file => {
    // 카드 틀
    const $col = $(`
      <div class="col-4 mb-2">
        <div class="card">
          <div class="row g-0">
            <div class="col-md-3 p-1 thumb"></div>
            <div class="col-md-9 d-flex align-items-center">
              <div class="card-body p-2">
                <a href="${file.path}" download class="card-title mb-0 text-truncate">
                  ${file.originalName}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    `);

    // 썸네일 또는 아이콘
    const isImage = /\.(jpe?g|png|gif|bmp)$/i.test(file.originalName);
    if (isImage) {
      $col.find(".thumb").html(`
        <img src="${file.path}" 
             class="img-fluid rounded-start" 
             style="width:60px; height:60px; object-fit:cover;" />
      `);
    } else {
      $col.find(".thumb").html(`
        <div class="d-flex justify-content-center align-items-center" 
             style="width:60px; height:60px;">
          <i class="fas fa-file-alt fa-2x text-gray-300"></i>
        </div>
      `);
    }

    $row.append($col);
  });
}

//------------------------------------------------------------------------------
// [[글 수정 이벤트]]
//------------------------------------------------------------------------------

$qnaModifyBtn.on("click", function () {

  const queryString = window.location.search || "";
  window.location.href = `/courseBoardQnA/modify/${boardNo}` + queryString;
});

//------------------------------------------------------------------------------
// [[답변 등록 이벤트]]
//------------------------------------------------------------------------------
$(document).on("click", ".js-comment-register", function () {
  const commentText = $commentTextarea.val().trim();
  if (!commentText) {
    return Swal.fire({
      icon: "warning",
      title: "답변 내용을 입력해주세요.",
      confirmButtonText: "확인"
    });
  }
  axios.put(`/api/qna/comment/${boardNo}`, { comment: commentText })
       .then(() => Swal.fire({ icon: "success", title: "답변이 등록되었습니다." }))
       .then(() => {
         const qs = window.location.search || "";
         location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
       })
       .catch(err => {
         Swal.fire({
           icon: "error",
           title: "등록 실패",
           text: err.response?.data?.message || "서버 오류가 발생했습니다."
         });
       });
});

//------------------------------------------------------------------------------
// [[답변 수정 진입 이벤트]]
//------------------------------------------------------------------------------
$(document).on("click", ".js-comment-modify", function () {
  $commentTextarea.prop("disabled", false);
  // 수정/삭제 버튼 숨기고, 저장/취소 버튼 보이기
  $(".js-comment-modify, .js-comment-delete, .js-comment-register").hide();
  $(".js-comment-confirm, .js-comment-cancel").show();
});

//------------------------------------------------------------------------------
// [[답변 저장(수정) 이벤트]]
//------------------------------------------------------------------------------
$(document).on("click", ".js-comment-confirm", function () {
  const newComment = $commentTextarea.val().trim();
  if (!newComment) {
    return Swal.fire({ icon: "warning", title: "수정할 답변을 입력해주세요." });
  }
  if (newComment === existingComment) {
    return Swal.fire({ icon: "info", title: "달라진 내용이 없습니다." });
  }
  axios.put(`/api/qna/comment2/${boardNo}`, { comment: newComment })
       .then(() => Swal.fire({ icon: "success", title: "답변이 수정되었습니다." }))
       .then(() => {
         const qs = window.location.search || "";
         location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
       })
       .catch(() => {
         Swal.fire({ icon: "error", title: "수정 실패", text: "서버 오류가 발생했습니다." });
       });
});

//------------------------------------------------------------------------------
// [[수정 취소 이벤트]]
//------------------------------------------------------------------------------
$(document).on("click", ".js-comment-cancel", function () {
  // 기존 상태로 복원
  $commentTextarea.prop("disabled", true).val(existingComment);
  $(".js-comment-confirm, .js-comment-cancel").hide();
  $(".js-comment-modify, .js-comment-delete, .js-comment-register").show();
});

//------------------------------------------------------------------------------
// [[답변 삭제 이벤트]]
//------------------------------------------------------------------------------
$(document).on("click", ".js-comment-delete", function () {
  Swal.fire({
    title: "정말 삭제하시겠습니까?",
    text: "삭제된 답변은 복구할 수 없습니다.",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "삭제",
    cancelButtonText: "취소"
  }).then(result => {
    if (!result.isConfirmed) {
      return;
    }
    axios.put(`/api/qna/comment/${boardNo}/deleted`)
         .then(() => Swal.fire({ icon: "success", title: "답변이 삭제되었습니다." }))
         .then(() => {
           const qs = window.location.search || "";
           location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
         })
         .catch(() => Swal.fire(
             { icon: "error", title: "삭제 실패", text: "오류가 발생했습니다." }));
  });
});

// //------------------------------------------------------------------------------
// // [[답변 이벤트]]
// //------------------------------------------------------------------------------
//
// $commentRegisterBtn.on("click", function () {
//
//   const commentText = $commentTextarea.val().trim();
//
//   // 내용이 없으면 경고
//   if (!commentText) {
//     Swal.fire({
//       icon: "warning",
//       title: "답변 내용을 입력해주세요.",
//       confirmButtonText: "확인"
//     });
//     return;
//   }
//
//   try {
//     axios.put(`/api/qna/comment/${boardNo}`, {
//       comment: commentText
//     })
//          .then(() => {
//            return Swal.fire({
//              icon: "success",
//              title: "답변이 등록되었습니다.",
//              confirmButtonText: "확인"
//            });
//          })
//          .then(() => {
//            // 목록 옵션을 유지하며 상세 페이지 리프레시
//            const qs = window.location.search || "";
//            window.location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
//          });
//
//   } catch (err) {
//     console.error(err);
//     Swal.fire({
//       icon: "error",
//       title: "등록 실패",
//       text: err.response?.data?.message || "서버 오류가 발생했습니다."
//     });
//   }
//
// });
//
// $(document).on("click", "#comment-modify-btn", function () {
//
//   // 텍스트 활성화
//   $commentTextarea.prop("disabled", false);
//
//   $commentModifyBtn.attr("id", "confirm-modify-btn");
//   $commentModifyBtn.find(".text").text("답변 저장");
//
//   $commentDeleteBtn.attr("id", "cancel-modify-btn");
//   $commentDeleteBtn.find(".text").text("수정 취소");
//
// });
//
// $(document).on("click", "#confirm-modify-btn", function () {
//
//   const newComment = $commentTextarea.val().trim();
//
//   // 빈 문자열 검사
//   if (!newComment) {
//     return Swal.fire({
//       icon: "warning",
//       title: "수정할 답변을 입력해주세요."
//     });
//   }
//
//   // 기존 내용과 동일한지 검사
//   if (newComment === existingComment) {
//     return Swal.fire({
//       icon: "info",
//       title: "달라진 내용이 없습니다.",
//       text: "기존 댓글과 다른 내용을 입력해주세요."
//     });
//   }
//
//   // API 호출
//   try {
//     axios.put(`/api/qna/comment2/${boardNo}`, { comment: newComment });
//     Swal.fire({
//       icon: "success",
//       title: "답변이 수정되었습니다."
//     }).then(() => {
//       // 상세 페이지 새로고침 (쿼리스트링 유지)
//       const qs = window.location.search || "";
//       window.location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
//     });
//   } catch (err) {
//     console.error(err);
//     Swal.fire({
//       icon: "error",
//       title: "수정 실패",
//       text: err.response?.data?.message || "서버 오류가 발생했습니다."
//     });
//   }
// });
//
// $(document).on("click", "#cancel-modify-btn", function () {
//
//   $commentTextarea.prop("disabled", true).val(existingComment);
//
//   $commentModifyBtn.attr("id", "comment-modify-btn");
//   $commentModifyBtn.find(".text").text("답변 수정");
//
//   $(this).attr("id", "comment-delete-btn");
//   $(this).find(".text").text("답변 삭제");
// });

//------------------------------------------------------------------------------
// [[글 삭제 이벤트]]
//------------------------------------------------------------------------------

$(document).on("click", "#comment-delete-btn", function () {
  console.log($(this).text().trim());

  if ($(this).text().trim() !== "답변 삭제") {
    return;
  }

  Swal.fire({
    title: "정말 삭제하시겠습니까?",
    text: "삭제된 글은 복구할 수 없습니다.",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "삭제",
    cancelButtonText: "취소"
  }).then((result) => {
    if (result.isConfirmed) {
      axios.put(`/api/qna/${boardNo}/deleted`)
           .then((res) => {
             Swal.fire("삭제 완료", "글이 삭제되었습니다.", "success")
                 .then(() => {
                   const qs = window.location.search;
                   window.location.href = `/courseBoardQnA/list${qs}`;
                 });
           })
           .catch((err) => {
             Swal.fire("삭제 실패", "오류가 발생했습니다.", "error");
           });
    }
  });
});

// //------------------------------------------------------------------------------
// // [[답변 삭제 이벤트]]
// //------------------------------------------------------------------------------
//
// $commentDeleteBtn.on("click", function () {
//   Swal.fire({
//     title: "정말 삭제하시겠습니까?",
//     text: "삭제된 답변은 복구할 수 없습니다.",
//     icon: "warning",
//     showCancelButton: true,
//     confirmButtonText: "삭제",
//     cancelButtonText: "취소"
//   }).then((result) => {
//     if (result.isConfirmed) {
//       axios.put(`/api/qna/comment/${boardNo}/deleted`)
//            .then(() => {
//              Swal.fire("삭제 완료", "답변이 삭제되었습니다.", "success")
//                  .then(() => {
//                    const qs = window.location.search;
//                    window.location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
//                  });
//            })
//            .catch(() => {
//              Swal.fire("삭제 실패", "오류가 발생했습니다.", "error");
//            });
//     }
//   });
// });

//------------------------------------------------------------------------------
// [[이전 페이지]]
//------------------------------------------------------------------------------

const $prevPage = $("#prev-page");

$prevPage.on("click", function () {

  const qs = window.location.search;
  window.location.href = `/courseBoardQnA/list${qs}`;
});

//------------------------------------------------------------------------------
// [[Web Socket]]
//------------------------------------------------------------------------------

let stompClient = null;

console.log(boardNo);

function connectStomp(boardNo) {
  const socket = new SockJS(`${location.origin}/ws`);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function () {
    console.log("STOMP 연결됨");

    stompClient.subscribe(`/topic/qna/${boardNo}`,
        async function (message) {
          const updatedLearner = JSON.parse(
              message.body);

          console.log(updatedLearner);

          try {
            const detailRes = await fetchQnADetail(boardNo);
            console.log(detailRes);

            userType = detailRes.data.message;
            renderQnADetailPageByUserType(userType, detailRes.data.data);
            renderQnAUpdateBtn(detailRes.data.data);
            renderDetailData(detailRes.data.data);
          } catch (e) {
            console.log("fetchQnADetail 중 에러", e);
          }

        });
  });
}

connectStomp(UrlUtils.getPathSegment(2));

//------------------------------------------------------------------------------
// [[필터]]
//------------------------------------------------------------------------------
// 사용자(수강생/강사) 필터 호출
function getUserCourses() {

  return fetchUserCourses()
  .then((res) => {
    renderUserCourseOptions("#courseSelector", res.data.data);
  })
  .catch((err) => console.log(err));
}

// 사용자(수강생/강사) 필터 생성
function renderUserCourseOptions(selector, data) {
  let $adminCourseSelect = $(selector);
  $adminCourseSelect.empty();

  // 진행중인 강좌 요소 찾기
  const defaultEl = data.find(el => el.inProgress) || data[0];
  const selectedCourseName = defaultEl ? defaultEl.courseName : "";

  $.each(data, function (index, el) {

    $adminCourseSelect.append(
        `<option value="${el.courseName}" data-is-in-progress="${el.inProgress}">${el.courseName}</option>`);
  });

  // if (selectedCourse === null || selectedCourse === undefined) {
  //     selectedCourse = selectedCourseName;
  //     $adminCourseSelect.val(selectedCourseName);
  // }

  // $adminCourseSelect.val(selectedCourse);
}

