let emailValid = false;

$(function(){


    // 유저 타입 셀렉터 이벤트
    $("#user-type-selector").change(function(){

        clearErr();
        if(emailValid){
            $("#email-msg").text("사용가능한 이메일입니다.").removeClass("text-danger").addClass("text-info");
        } else {
            $("#email-msg").text("이메일 사용가능 여부 확인을 해주세요.").removeClass("text-info").addClass("text-danger");
        }

        if($(this).val() == "LEARNER"){
            $(".staff-detail-box").empty();
        } else if ($(this).val() == "INSTRUCTOR"){

            let output = `
                <div class="col-md-6">
                    <p class="mb-1 pb-1">직급
                      <span class="text-danger">*</span>&nbsp;&nbsp;
                      <span id="position-msg" class="text-danger small error-box"></span>
                    </p>
                    <select id="position" class="form-control" name="position">
                      <option value="">선택</option>
                      <option value="FULLTIME_INSTRUCTOR">전임강사</option>
                    </select>
                  </div>
                  <div class="col-md-6">
                    <div class="form-group">
                      <label for="hire-date">입사일자<span class="text-danger">*</span>&nbsp;&nbsp;</label>
                      <span id="hire-date-msg" class="text-danger small error-box"></span>
                      <input
                        type="date"
                        id="hire-date"
                        name="hireDate"
                        class="form-control"
                        >
                    </div>
                  </div>`;
            $(".staff-detail-box").html(output);
        } else {
            let output = `
                <div class="col-md-6">
                    <p class="mb-1 pb-1">직급
                      <span class="text-danger">*</span>&nbsp;&nbsp;
                      <span id="position-msg" class="text-danger small error-box"></span>
                    </p>
                    <select id="position" class="form-control" name="position">
                      <option value="">선택</option>
                      <option value="GENERAL_MANAGER">총괄실장</option>
                      <option value="COURSE_HEAD">학과장</option>
                    </select>
                  </div>
                  <div class="col-md-6">
                    <div class="form-group">
                      <label for="hire-date">입사일자<span class="text-danger">*</span>&nbsp;&nbsp;</label>
                      <span id="hire-date-msg" class="text-danger small error-box"></span>
                      <input
                        type="date"
                        id="hire-date"
                        name="hireDate"
                        class="form-control"
                        >
                    </div>
                  </div>`;
            $(".staff-detail-box").html(output);

        }

    });

    $("#email").change(function(){
        showErr("email", "이메일 사용가능 여부 확인을 해주세요.");
        emailValid = false;
    })

    // 이메일 확인
    $("#email-check-btn").click(function(e){
       e.preventDefault();
        let emailRegExp    = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        let inputEmail = $("#email").val();


        if (!emailRegExp.test(inputEmail)){
            $("#email-msg").text("유효하지 않은 이메일 주소입니다.").removeClass("text-info").addClass("text-danger");
            emailValid = false;
            return;

        }

        $.ajax({
                   url: "/operationsManagement/userRegister/checkEmail",
                   type    : "GET",
                   dataType: "text", // 수신받을 데이터의 타입 (MIME TYPE)
                   data       : {
                       inputEmail : inputEmail
                   },
                   contentType: "application/json; charset=utf-8",
                   async   : false, // 비동기옵션 off
                   success : function (data) { // 통신이 성공하면 수행할 함수

                       if(data == "availableEmail"){
                           $("#email-msg").text("사용가능한 이메일입니다.").removeClass("text-danger").addClass("text-info");
                           emailValid = true;
                       } else {
                           $("#email-msg").text("사용중인 이메일입니다.").removeClass("text-info").addClass("text-danger");
                           emailValid = false;
                       }

                   },
                   error   : function () {},
                   complete: function () {}
                   });



    });

    $("#register-btn").click(function(e){
        e.preventDefault();

        clearErr();

        if($("#user-type-selector").val() == "LEARNER"){

            if(checkLearnerValid()){

                $("#user-register-form").submit();

            }

        } else {

            if(checkStaffValid()){

                $("#user-register-form").submit();

            }

        }



    });

    $("#cancel-btn").click(function(e){
       e.preventDefault();
       $("#fullname").val("");
       $("#email").val("");
       $("#birthday").val("");
       $('input[name="gender"]').prop('checked', false);
       $("#position").val("");
       $("#hire-date").val("");

       emailValid = false;
       clearErr();
       $("#email-msg").text("이메일 사용가능 여부 확인을 해주세요.").removeClass("text-info").addClass("text-danger");


    });


});

function checkLearnerValid(){
    let result = true;

    if($("#fullname").val().length < 2 || $("#fullname").val().length > 20){
        showErr("fullname", "이름은 2글자 이상 20글자 이하로 입력하셔야 합니다.")
        result = false;
    }


    if (!emailValid){
        $("#email-msg").text("이메일 사용가능 여부 확인을 해주세요.").removeClass("text-info").addClass("text-danger");
        result = false;
    } else {
        $("#email-msg").text("사용가능한 이메일입니다.").removeClass("text-danger").addClass("text-info");
    }

    if($("#birthday").val() == ""){
        showErr("birthday", "생년월일을 입력해주세요.");
        result = false;
    }

    if($('input[name="gender"]:checked').val() == undefined){
        $("#gender-msg").text("성별을 입력해주세요.")
        result = false;
    }

    return result;
}

function showErr (errNextId, errMsg){
    $(`#${errNextId}`).prev().text(errMsg);
}

function clearErr(){
    $(".error-box").empty();
}

function checkStaffValid(){
    let result = true;

    if($("#fullname").val().length < 2 || $("#fullname").val().length > 20){
        showErr("fullname", "이름은 2글자 이상 20글자 이하로 입력하셔야 합니다.")
        result = false;
    }


    if (!emailValid){
        $("#email-msg").text("이메일 사용가능 여부 확인을 해주세요.").removeClass("text-info").addClass("text-danger");
        result = false;
    } else {
        $("#email-msg").text("사용가능한 이메일입니다.").removeClass("text-danger").addClass("text-info");
    }

    if($("#birthday").val() == ""){
        showErr("birthday", "생년월일을 입력해주세요.");
        result = false;
    }

    if($('input[name="gender"]:checked').val() == undefined){
        $("#gender-msg").text("성별을 입력해주세요.")
        result = false;
    }


    if($("#position").val() == ""){
        $("#position-msg").text("직급을 입력해주세요.")
        result = false;
    }

    if($("#hire-date").val() == ""){
        showErr("hire-date", "입사일자를 입력해주세요.")
        result = false;
    }

    return result;
}

function cancelAll(){
    clearErr();
    $("#fullname").val("");
    $("#email").val("");
    $("#birthday").val("");
    $('input[name="gender"]').prop('checked', false);

    if($("#user-type-selector").val() != "LEARNER"){
        $("#position").val("");
        $("#hire-date").val("");
    }

}