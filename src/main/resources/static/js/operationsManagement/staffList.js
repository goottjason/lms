const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();
let baseConfig = {
    loginUserId: loginUserId,
    loginUserType: loginUserType
}
let staffPageConfig = {
    pageNo: 1,
    pageSize: 8,
    type: null,
    keyword: null,
    orderBy: "fullname",
    orderDirection: "ASC",
    staffId: null,
    staffType: null,
    staffPosition: null,
    includeLeaveDate: null,
}


$(document).ready(function() {



    getListState();

    // 체크박스 상태 유지
    /*if (staffPageConfig.includeLeaveDate != null) {
    }*/
    $('#include-leaver').attr('checked', staffPageConfig.includeLeaveDate);

    // 타입별 셀렉트박스 변경 이벤트
    $('#type-select').on('change', handleTypeSelectChange);
    // 포지션별 셀렉트박스 변경 이벤트
    $('#position-select').on('change', handlePositionSelectChange);
    // 페이지 로드 시 타입별 셀렉트박스 변경 이벤트 실행
    $('#type-select').trigger('change');
    // 퇴사자포함 체크박스 변경 이벤트
    $('#include-leaver').on('change', handleIncludeLeaverChange);
    // 검색 버튼 클릭 또는 엔터 이벤트
    $('#search-button').on('click', handleSearchChange);
    $(document).on('keydown', '#search-input', function(e) {
        if (e.key == "Enter") {
            e.preventDefault();
            handleSearchChange();
        }
    });
    $(document).on('click', '.page-link', handlePageBtnClick);

});

function handlePageBtnClick() {
    staffPageConfig.pageNo = $(this).data('page');
    saveListState();
    fetchAndDisplayView();
}

function handleTypeSelectChange() {
    console.log("typeSelectChange");
    var type = $(this).val();
    var $positionSelect = $('#position-select');
    var $positionOptions = $positionSelect.find('option');

    $positionOptions.show();

    if (type === 'ADMINISTRATOR') {
        $positionOptions.filter('[value="FULLTIME_INSTRUCTOR"]').hide();
        $positionSelect.val('');
    } else if (type === 'INSTRUCTOR') {
        $positionOptions.filter('[value="GENERAL_MANAGER"], [value="COURSE_HEAD"]').hide();
        $positionSelect.val('');
    } else if (type === null) {
        $positionSelect.val('');
    }
    var position = $positionSelect.val();
    // type (전체, 관리자, 강사)에 따라 교직원 전체, 관리자 전체, 강사 전체로 필터링
    staffPageConfig.staffType = type ? type : null;
    staffPageConfig.staffPosition = position ? position : null;
    saveListState();
    fetchAndDisplayView();
}
function handlePositionSelectChange() {
    var position = $(this).val();

    var type = $('#type-select').val();

    /* position (전체, 총괄관리자, 학과장, 전임강사)에 따라
    교직원 전체, 총괄관리자 전체, 학과장 전체, 전임강사 전체로 필터링 */
    staffPageConfig.staffType = type ? type : null;
    staffPageConfig.staffPosition = position ? position : null;
    console.log(staffPageConfig);
    saveListState();
    fetchAndDisplayView();
}
function handleIncludeLeaverChange() {
    // 체크하면 true, 체크 해제하면 null
    staffPageConfig.includeLeaveDate = $(this).is(':checked') ? true : null;
    console.log(staffPageConfig);
    saveListState();
    fetchAndDisplayView();
}
function handleSearchChange() {
    staffPageConfig.pageNo = 1;
    staffPageConfig.pageSize = 12;
    staffPageConfig.staffId = null;
    staffPageConfig.staffType = null;
    staffPageConfig.staffPosition = null;
    staffPageConfig.keyword = $("#search-input").val() ? $("#search-input").val() : null;
    console.log(staffPageConfig);
    saveListState();
    fetchAndDisplayView();
}
async function fetchAndDisplayView() {
    let staffsWithPagination = await apiGetRequestAboutStaff(
        '/api/management/staffs');
    let staffs = staffsWithPagination?.staffRepsDTOS || [];
    if (!Array.isArray(staffs)) staffs = [];
    console.log(staffs);
    displayCards(staffs);
    displayPagination(staffsWithPagination);
}
async function apiGetRequestAboutStaff(endpoint, additionalParams = {}) {
    try {
        const response = await axios.get(endpoint, {
            params: { ...baseConfig, ...staffPageConfig }
        });
        console.log(response.data);
        return response.data.data;
    } catch (error) {
        console.error(`${endpoint} 요청 오류:`, error);
        return [];
    }
}
function displayCards(staffs) {
    $('#card-list').empty();
    if(staffs.length == 0) {
        $('#card-list').removeClass('row-cols-md-4');
        $("#card-list").html(
            "<p class='text-center w-100 pt-3'>교직원이 없습니다.</p>");
        return;
    }
    $('#card-list').addClass('row-cols-md-4');
    staffs.forEach(function(staff) {
        /*let assignmentHistoryHtml = '';
        if (Array.isArray(staff.assignmentHistoryList)) {
            staff.assignmentHistoryList.forEach(function(assignment) {
                assignmentHistoryHtml += `<p class="card-text mb-1">${assignment.courseName}</p>`;
            });
        } else {
            assignmentHistoryHtml += `<p class="card-text">-</p>`;
        }*/
        let positionText = '';
        if (staff.position == 'GENERAL_MANAGER') {
            positionText = '총괄실장';
        } else if (staff.position == 'COURSE_HEAD') {
            positionText = '학과장';
        } else if (staff.position == 'FULLTIME_INSTRUCTOR') {
            positionText = '강사';
        }
        let leaveDateHtml =
                staff.leaveDate ?
                `<p class="card-text">퇴사일 ${staff.leaveDate}</p>` :
                '<p class="card-text"><span' +
                ' style="visibility:hidden;">퇴사일 2000-01-01</span></p>';

        let rowHtml = `
            <div class="col mb-4">
              <div class="card text-center">
                <img src="${staff.profileImg != null ? staff.profileImg : 'https://joon-s3upload.s3.ap-northeast-2.amazonaws.com/upload/user/avatar.png'}" class="rounded-circle mt-3 mx-auto d-block" style="width: 150px; height: 150px; object-fit: cover;">
                <div class="card-body">
                  <h5 class="card-title mb-1">${staff.fullname} ${positionText}님</h5>
                  <p class="card-text mb-1">${staff.mobile || '-'}</p>
                  <p class="card-text">${staff.email}</p>
                  <p class="card-text mt-4 mb-1">입사일 ${staff.hireDate}</p>
                  ${leaveDateHtml}
                  <a href="staffDetail?staffId=${staff.userId}" class="btn btn-primary w-100">상세보기</a>
                </div>
              </div>
            </div>
        `;
        $('#card-list').append(rowHtml);
    });
}
function displayPagination(data) {

    // 기록이 없을 때, 페이지네이션도 표시되지 않음
    if(data.totalRecords == 0) {
        $("#staff-pagination").html(""); // 바꿔야 할 부분
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

    $("#staff-pagination").html(output);
}



function getListState() {

    let staffListState = sessionStorage.getItem('staffListState');

    if (staffListState) {
        const staffListState = JSON.parse(sessionStorage.getItem('staffListState'));
        Object.assign(staffPageConfig, staffListState);
    }
}

function saveListState() {
    sessionStorage.setItem('staffListState', JSON.stringify({
          ...staffPageConfig
      }));

}