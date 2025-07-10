// 수정 뷰
let actualMap = new Map();
// let urlParams = new URLSearchParams(window.location.search);
// let trainingId = urlParams.get("trainingId");

function modifyView() {
  $("#detailTitle").html("훈련 일지 수정");

  Array.from($(".detail-actual")).forEach(function (e) {
    $(e).attr("contenteditable", "true");
    // console.log(typeof($(e).data("id"))); //number
  });

  // console.log("(수정 전)actualMap", actualMap);

  let output = "";
  output = `<button class="btn btn-primary" id = "modifyRegister">
<span class="icon text-white-50">
        <i class="fa fa-wrench"></i>
      </span>
        <span class="text px-3">수정 등록</span>
</button>
      <button class="btn btn-secondary" id = "toDetail"> 
      <span class="icon text-white-50">
          <i class="fas fa-list"></i>
      </span>
        <span class="text px-4">취소</span>
        </button>`
  $("#modifyDeleteDiv").html(output);

}

// 수정할 정보 취합
function modifyData() {
  // actual data 보내기

  Array.from($(".detail-actual")).forEach(function (e) {
    let text = $(e).text();
    actualMap.set($(e).data("id"), text);
    // console.log(typeof($(e).data("id"))); //number
  });

  let postMap = Object.fromEntries(actualMap)
  // console.log("(수정 후)postMap", postMap);

  let modifyFinalDTO = {
    postMap: postMap,
    trainingId: trainingId
  }

  axios.post("/training/trainingModify", JSON.stringify(modifyFinalDTO), {
    headers: {
      "Content-Type": "application/json"
    }
  })
  .then(function (response) {
    // console.log("response", response);
    let responseMessage = response.data.message;
    swal.fire({
      title: responseMessage,
      text: '훈련일지가 수정되었습니다.',
      icon: 'info',
      confirmButtonText: '예'
    }).then((result) => {
      location.href = "/training/trainingDetail?trainingId=" + trainingId;
    })
  }).catch(function (error) {
    // console.log("error", error);
    let errorMessage = error.response.data.message;
    let errorCode = error.response.data.code;
    let errorData = error.response.data.data;

    if (errorMessage != null && errorMessage.length > 0) {
      if (errorCode === 400) {
        // alert(errorMessage);
        Object.entries(errorData).forEach(([key, value]) => {
          let errorMsg = value[0].defaultMessage;
          // alert(errorMsg);
          if (errorMsg) {
            $("#fieldError").html(errorMsg);
          }
        })
        // $("#fieldError").html(errorData.);
      } else if (errorCode === 401 || errorCode === 404) {
        swal.fire({
          title: '훈련일지 수정 실패',
          text: error.data,
          icon: 'error',
          confirmButtonText: '예'
        }).then((result) => {
          location.href = "/training/trainingDetail?trainingId=" + trainingId;
        })
      } else {
        swal.fire({
          title: '훈련일지 수정 실패',
          text: errorMessage,
          icon: 'error',
          confirmButtonText: '예'
        }).then((result) => {
          location.href = "/training/trainingList";
        })
      }
    }
  })
}

// 게시글 삭제 function
function deleteTraining() {

  axios.post("/training/deleteTraining", {
    trainingIdBody: trainingId,
    courseIdBody: courseId
  })
  .then(function (response) {
    // console.log("response", response);
    location.href = "/training/trainingList";
  }).catch(function (error) {
    // console.log("error", error);
    let errorMessage = error.response.data.message;
    swal.fire({
      title: '훈련일지 삭제 실패',
      text: errorMessage,
      icon: 'error',
      confirmButtonText: '예'
    }).then((result) => {
      location.href = "/training/trainingList";
    })
  })
}

// $function 모음
$(function () {
  // console.log("test"); //성공

  //수정 뷰 보이기
  $("#modifyBtn").on("click", function () {
    // alert("!");
    modifyView();
    // contenteditable="true"
  })

  //수정할 정보 서버로 보내기
  $(document).on("click", "#modifyRegister", function () {
    // alert("!");
    if ($("")) {
      modifyData();
    }
  })

  // 삭제 모달 버튼 클릭 시
  $(document).on("click", "#deleteTraining", function () {
    // alert("!");

    deleteTraining();
  })

  // 취소 버튼 클릭시
  $(document).on("click", "#toDetail", function () {
    location.href = "/training/trainingDetail?trainingId=" + trainingId;
  })

})