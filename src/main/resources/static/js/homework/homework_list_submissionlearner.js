
function submissionForLearner(s) {

  let homeworkId = $(s).data("id");

  // console.log("homeworkId",homeworkId);


  axios.get("/homework/submissionListForLearner?homeworkId="+homeworkId)
  .then(function (response) {
    console.log("response", response);
    let responseMessage = response.data.message;
  }).catch(function (error) {
    console.log("error", error);
    let errorMessage = error.response.data.message;
  })


}


$(function () {

  // console.log("test");
  $(".submissionBtnForLearner").on("click", function () {
    // alert("!");
    submissionForLearner(this);
  })

})