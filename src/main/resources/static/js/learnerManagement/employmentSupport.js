let categorizeType = 0;
let pageNo = 1;
let isWithSearch = false;



$(function(){


    if(loginUser.type == "INSTRUCTOR"){

        let data = getCoursesByInProgress(-1)
        showCourseSelector(data);
        getEmploymentList();

        $("#courseSelector").change(function(){
            pageNo = 1;
            isWithSearch = false;
            $("#search-input").val("");
            getEmploymentList();
        });


    }

    if(loginUser.type == "ADMINISTRATOR"){

        let data = getCoursesByInProgress($("#first-category").val());
        showSecondCategory(data);
        getEmploymentList();

        $("#first-category").change(function(){

            pageNo = 1;
            isWithSearch = false;
            $("#search-input").val("");
            let data = getCoursesByInProgress($("#first-category").val());
            showSecondCategory(data);
            getEmploymentList();

        });

        $("#second-category").change(function(){

            pageNo = 1;
            isWithSearch = false;
            $("#search-input").val("");
            getEmploymentList();

        });

    }

    $("#search-btn").click(function(){
        pageNo = 1;
        // console.log($("#search-input").val().length);
        if($("#search-input").val().length > 0){
            isWithSearch = true;
        } else {
            isWithSearch = false;
        }
        getEmploymentList();

    });

    $("#search-input").keydown(function(e){
       if (e.keyCode == 13){

           pageNo = 1;
           if($("#search-input").val().length > 0){
               isWithSearch = true;
           } else {
               isWithSearch = false;
           }
           getEmploymentList();
       }
    });

    $(document).on("click", ".employment-modal-btn", function(){

        // $("#employmentModal").find("input").prop("readonly", true);
        // $("#save-employment-btn").hide();
        // $("#modify-employment-btn").show();
        //
        // $("#save-employment-btn").attr("data-id", "");
        // $("#company-name").val("");
        // $("#company-phone").val("");
        // $("#company-address").val("");
        // $("#counseling-details").val("");
        //
        // $(".employment-status").prop("checked", false);
        // $(".is-counseling-received").prop("checked", false);

        $.ajax({
                   url: "/learnerManagement/employment/getEmploymentById",
                   type    : "GET",
                   dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
                   data       : {
                       id: this.getAttribute("data-id")
                   },
                   // contentType: "application/json; charset=utf-8",
                   async   : false, // 비동기옵션 off
                   success : function (data) { // 통신이 성공하면 수행할 함수

                       // console.log(data);
                       $("#employmentModal").find("input").prop("readonly", false);

                       $(".employment-status").prop("checked", false);
                       $(".is-counseling-received").prop("checked", false);



                       $(".employment-status").each(function(index, item){
                           if($(item).val() == data.employmentStatus){
                               $(item).prop("checked", true);
                           }
                       });

                       $("#company-name").val(data.companyName);
                       $("#company-phone").val(data.companyPhone);
                       $("#company-address").val(data.companyAddress);

                       $(".is-counseling-received").each(function(index, item){
                           if($(item).val() == data.isCounselingReceived){
                               $(item).prop("checked", true);
                           }
                       });
                       $("#counseling-details").val(data.counselingDetails);


                       $("#employmentModal").find("input").prop("readonly", true);

                       $(".save-employment-btn").hide();
                       $("#modify-employment-btn").show();

                       // $(".save-employment-btn").attr("data-id", "");
                       $(".save-employment-btn").attr("data-id", data.id);


                   },
                   error   : function () {
                   },
                   complete: function () {
                   }
               });


    });

    $(document).on("click", "#modify-employment-btn", function(){

        $("#employmentModal").find("input").prop("readonly", false);
        $("#modify-employment-btn").hide();
        $(".save-employment-btn").show();


    });

    $(document).on("click", ".save-employment-btn", function(){

        let employmentStatus = $(".employment-status:checked").val();
        let isCounselingReceived = $(".is-counseling-received:checked").val();
        // console.log("id : ", this.getAttribute("data-id"));
        // console.log("isCounselingReceived : ", isCounselingReceived);
        // console.log("employmentStatus : ", employmentStatus);


        $.ajax({
                   url: "/learnerManagement/employment/modifyEmployment",
                   type    : "POST",
                   dataType: "text", // 수신받을 데이터의 타입 (MIME TYPE)
                   data       : JSON.stringify({
                       id: this.getAttribute("data-id"),
                       isCounselingReceived: isCounselingReceived,
                       counselingDetails: $("#counseling-details").val(),
                       employmentStatus: employmentStatus,
                       companyName: $("#company-name").val(),
                       companyPhone: $("#company-phone").val(),
                       companyAddress: $("#company-address").val()
                   }),
                   contentType: "application/json; charset=utf-8",
                   async   : false, // 비동기옵션 off
                   success : function (data) { // 통신이 성공하면 수행할 함수

                       if(data == "success"){
                           Swal.fire({
                                         icon: "success",
                                         title: "저장되었습니다!",
                                         text: "성공적으로 저장되었습니다.",
                                         footer: ''
                                     });
                       } else {
                           Swal.fire({
                                         icon: "error",
                                         title: "수정불가능!",
                                         text: "취업관리 DB 테이블을 확인해주세요.",
                                         footer: ''
                                     });
                       }

                       $("#employmentModal").find("input").prop("readonly", true);
                       $(".save-employment-btn").hide();
                       $("#modify-employment-btn").show();

                       getEmploymentList();


                   },
                   error   : function () {
                   },
                   complete: function () {
                   }
               });

    });

});


function getCoursesByInProgress(inProgressType){

    let returnData;

    $.ajax({
               url: "/learnerManagement/employment/getCoursesByInProgress",
               type    : "GET",
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               data       : {
                   inProgressType: inProgressType
               },
               // contentType: "application/json; charset=utf-8",
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   returnData = data;


               },
               error   : function () {
               },
               complete: function () {
               }
           });
    return returnData;
}

function getEmploymentList(){

    decideCategorizeType();

    let secondCategoty;
    if(loginUser.type == "INSTRUCTOR"){
        secondCategoty = $("#courseSelector").val();
    } else {
        secondCategoty = $("#second-category").val();
    }

    $.ajax({
               url: "/learnerManagement/employment/getEmploymentList",
               type    : "GET",
               dataType: "json", // 수신받을 데이터의 타입 (MIME TYPE)
               data       : {
                   pageNo: pageNo,
                   categorizeType: categorizeType,
                   firstCategory: $("#first-category").val(),
                   secondCategory: secondCategoty,
                   withSearch: isWithSearch,
                   keyword: $("#search-input").val()
               },
               // contentType: "application/json; charset=utf-8",
               async   : false, // 비동기옵션 off
               success : function (data) { // 통신이 성공하면 수행할 함수

                   // console.log(data);

                   let output = ``;
                   $(data.voList).each(function(index, item){

                       output += `<tr>
                                    <td class="text-center align-middle">${item.isInProgress ? '진행 중' : '종료'}</td>
                                    <td class="title align-middle">${item.name}</td>
                                    <td class="text-center align-middle">${item.instructorFullName}</td>
                                    <td class="text-center align-middle">${item.learnerFullName}</td>
                                    <td class="text-center align-middle ${item.counselingReceived ? 'text-success' : 'text-danger'}">${item.counselingReceived ? '상담완료' : '미상담'}</td>
                                    <td class="text-center align-middle">`;

                       if(item.employmentStatus == "UNEMPLOYED"){
                           output += `취업준비중`;
                       } else if (item.employmentStatus == "EMPLOYED"){
                           output += `취업`;
                       } else if (item.employmentStatus == "STUDYING"){
                           output += `학업`;
                       } else {
                           output += `취업의사없음`;
                       }


                       output += `  </td>
                                    <td class="text-center align-middle">
                                      <button class="btn btn-primary btn-icon-split btn-sm employment-modal-btn" data-toggle="modal"
                                              data-target="#employmentModal" data-id="${item.id}">
                                        <span class="text">관리</span>
                                      </button>
                                    </td>
                                  </tr>`;

                   });

                   $("#employment-support-list").html(output);
                   makeAndShowPagingArea(data);

               },
               error   : function () {
               },
               complete: function () {
               }
           });

}

function decideCategorizeType() {

    if(loginUser.type == "INSTRUCTOR"){
        categorizeType = 2;
        return;
    }

    let firstCategory  = $("#first-category").val();
    let secondCategory = $("#second-category").val();

    if (firstCategory == -1 && secondCategory == -1) {
        categorizeType = 0;
    } else if (firstCategory != -1 && secondCategory == -1) {
        categorizeType = 1;
    } else {
        categorizeType = 2;
    }

}

function makeAndShowPagingArea(data) {

    let output = ``;

    if (data.prev) {
        output += `<li class="page-item">
                           <span class="page-link" onclick="pushPrevBtn(${data.start})">이전</span>
                         </li>`;
    } else {
        output += `<li class="page-item disabled">
                           <span class="page-link">이전</span>
                         </li>`;
    }

    for (let i = data.start; i <= data.end; i++) {

        if (pageNo == i) {
            output += `<li class="page-item active">
                             <span class="page-link" onclick="pushPageBtn(this)">${i}</span>
                           </li>`;

        } else {
            output += `<li class="page-item">
                             <span class="page-link" onclick="pushPageBtn(this)">${i}</span>
                           </li>`;
        }

    }

    if (data.next) {
        output += `<li class="page-item">
                           <span class="page-link" onclick="pushNextBtn(${data.start})">다음</span>
                         </li>`;
    } else {
        output += `<li class="page-item disabled">
                           <span class="page-link">다음</span>
                         </li>`;
    }

    $("#paging-area").html(output);

}

function pushNextBtn(startNo) {
    pageNo = startNo + 10;
    getEmploymentList();

}

function pushPrevBtn(startNo) {
    pageNo = startNo - 10;
    getEmploymentList();
}

function pushPageBtn(thisBtn) {

    pageNo = $(thisBtn).text();
    getEmploymentList();

}

function showSecondCategory(data){

    let output = `<option value="-1">전체</option>`;
    $(data).each(function(index, item){
        output += `<option value="${item.id}">${item.name}</option>`;
    });

    $("#second-category").html(output);
}

function showCourseSelector(data){
    let output = ``;
    $(data).each(function(index, item){
        output += `<option value="${item.id}">${item.name}</option>`;
    });

    $("#courseSelector").html(output);
}


