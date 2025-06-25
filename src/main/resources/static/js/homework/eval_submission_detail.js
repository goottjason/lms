// 평가 수정 및 삭제

let modifyFileList = []; //eval 수정 시 들어갈 파일들
let deleteModifyFileList = []; //eval 수정 시 들어갈 파일 id(삭제 예정)

//평가 수정 란 보이게 하기
function showEvalModify() {

  let output = `<form id="evalModifyForm" method="post" enctype="multipart/form-data" action="/homework/modifyEvalPost">
  <div class="card shadow mb-4">
    <div class="card-header py-2">
      <div class="row align-items-center">
        <div class="col-1">
          <h6 class="m-0 font-weight-bold text-primary">평가</h6>
        </div>
        <div class="col-3 ml-auto d-flex justify-content-end align-items-center">
        <label for="isPass"></label>
          <select class="form-control" id = "isPass" name="isPass">
            <option value="1">PASS</option>
            <option value="0">FAIL</option>
          </select>
        </div>
      </div>
    </div>

    <div class="card-body py-3">
      <div class="form-group">
      <label for="content"></label>
      <span id="evalContentModify" style="color: red;"></span>
        <textarea id="content" class="form-control" placeholder="평가 내용을 작성하세요."
                  style="height: 200px;" name="content">${evalDTO.content}</textarea>
      </div>
      <div class="row mb-4">
        <div class="col-md-12">
          <div class="mb-2">첨부파일</div>
          <div class="custom-file">
            <input type="file" class="custom-file-input" id="evalModifyFile" multiple>
            <label class="custom-file-label" for="evalFile">파일을 선택하세요.</label>
          </div>
        </div>
      </div>
    </div>`;

  output += `<div class="card-footer">
      <p class="mt-2">기존 파일</p>`

  // 조회된 파일 리스트가 있을 경우
  for (let file of evalFiles) {

    if (file.isImage === false) {
      output += `
    <div class="card mb-2 w-100" style="height: 60px;">
      <div class="card-body d-flex align-items-center justify-content-between p-2">
        <span class="text-truncate" style="max-width: 85%; font-size: 0.9rem;">📄 ${file.originalName}</span>
        <span style="cursor:pointer;" onclick="removeModifyBefore(this)" data-id="${file.id}">❌</span>
      </div>
    </div>`;
    } else {
      output += `
    <div class="card mb-2 w-100" style="height: 80px;">
      <div class="card-body d-flex align-items-center justify-content-between p-2">
        <img src="${file.path}" style="width: 100px; height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #ccc;" />
        <span style="cursor:pointer;" onclick="removeModifyBefore(this)" data-id="${file.id}">❌</span>
      </div>
    </div>`;
    }

  }

  //조회된 파일 리스트 끝

  //preview 단
  output += `<div class="card-footer">
      <p class="mt-2">추가할 파일</p>
      <div id="modifyPreview"></div>
      </div>
      
    <div class="card-footer d-flex justify-content-end">
      <button class="btn btn-info btn-icon-split mr-2" id = "evalModifyPost" type="button">
        <span class="icon text-white-50"><i class="fa fa-wrench"></i></span>
        <span class="text px-3">평가 수정 등록</span>
      </button>
      <button class="btn btn-secondary btn-icon-split mr-2">
        <span class="icon text-white-50"><i class="fa fa-times"></i></span>
        <span class="text px-3">목록</span>
      </button>
    </div>
  </div>
  </form>`;

  $("#eval").html(output);
  $("#modifyEval").remove();
  $("#deleteEval").remove();
  $("#toList").remove();

}

//평가 수정 란의 기존 파일 삭제
function removeModifyBefore(x) {
  $(x).closest(".card.mb-2").remove();
  console.log($(x).data("id"));
  deleteModifyFileList.push($(x).data("id"));
  console.log(deleteModifyFileList);
}

// 파일 추가 시 파일 리스트에 넣기
function pushFileListModify(files) {

  for (let file of files) {
    if (file.size > MAX_FILE_SIZE) {
      alert("파일 사이즈는 8MB를 초과할 수 없습니다.")
    } else {
      modifyFileList.push(file);
      console.log(modifyFileList);
    }
  }
}

// 파일 추가 시 프리뷰
function previewFileModify(modifyFileList) {
  $("#modifyPreview").empty();

  console.log(modifyFileList);

  modifyFileList.forEach(function (file, index) {
    if (!file.type.match('image/*')) {
      //이미지가 아닐 때
      let fileReader = new FileReader();
      fileReader.readAsDataURL(file);
      fileReader.onload = function (e) {
        // console.log(e.target.result);
        let output = ``;
        output += `<div class="card mb-2 w-100" style="height: 60px;">
      <div class="card-body d-flex align-items-center justify-content-between p-2">
        <span class="text-truncate" style="max-width: 85%; font-size: 0.9rem;">📄${file.name}</span>
        <span style="cursor:pointer;" onclick="removeModifyInsert(${index});">❌</span>
      </div>
    </div>`;
        $("#modifyPreview").append(output);
      }
    } else {
      //이미지
      let fileReader = new FileReader();
      fileReader.readAsDataURL(file);
      fileReader.onload = function (e) {
        // console.log(e.target.result);
        let output = ``;
        output += `<div class="card mb-2 w-100" style="height: 80px;">
      <div class="card-body d-flex align-items-center justify-content-between p-2">
        <img src="${e.target.result}" style="width: 100px; height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #ccc;" />
        <span style="cursor:pointer;" onclick="removeModifyInsert(${index});">❌</span>
      </div>
     </div>`;
        $("#modifyPreview").append(output);
      }
    }
  })
}

// insert한 파일 다시 삭제(modifyFileList)
function removeModifyInsert(index) {
  modifyFileList.splice(index, 1);
  previewFileModify(modifyFileList);
}

// form 보내기
function modifyEvalPost() {

  let form = document.getElementById("evalModifyForm");

  let formData = new FormData(form);

  formData.append("id", evalId);

  formData.append("instructorId", instructorId);

  modifyFileList.forEach(function (file, index) {
    formData.append("modifyFileList", file);
  });

  deleteModifyFileList.forEach(function (id) {
    formData.append("deleteModifyFileList", id);
  });

  axios.post("/homework/modifyEvalPost", formData)
  .then(function (response) {
    console.log(response)
    console.log("submissionId",submissionId);
    location.href = "/homework/submissionDetail?submissionId=" + submissionId;
  }).catch(function (error) {
    console.log("error", error);
    let errorCode = error.response.data.code;
    let errorMsg = error.response.data.message;
    if (errorCode === 400) {
      errorMap = error.response.data.data;
      if (errorMap.content) {
        $("#evalContentModify").html(errorMap.content);
      } else {
        $("#evalContentModify").html("");
      }
    } else if (errorCode === 401 || errorCode === 404) {
      alert(errorMsg); //모든 에러메세지가 나타남.
      savePage = localStorage.getItem("savePage");
      if (savePage && savePage !== "undefined" && savePage.trim() !== "") {
        // console.log("savePage",savePage);
        location.href = savePage;
      } else {
        location.href = "/homework/homeworkList";
      }
    } else if(errorCode === 500){
      alert(errorMsg);
      location.href = "homework/submissionDetail?submissionId=" + submissionId;
    }

  })
}

// eval delete 요청
function deleteEvalPost() {

  axios.delete("/homework/deleteEval", {
      data:evalDTO
  })
  .then(function (response) {
    console.log(response);
    location.href = "/homework/submissionDetail?submissionId=" + submissionId;
  }).catch(function (error) {
    console.log("error", error);
    let errorMsg = error.response.data.message;
    let errorCode = error.response.data.code;
    alert(errorMsg);
    if(errorCode === 401){
      location.href = "/";
    }

  })

}

$(function () {
  // console.log("eval_submission_detail_test:", "js 전송 성공");

  console.log("eval",evalDTO);
  console.log("id",evalId);
  console.log("instructorId",instructorId);

  // 수정 버튼 클릭하면 수정 폼 나오기
  $("#modifyEval").click(function () {
    showEvalModify();
  });

  $(document).on("change", "#evalModifyFile", function (e) {
    e.preventDefault();
    console.log(e.target.files);
    let files = e.target.files;
    pushFileListModify(files);
    previewFileModify(modifyFileList);

  });

  // 폼 데이터 보내기
  $(document).on("click","#evalModifyPost",function (e) {
    e.preventDefault();

    console.log("modifyFileList", modifyFileList);
    modifyEvalPost();
  });

  // eval 삭제 버튼 클릭 시 창 띄우기
  $(document).on("click", "#deleteEval", function () {
    // alert("!"); //문제x
    // showDeleteEval();
    $('.toast').toast('show');
  });

  //eval 정말 삭제
  $("#toastDeleteBtn").on("click",function(e){
    // alert("!"); //작동 ok
    e.preventDefault();
    deleteEvalPost();

  });





});

