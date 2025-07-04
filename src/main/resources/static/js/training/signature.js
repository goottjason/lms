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

function redo(){
  let restored = previous.pop();
  if (restored) {
    data.push(restored);
    signaturePad.clear();
    signaturePad.fromData(data);
  }
}

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

  $("#save").on("click", function () {
    let dataURL = signaturePad.toDataURL();
    console.log("dataURL", dataURL); //base64 인코딩한 문자열

    $("#signature").parent().append(` <img src="${dataURL}" id = "signatureImg" style="width: 100px; height: 100px;" />`);



  })



});