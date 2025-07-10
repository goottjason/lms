const canvas = $("#canvas").get(0); //그림 그리는 판
let ctx = canvas.getContext("2d"); //그리기 객체(붓)

const signaturePad = new SignaturePad(canvas); // ctx를 기반으로 캔버스에 선을 그려주는 라이브러리

let previous = [];

let data;

function undo() {
  data = signaturePad.toData(); //초기화
  if (data.length > 0) {
    signaturePad.clear();
    previous.push(data.pop());
    signaturePad.fromData(data) //pop된 배열
  }
}

function redo() {
  let restored = previous.pop();
  if (restored) {
    data.push(restored);
    signaturePad.clear();
    signaturePad.fromData(data);
  }
}

// 서명 저장 시 해당 관리자에게 알림 보내기

$(function () {
  signaturePad.onEnd = function () {
    data = signaturePad.toData(); //초기화
    console.log("onEnd 후 data", data);
  }

  //되돌리기
  $("#undo").on("click", function () {
    undo();
  })

  //다시 하기
  $("#redo").on("click", function () {

    redo();

  })

  function sendSignature(base64) {
    axios.post("/training/signature", base64)
    .then(function (response) {
      // console.log(response);
      // let responseData = response.data.data;
      let userArr = [instructorId];
      let msg = response.data.message;
      swal.fire({
        title: msg,
        icon: "success",
        confirmButtonText: "예"
      }).then((result) => {
        sendNotification(userArr, "관리자의 서명이 등록되었습니다.", false,
            "/training/trainingDetail?trainingId=" + trainingId);
        location.href = "/training/trainingDetail?trainingId=" + trainingId;
      })
    }).catch(function (error) {
      // console.log(error);
      let errorMsg = error.response.data.message;
      swal.fire({
        title: "서명 저장 실패",
        text: errorMsg,
        icon: "error",
        confirmButtonText: "예"
      })
    })
  }

  function isSignatureEmpty() {
    let dataURL = signaturePad.toDataURL();
    // console.log("dataURL", dataURL); //base64 인코딩한 문자열

    let base64 = {
      "dataURL": dataURL,
      "trainingId": trainingId
    }

    if (signaturePad.isEmpty()) {
      swal.fire({
        title: "서명을 입력해주세요",
        icon: "warning",
        confirmButtonText: "예"
      });
      return;

    } else {
      sendSignature(base64);
    }
  }



  $("#save").on("click", function () {
    // 서명이 비었는지 검사 -> 비었으면 제출 x, 아니면 제출
    isSignatureEmpty();

    // $("#signature").parent().append(` <img src="${dataURL}" id =
    // "signatureImg" style="width: 100px; height: 100px;" />`);

  })

});