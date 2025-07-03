
const canvas = $("#canvas").get(0); //그림 그리는 판
let ctx = canvas.getContext("2d"); //그리기 객체(붓)

const signaturePad = new SignaturePad(canvas); // ctx를 기반으로 캔버스에 선을 그려주는 라이브러리

let history = []; // 데이터 객체 배열


function undo(){
  if(history.length > 0){
    history.pop();
    let previous = history.length > 0 ? history[history.length - 1] : [];
    console.log("previous",previous);
    signaturePad.clear();
    signaturePad.fromData(previous);
  }
}

function redo(){

}


$(function() {
  let data = signaturePad.toData();
  history.push(data);

  canvas.addEventListener("onend", function () {
    if(data.length > 0){
      history.push(data);
    }
    console.log("data",data);
  });


  $("#undo").on("click", function () {
    undo();
  })

  $("#redo").on("click", function () {
    console.log("history",history);
    console.log("data",data);
  })


});