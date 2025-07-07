

// 학생이 submissionDetail에 접근할 때
function submissionForLearner(s) {

  let homeworkId = $(s).data("id");

  // console.log("homeworkId",homeworkId);

  axios.get("/homework/submissionListForLearner?homeworkId=" + homeworkId)
  .then(function (response) {
    console.log("response", response);
    // let responseMessage = response.data.message;
    let responseParam = response.data.data;
    Swal.fire({
      title: '제출물로 이동합니다.',
      icon: 'info',
      confirmButtonText: '예'
    }).then((result) => {
      prevPage("/homework/submissionDetail?submissionId=" + responseParam);
      // location.href = "/homework/submissionDetail?submissionId=" + responseParam;
    })
  }).catch(function (error) {
    console.log("error", error);
    let errorMessage = error.response.data.message;
    Swal.fire({
      title: '제출물에 접근할 수 없습니다.',
      text: errorMessage,
      icon: 'error',
      confirmButtonText: '예'
    })
  })
}

$(function () {

  // 학생이 submissionDetail에 접근할 때
  $(".submissionBtnForLearner").on("click", function () {
    // alert("!");
    submissionForLearner(this);
  })

})