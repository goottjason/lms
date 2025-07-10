// 달력칸 클릭 시, 등록 페이지로 이동 (강사만 해당)
//날짜 파싱(훈련 일자)
function parseDate(calendarDay) {

  //calendarDay는 일수를 나타내는 칸
  let registerDay = $(calendarDay).find(".day-number").text().padStart(2, "0"); //일수
  let registerTitle = $(".calendar-title").text();
  let registerYear = registerTitle.split("년")[0];
  let registerMonth = registerTitle.split("년")[1].split("월")[0].trim().padStart(
      2, "0");
  // console.log(registerDay); //성공
  // console.log(registerYear); //성공
  // console.log(registerMonth); //성공

  let registerDate = `${registerYear}-${registerMonth}-${registerDay}`; //보낼 날짜
  // console.log(registerDate);

  if (registerDay !== "00" && loginUserType === "INSTRUCTOR") {
    location.href = `/training/trainingRegister?registerDate=`
        + encodeURIComponent(registerDate);
  }

}

//현재 리스트 페이지 저장?

//과정명은 컨트롤러에서 현재 과정명 추가

$(function () {

  // console.log("trainingList"); 성공
  $(document).on("click", ".calendar-day", function () {
    parseDate(this);
  })

})