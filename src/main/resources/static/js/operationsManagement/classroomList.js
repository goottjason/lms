const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();
let classrooms = null;
let baseConfig = {
    loginUserId: loginUserId,
    loginUserType: loginUserType
}
let classroomPageConfig = {
    pageNo: 1,
    pageSize: 10,
    type: null,
    keyword: null,
    orderBy: "id",
    orderDirection: "ASC"
}
let staffConfig = {
    pageNo: null,
    pageSize: null,
    type: "fullname",
    keyword: null,
    orderBy: "fullname",
    orderDirection: "ASC",
    // 필터링
    staffId: null,
    staffType: "ADMINISTRATOR",
    staffPosition: null,
    onlyLeaver: false,
}
let staffs = null;
$(document).ready(() => {
    initState();
    $(document).on('click', '.page-link', handlePageBtnClick);

    // 저장(insert): 생성요청 후 display (강의실명 동일한지 체크, 정, 부 동일한지 체크)

    $(document).on('click', '#add-button', handleAddBtnClick);

    // 수정: 강의실명 readonly 풀고, 관리자 기존의 선택된 사람 넣어둔 채로 선택 가능하게 한 후 수정요청 후 display
    $(document).on('click', '.edit-button', handleEditBtnClick);
    // 저장(update)
    $(document).on('click', '.save-button', handleSaveBtnClick);

    // 삭제: 삭제 모달 띄우고 삭제요청 후 display
    $(document).on('click', '.remove-button', handleRemoveBtnClick);

    $(document).on('click', '.cancel-button', handleCancelBtnClick);
});

function handleCancelBtnClick() {
    // displayTableView();
    let classroomId = $(this).data('id');
    displayTrView(classroomId);
}
async function displayTrView(classroomId) {

    let classroomsWithPagination = await apiGetRequestAboutClassroom(
        '/api/management/classrooms');
    let classrooms = Array.isArray(classroomsWithPagination.classroomRepsDTOS) ?
                     classroomsWithPagination.classroomRepsDTOS : [classroomsWithPagination.classroomRepsDTOS];
    displayOnlyTr(classrooms, classroomId);
}
function displayOnlyTr(classrooms, classroomId) {
    $(`tr[data-id=${classroomId}]`).empty();
    classrooms.forEach(function(classroom) {
       if (classroom.id == classroomId) {
           let trHtml = `

                  <td class="text-center align-middle" id="name-td-${classroom.id}">
                      <input type="text" class="form-control" 
                      value="${classroom.name}" id="name-input-${classroom.id}" readonly></td>
                  <td class="align-middle">
                      ${classroom.isActive? classroom.courseName : '미배정'}</td>
                  <td class="text-center align-middle ${classroom.isActive? 'text-danger':''}">
                    ${classroom.isActive? '사용중':'미사용'}</td>
                  <td class="text-center align-middle" id="pri-td-${classroom.id}">
                      <input type="text" class="form-control" value="${classroom.priUserFullname}" id="pri-input-${classroom.id}" data-id="${classroom.priUserId}" readonly>
                  </td>
                  <td class="text-center align-middle" id="sec-td-${classroom.id}">
                      <input type="text" class="form-control" value="${classroom.secUserFullname}" id="sec-input-${classroom.id}" data-id="${classroom.secUserId}" readonly>
                  </td>
                  <td class="text-center align-middle">
                      <button class="btn btn-info btn-icon-split btn-sm edit-button" data-id="${classroom.id}">
                        <span class="icon text-white-50">
                          <i class="fa fa-wrench"></i>
                        </span>
                        <span class="text">수정</span>
                      </button>
                      <button class="btn btn-primary btn-icon-split btn-sm save-button" style="display:none" data-id="${classroom.id}">
                      <span class="icon text-white-50">
                          <i class="fa fa-upload"></i>
                        </span>
                        <span class="text">저장</span>
                      </button>
                      ${classroom.courseName == '' ? `
                        <button class="btn btn-danger btn-icon-split btn-sm remove-button" data-id="${classroom.id}">
                        <span class="icon text-white-50">
                          <i class="fa fa-wrench"></i>
                        </span>
                          <span class="text">삭제</span>
                        </button>
                        ` : ''}
                      <button class="btn btn-warning btn-icon-split btn-sm cancel-button" style="display:none" data-id="${classroom.id}">
                      <span class="icon text-white-50">
                          <i class="fa fa-wrench"></i>
                        </span>
                        <span class="text">취소</span>
                      </button>
                  </td>

            `;
           $(`tr[data-id=${classroomId}]`).html(trHtml);
       }
    });

}

async function handleAddBtnClick() {
    let newClassroomName = $("#classroom-input").val();
    let newPriUserId = $('#primary-select').val();
    let newSecUserId = $('#secondary-select').val();
    if (newPriUserId == newSecUserId) {
        Swal.fire({
                      icon: "error",
                      title: "저장불가능!",
                      text: "관리자(정)과 관리자(부)가 동일인입니다.",
                      footer: ''
                  });
        return;
    }
    let result = await apiPostRequestAboutClassroom(
        '/api/management/classroom', {
            name: newClassroomName,
            primaryAdminId: newPriUserId,
            secondaryAdminId: newSecUserId
        }
    );
    if(result == true){
        displayTableView();
    } else {
    }
}

function handleEditBtnClick() {
    let classroomId = $(this).data('id');
    let orgPriUserId = $('#pri-input-'+classroomId).data('id');
    let orgSecUserId = $('#sec-input-'+classroomId).data('id');
    // 강의실명 readonly 해제
    $("#name-input-"+classroomId).removeAttr('readonly');
    // 정, 부 셀렉트박스
    $("#pri-td-"+classroomId).html(`<select id="pri-select-${classroomId}" class="form-control"></select>`);
    $("#sec-td-"+classroomId).html(`<select id="sec-select-${classroomId}" class="form-control"></select>`);
    updateModSelectBox($("#pri-select-"+classroomId), staffs, orgPriUserId);
    updateModSelectBox($("#sec-select-"+classroomId), staffs, orgSecUserId);
    // 수정 -> 저장버튼으로 변경
    $(`.edit-button[data-id=${classroomId}]`).hide();
    $(`.save-button[data-id=${classroomId}]`).show();
    // 삭제 -> 취소버튼으로 변경
    $(`.remove-button[data-id=${classroomId}]`).hide();
    $(`.cancel-button[data-id=${classroomId}]`).show();


}

function handleRemoveBtnClick() {
    let classroomId = $(this).data('id');

    Swal.fire({
                  title             : "정말 삭제하시겠습니까?",
                  text              : "삭제하시면 되돌릴 수 없습니다.",
                  icon              : "warning",
                  showCancelButton  : true,
                  confirmButtonColor: "#3085d6",
                  cancelButtonColor : "#d33",
                  confirmButtonText : "삭제"
              }).then((result) => {
        if (result.isConfirmed) {
            removeClassroom(classroomId);
        }
    });
}

async function removeClassroom(classroomId) {
    let result = await apiDeleteRequestAboutClassroom(
        '/api/management/classroom', {
            id: classroomId
        }
    );
    if(result) {
        displayTableView();
    } else {
        return;
    }
}
async function handleSaveBtnClick() {
    let classroomId = $(this).data('id');
    let newClassroomName = $("#name-input-"+classroomId).val();
    let newPriUserId = $('#pri-select-'+classroomId).val();
    let newSecUserId = $('#sec-select-'+classroomId).val();
    if (newPriUserId == newSecUserId) {
        Swal.fire({
                      icon: "error",
                      title: "수정불가능!",
                      text: "관리자(정)과 관리자(부)가 동일인입니다.",
                      footer: ''
                  });
        return;
    }
    let result = await apiPatchRequestAboutClassroom(
        '/api/management/classroom', {
            id: classroomId,
            name: newClassroomName,
            primaryAdminId: newPriUserId,
            secondaryAdminId: newSecUserId
        }
    );
    if(result == true){
        displayTrView(classroomId);
        /*displayTableView();*/
    } else {
        return;
    }
}

async function apiPatchRequestAboutClassroom(endpoint, additionalParams) {
    try {
        const response = await axios.patch(endpoint, {
            baseReqDTO: baseConfig,
            classroomReqDTO: additionalParams });
        Swal.fire({
                      icon: "success",
                      title: "수정완료!",
                      text: "성공적으로 수정되었습니다.",
                      footer: ''
                  });
        return true;
    } catch (error) {
        Swal.fire({
                      icon: "error",
                      title: "수정실패!",
                      text: "강의실명은 중복될 수 없습니다.",
                      footer: ''
                  });
        console.error(`${endpoint} 요청 오류:`, error);
        return false;
    }
}

async function apiPostRequestAboutClassroom(endpoint, additionalParams) {
    try {
        const response = await axios.post(endpoint, {
            baseReqDTO: baseConfig,
            classroomReqDTO: additionalParams });
        console.log(response);
        Swal.fire({
                      icon: "success",
                      title: "추가완료!",
                      text: "성공적으로 추가되었습니다.",
                      footer: ''
                  });
        return true;
    } catch (error) {
        Swal.fire({
                      icon: "error",
                      title: "추가실패!",
                      text: "강의실명은 중복될 수 없습니다.",
                      footer: ''
                  });
        console.error(`${endpoint} 요청 오류:`, error);
        return false;
    }
}

async function apiDeleteRequestAboutClassroom(endpoint, additionalParams) {
    try {
        const response = await axios.delete(endpoint, {
            data: {
                baseReqDTO: baseConfig,
                classroomReqDTO: additionalParams }
            });

        Swal.fire({
                      icon: "success",
                      title: "삭제완료!",
                      text: "성공적으로 삭제되었습니다.",
                      footer: ''
                  });
        return true;
    } catch (error) {
        Swal.fire({
              icon: "error",
              title: "삭제불가능!",
              text: "알 수 없는 오류가 발생했습니다. 다시 시도해주세요.",
              footer: ''
          });
        console.error(`${endpoint} 요청 오류:`, error);
        return false;
    }
}

async function initState() {
    // 관리자(정), 관리자(부) 셀렉트 박스 읽어오기
    let staffsWithPaging = await apiGetRequestParams(
        '/api/operationsmanagement/staffs',
        {...baseConfig, ...staffConfig});

    staffs = staffsWithPaging?.records || [];
    if (!Array.isArray(staffs)) {
        staffs = [];
    }

    updateSelectBox('#primary-select', staffs);
    updateSelectBox('#secondary-select', staffs);

    displayTableView();
}
async function apiGetRequestParams(endpoint, params) {
    try {
        const response = await axios.get(endpoint, {params: params});
        return response.data.data;
    } catch (error) {
        return [];
    }
}
async function displayTableView() {

    let classroomsWithPagination = await apiGetRequestAboutClassroom(
        '/api/management/classrooms');
    let classrooms = Array.isArray(classroomsWithPagination.classroomRepsDTOS) ?
                   classroomsWithPagination.classroomRepsDTOS : [classroomsWithPagination.classroomRepsDTOS];
    displayClassrooms(classrooms);
    displayPagination(classroomsWithPagination);
}
function displayPagination(data) {
    // 기록이 없을 때, 페이지네이션도 표시되지 않음
    if(data.totalRecords == 0) {
        $("#classroom-pagination").html(""); // 바꿔야 할 부분
        return;
    }

    let output = `<ul class="pagination justify-content-center" style="margin:20px 0">`;

    // 이전 버튼
    let prevBlockPage = data.blockStartPage > 1 ? data.blockStartPage - 1 : 1;
    output += `
    <li class="page-item ${data.blockStartPage == 1? 'disabled': ''}">
      <a class="page-link page-btn" href="#" data-page="${prevBlockPage}">이전</a>
    </li>`;

    // 페이지 번호 버튼
    for (let i = data.blockStartPage; i <= data.blockEndPage; i++) {
        let active = data.pageNo == i ? "active" : "";
        output += `
      <li class="page-item ${active}">
        <a class="page-link page-btn" href="#" data-page="${i}">${i}</a>
      </li>`;
    }

    // 다음 버튼
    let nextBlockPage = data.blockEndPage < data.lastPage ? data.blockEndPage + 1 : data.lastPage;
    output += `
    <li class="page-item ${data.blockEndPage == data.lastPage ? 'disabled': ''}">
      <a class="page-link page-btn" href="#" data-page="${nextBlockPage}">다음</a>
    </li></ul>`;

    $("#classroom-pagination").html(output);
}

function displayClassrooms(classrooms) {
    if(classrooms.length == 0) {
        $("#table-body").html(
            "<tr class='text-center'><td colspan='6'>데이터가 없습니다.</td></tr>");
        return;
    }
    $('#table-body').empty();
    classrooms.forEach(function(classroom) {
        let rowHtml = `
        <tr data-id="${classroom.id}">
          <td class="text-center align-middle" id="name-td-${classroom.id}">
              <input type="text" class="form-control" 
              value="${classroom.name}" id="name-input-${classroom.id}" readonly></td>
          <td class="align-middle">
              ${classroom.isActive? classroom.courseName : '미배정'}</td>
          <td class="text-center align-middle ${classroom.isActive? 'text-danger':''}">
            ${classroom.isActive? '사용중':'미사용'}</td>
          <td class="text-center align-middle" id="pri-td-${classroom.id}">
              <input type="text" class="form-control" value="${classroom.priUserFullname}" id="pri-input-${classroom.id}" data-id="${classroom.priUserId}" readonly>
          </td>
          <td class="text-center align-middle" id="sec-td-${classroom.id}">
              <input type="text" class="form-control" value="${classroom.secUserFullname}" id="sec-input-${classroom.id}" data-id="${classroom.secUserId}" readonly>
          </td>
          <td class="text-center align-middle">
              <button class="btn btn-info btn-icon-split btn-sm edit-button" data-id="${classroom.id}">
                <span class="icon text-white-50">
                      <i class="fas fa-wrench"></i>
                  </span>
                <span class="text">수정</span>
              </button>
              <button class="btn btn-primary btn-icon-split btn-sm save-button" style="display:none" data-id="${classroom.id}">
              <span class="icon text-white-50">
              <i class="fas fa-upload"></i>
                </span>
                <span class="text">저장</span>
              </button>
              ${classroom.courseName == '' ? `
                <button class="btn btn-danger btn-icon-split btn-sm remove-button" data-id="${classroom.id}">
                    <span class="icon text-white-50">
                      <i class="fas fa-wrench"></i>
                    </span>
                  <span class="text">삭제</span>
                </button>
                ` : ''}
              <button class="btn btn-warning btn-icon-split btn-sm cancel-button" style="display:none" data-id="${classroom.id}">
              <span class="icon text-white-50">
              <i class="fas fa-upload"></i>
          </span>
                <span class="text">취소</span>
              </button>
          </td>
        </tr>
      `;
        $('#table-body').append(rowHtml);
    });
}
function updateSelectBox(selector, data) {
    const $select = $(selector).empty();
    data.forEach(staff => {
        $select.append($('<option>').val(staff.userId).text(staff.fullname));
    });
}
function updateModSelectBox(selector, data, userId) {
    const $select = $(selector).empty();
    data.forEach(staff => {
        $select.append($('<option>').val(staff.userId).text(staff.fullname));
    });
    $select.find(`option[value="${userId}"]`).prop('selected', true);
}


async function apiGetRequestAboutClassroom(endpoint, additionalParams = {}) {
    try {
        const response = await axios.get(endpoint, {
            params: { ...baseConfig, ...classroomPageConfig }
        });
        return response.data.data;
    } catch (error) {
        console.error(`${endpoint} 요청 오류:`, error);
        return [];
    }
}

function handlePageBtnClick() {
    classroomPageConfig.pageNo = $(this).data('page');
    displayTableView();
}
