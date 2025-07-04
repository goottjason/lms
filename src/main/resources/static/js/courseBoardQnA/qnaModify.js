const $courseSelect = $("#courseSelector");

let boardNo;
let qnaPostData;

$(document).ready(async function () {

  await getUserCourses();
  $courseSelect.prop("disabled", true);
  const courseName = new URLSearchParams(window.location.search).get(
      "searchOptions.courseName");
  $courseSelect.val(courseName);

  const queryString = window.location.search || "";
  $("#prev-page").attr("href", `/courseBoardQnA/list` + queryString);

  boardNo = $("#qna-board-no").val();
  const detailRes = await fetchQnADetail(boardNo);
  console.log(detailRes);

  renderModifyData(detailRes.data.data);
  renderExistingFiles(detailRes.data.data.uploadFiles);

});

//------------------------------------------------------------------------------
// [[수정 페이지 render]]
//------------------------------------------------------------------------------

const $titleInput = $("#qna-title");
const $contentInput = $("#qna-content");
const $secretCheck = $("#secret-check");
const $fileInput = $("#qna-file");

function renderModifyData(data) {

  qnaPostData = {
    title: data.title,
    content: data.content,
    isSecret: data.isSecret
  };

  $titleInput.val(qnaPostData.title);
  $contentInput.text(qnaPostData.content);
  $secretCheck.prop("checked", qnaPostData.isSecret);

}

// 기존 파일 정보와 삭제 ID 추적용
let existingFiles = [];
const removedFileIds = [];

function renderExistingFiles(files) {
  existingFiles = [...files];
  const $preview = $("#existing-preview").empty();

  if (!files.length) {
    return $preview.append(`<div class="text-muted">기존 첨부파일이 없습니다.</div>`);
  }

  files.forEach((file, idx) => {
    const $card = $(`
      <div class="card mb-2 w-100" style="height:80px; max-width:600px; margin:12px;">
        <div class="card-body d-flex align-items-center justify-content-between p-2"></div>
      </div>
    `);
    const $body = $card.find(".card-body");

    // 썸네일 또는 아이콘
    if (/\.(jpe?g|png|gif|bmp)$/i.test(file.originalName)) {
      $body.append(`
        <img src="${file.path}"
             class="img-fluid rounded-start"
             style="width:200px; height:70px; object-fit:contain;" />
      `);
    } else {
      $body.append(`
        <span class="text-truncate" style="max-width:85%; font-size:0.9rem;">
          📄 ${file.originalName}
        </span>
      `);
    }

    // 삭제 버튼
    $("<span style=\"cursor:pointer;\">❌</span>")
    .appendTo($body)
    .on("click", () => removeExistingFile(idx));

    $preview.append($card);
  });
}

// 기존 파일 삭제 처리
function removeExistingFile(index) {
  const [removed] = existingFiles.splice(index, 1);
  removedFileIds.push(removed.id);
  renderExistingFiles(existingFiles);
}

//------------------------------------------------------------------------------
// [[파일 등록]]
//------------------------------------------------------------------------------

// qnaModify.js 맨 위 또는 document.ready 안에 추가
const MAX_FILE_SIZE = 1024 * 1024 * 8;
let newUploadFiles = [];

// 파일 선택 이벤트
$("#qna-file").on("change", function (e) {
  newUploadFiles = [];
  const files = Array.from(e.target.files);

  files.forEach(file => {
    const ext = file.name.split(".").pop().toLowerCase();
    if (file.size > MAX_FILE_SIZE) {
      Swal.fire("파일 크기 초과", "8MB 이하 파일만 업로드 가능합니다.", "error");
    } else if (ext === "exe") {
      Swal.fire("실행파일 불가", "exe 파일은 업로드할 수 없습니다.", "error");
    } else {
      newUploadFiles.push(file);
    }
  });

  updateNewFileInput();
  showNewPreview();
});

// input.files 동기화
function updateNewFileInput() {
  const dt = new DataTransfer();
  newUploadFiles.forEach(f => dt.items.add(f));
  $("#qna-file")[0].files = dt.files;
}

// 새 파일 미리보기
function showNewPreview() {
  const $pv = $("#new-preview").empty();
  if (!newUploadFiles.length) {
    return;
  }

  newUploadFiles.forEach((file, idx) => {
    const $card = $(`
      <div class="card mb-2 w-100" style="height:80px; max-width:600px; margin:12px;">
        <div class="card-body d-flex align-items-center justify-content-between p-2"></div>
      </div>
    `);
    const $body = $card.find(".card-body");

    // 이미지면 썸네일, 아니면 아이콘
    if (file.type.startsWith("image/")) {
      const reader = new FileReader();
      reader.onload = e => {
        $body.prepend(`<img src="${e.target.result}"
                          class="img-fluid rounded-start"
                          style="width:200px;height:70px;object-fit:contain;">`);
      };
      reader.readAsDataURL(file);
    } else {
      $body.prepend(`<span class="text-truncate" style="max-width:85%;font-size:0.9rem;">
                        📄 ${file.name}
                     </span>`);
    }

    // 삭제 버튼
    $("<span style=\"cursor:pointer;\">❌</span>")
    .appendTo($body)
    .on("click", () => removeNewFile(idx));

    $pv.append($card);
  });
}

// 새 파일 삭제
function removeNewFile(index) {
  newUploadFiles.splice(index, 1);
  updateNewFileInput();
  showNewPreview();
}

//------------------------------------------------------------------------------
// [[수정 이벤트]]
//------------------------------------------------------------------------------

const $qnaModifyBtn = $("#qna-modify-btn");

$qnaModifyBtn.on("click", async function () {
  const newTitle = $titleInput.val().trim();
  const newContent = $contentInput.val().trim();
  const newSecret = $secretCheck.prop("checked");

  // 변경 사항 체크
  if (
      newTitle === qnaPostData.title &&
      newContent === qnaPostData.content &&
      newSecret === qnaPostData.isSecret &&
      !removedFileIds.length &&
      newUploadFiles.length === 0
  ) {
    return Swal.fire({
      icon: "info",
      title: "변경된 내용이 없습니다.",
      text: "제목·내용·공개여부·파일 중 하나 이상 변경해 주세요.",
      confirmButtonText: "확인"
    });
  }

  const formData = new FormData();
  formData.append("title", newTitle);
  formData.append("content", newContent);
  formData.append("isSecret", newSecret);

  // 삭제된 기존 파일 ID
  removedFileIds.forEach(id => {
    formData.append("removedFileIds", id);
  });

  // 새로 추가된 파일
  newUploadFiles.forEach(file => {
    formData.append("uploadFiles", file);
  });

  try {
    await axios.put(
        `/api/qna/${boardNo}`,
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
    );

    Swal.fire({
      icon: "success",
      title: "수정 완료",
      confirmButtonText: "확인"
    }).then(() => {
      const qs = window.location.search || "";
      window.location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
    });
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

const $prevPage = $("#prev-page");

$prevPage.on("click", function () {

  const qs = window.location.search || "";
  window.location.href = `/courseBoardQnA/detail/${boardNo}${qs}`;
});

// 사용자(수강생/강사) 필터 호출
function getUserCourses() {

  return fetchUserCourses()
  .then((res) => {
    renderUserCourseOptions("#courseSelector", res.data.data);
  })
  .catch((err) => console.log(err));
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

/**
 * URL 파라미터 유틸
 */
const UrlUtils = {
  /**
   * 쿼리스트링에서 값 꺼내기
   * @param {string} name 키 이름
   * @returns {string|null}
   */
  getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
  },

  /**
   * 경로 템플릿에 맞춰 세그먼트 파싱하기
   * @param {string} template e.g. '/test/testDetail/:testId/:foo'
   * @returns {Object} { testId: '8029', foo: '...' }
   */
  getPathParams(template) {
    const pathSegs = window.location.pathname.split("/")
                           .filter(Boolean);
    const templateSegs = template.split("/").filter(Boolean);
    const params = {};
    templateSegs.forEach((seg, i) => {
      if (seg.startsWith(":")) {
        const key = seg.slice(1);
        params[key] = pathSegs[i] || null;
      }
    });
    return params;
  },

  /**
   * 경로 세그먼트 인덱스로 가져오기
   * @param {number} index 0부터 시작 (첫 번째 유효 세그먼트)
   * @returns {string|null}
   */
  getPathSegment(index) {
    const segs = window.location.pathname.split("/").filter(Boolean);
    return segs[index] || null;
  }
};