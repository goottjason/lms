const $courseSelect = $("#courseSelector");

const $titleInput = $("#qna-title");
const $contentInput = $("#qna-content");
const $secretCheck = $("#secret-check");
const $fileInput = $("#qna-file");

$(document).ready(async function () {

  await getUserCourses();
  $courseSelect.prop("disabled", true);
  const courseName = new URLSearchParams(window.location.search).get(
      "searchOptions.courseName");
  $courseSelect.val(courseName);

  const queryString = window.location.search || "";
  $("#prev-page").attr("href", `/courseBoardQnA/list` + queryString);

});

// 사용자(수강생/강사) 필터 호출
function getUserCourses() {

  return fetchUserCourses()
  .then((res) => {
    renderUserCourseOptions("#courseSelector", res.data.data);
  })
  .catch((err) => {
    // console.log(err);
  });
}

// 사용자(수강생/강사) 필터 생성
function renderUserCourseOptions(selector, data) {
  let $adminCourseSelect = $(selector);
  $adminCourseSelect.empty();

  // 진행중인 강좌 요소 찾기
  const defaultEl = data.find(el => el.inProgress) || data[0];
  const selectedCourseName = defaultEl ? defaultEl.courseName : "";

  $.each(data, function (index, el) {

    $adminCourseSelect.append(
        `<option value="${el.courseName}" data-is-in-progress="${el.inProgress}">${el.courseName}</option>`);
  });

  // if (selectedCourse === null || selectedCourse === undefined) {
  //     selectedCourse = selectedCourseName;
  //     $adminCourseSelect.val(selectedCourseName);
  // }

  // $adminCourseSelect.val(selectedCourse);
}

//------------------------------------------------------------------------------
// [[파일 관련]]
//------------------------------------------------------------------------------

const MAX_FILE_SIZE = 1024 * 1024 * 8;
let uploadFiles = [];

$fileInput.on("change", function (e) {

  uploadFiles = [];

  let newFiles = Array.from(e.target.files);

  newFiles.forEach(function (file) {
    const ext = file.name.split(".").pop().toLowerCase();

    if (file.size > MAX_FILE_SIZE) {
      Swal.fire({
        title: "파일 사이즈를 초과하였습니다.",
        icon: "error",
        confirmButtonText: "예"
      });
    } else if (ext.trim() === "exe") {
      Swal.fire({
        title: "실행 파일은 업로드할 수 없습니다.",
        icon: "error",
        confirmButtonText: "예"
      });
    } else {

      uploadFiles.push(file);
    }
  });
  // console.log(uploadFiles); // 선택된 파일

  updateFileInput();
  showPreview();
});

function updateFileInput() {
  // console.log(uploadFiles); // 선택된 파일
  const dataTransfer = new DataTransfer();
  uploadFiles.forEach(function (file) {
    dataTransfer.items.add(file);
  });

  $("#qna-file")[0].files = dataTransfer.files;
  // console.log($("#qna-file")[0].files);
  // console.log(uploadFiles); // 선택된 파일
}

function showPreview() {
  $("#preview").empty();

  uploadFiles.forEach(function (file, index) {
    // wrapper/container
    const $card = $(`
      <div class="card mb-2 w-100" style="height:80px; max-width:600px; margin:12px;">
        <div class="card-body d-flex align-items-center justify-content-between p-2"></div>
      </div>
    `);
    const $body = $card.find(".card-body");

    // 삭제 버튼
    const $removeBtn = $(`<span style="cursor:pointer;">❌</span>`)
    .on("click", () => removeFile(index));
    $body.append($removeBtn);

    if (file.type.startsWith("image/")) {
      const reader = new FileReader();
      reader.onload = function (e) {
        const $img = $(`<img src="${e.target.result}" 
                          class="img-fluid rounded-start" 
                          style="width:200px; height:70px; object-fit:contain;" />`);
        $body.prepend($img);
        $("#preview").append($card);
      };
      reader.readAsDataURL(file);
    } else {
      const $icon = $(`<span class="text-truncate" 
                        style="max-width:85%; font-size:0.9rem;">
                        📄 ${file.name}
                      </span>`);
      $body.prepend($icon);
      $("#preview").append($card);
    }
  });
}

function removeFile(index) {

  uploadFiles.splice(index, 1);

  updateFileInput();
  showPreview();
}

//------------------------------------------------------------------------------
// [[글 등록]]
//------------------------------------------------------------------------------

$("#qna-register-btn").on("click", async function (e) {
  e.preventDefault();

  // FormData 에 form 필드들 append
  const formData = new FormData();
  formData.append("title", $titleInput.val().trim());
  formData.append("content", $contentInput.val().trim());
  formData.append("isSecret", $secretCheck.prop("checked"));
  // courseName 은 쿼리스트링에서 받았던 상태 유지용
  const courseName = new URLSearchParams(window.location.search)
  .get("searchOptions.courseName") || "";
  formData.append("courseName", courseName);

  // 파일 여러 개 append
  const files = $fileInput[0].files;
  for (let i = 0; i < files.length; i++) {
    formData.append("uploadFiles", files[i]);
  }

  try {
    // multipart/form-data 로 POST
    const res = await axios.post(
        "/api/qna",
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
    );

    // console.log(res);
    // 성공 시 응답 처리
    if (res.data.data) {
      Swal.fire("완료", "Q&A가 등록되었습니다.", "success")
          .then(() => {
            // 등록 후 목록으로, 상태 유지
            const qs = window.location.search || "";
            window.location.href = "/courseBoardQnA/list" + qs;
          });
    }
  } catch (err) {
    console.error(err);
    showValidationErrors(err.response.data.data);
  }

});

function showValidationErrors(errorData) {
  // 초기화
  $("#qna-title").removeClass("is-invalid");
  $("#qna-content").removeClass("is-invalid");
  $("#title-error").text("");
  $("#content-error").text("");

  if (errorData.title) {
    $("#qna-title").addClass("is-invalid");
    $("#title-error").text(errorData.title);
  }

  if (errorData.content) {
    $("#qna-content").addClass("is-invalid");
    $("#content-error").text(errorData.content);
  }
}

