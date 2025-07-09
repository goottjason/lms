const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();

let baseConfig = {
    loginUserId: loginUserId,
    loginUserType: loginUserType
}
let staffConfig = {
    pageNo: 1,
    pageSize: 8,
    type: "fullname",
    keyword: null,
    orderBy: "fullname",
    orderDirection: "ASC",
    // 필터링
    staffId: null,
    staffType: null,
    staffPosition: null,
    onlyLeaver: false,
}

$(document).ready(function() {

    // 세션 관리
    window.addEventListener("beforeunload", function (e) {
        // 외부로 이동 시 config 데이터 삭제
        if (!sessionStorage.getItem("isEnteringDetail")) {
            sessionStorage.removeItem("staffConfig");
        }
        // 내부로 이동 시 플래그만 삭제 (목록으로 돌아와도 config 정보는 남아있음)
        else {
            sessionStorage.removeItem("isEnteringDetail");
        }
    });
    // 세션 정보 불러오기
    getStatus();

    // 상단셀렉트박스 삭제 (only ADMINISTRATOR)
    $("#courseSelector").hide();

    // 페이지 로드시, 퇴사자 포함 체크된 경우 라디오버튼 유지
    if (staffConfig.onlyLeaver == null) {
        $('input[name="staff-filter"][value=""]').prop('checked', true);
    } else if (staffConfig.onlyLeaver == 'true') {
        $('input[name="staff-filter"][value="true"]').prop('checked', true);
    } else if (staffConfig.onlyLeaver == 'false') {
        $('input[name="staff-filter"][value="false"]').prop('checked', true);
    }
    // 페이지 로드시, 셀렉트박스 및 검색창 유지
    $('#type-select').val(staffConfig.staffType);
    $('#position-select').val(staffConfig.staffPosition);
    $('#search-input').val(staffConfig.keyword);

    // 교직원 리스트 로드
    fetchAndDisplayStaffs();

    // 이벤트 핸들러
    $(document).on("change", "input[name='staff-filter']", handleStaffFilterChange);
    $(document).on("change", "#type-select", handleTypeSelectChange);
    $(document).on("change", "#position-select", handlePositionSelectChange);
    $(document).on("click", "#search-button", handleSearchButtonClick);
    $(document).on("keydown", "#search-input", function (e) {
        if (e.key == "Enter") {
            e.preventDefault();
            handleSearchButtonClick();
        }
    });
    $(document).on("click", ".page-link", handlePageButtonClick);
});

function getStatus() {
    let staffStatusByUser = sessionStorage.getItem("staffConfig");
    if (staffStatusByUser) {
        const parsedStaffConfig = JSON.parse(
            sessionStorage.getItem("staffConfig"));
        Object.assign(staffConfig, parsedStaffConfig);
    }
}
function setStatus() {
    sessionStorage.setItem(
        "staffConfig", JSON.stringify(staffConfig));
}
function setFlag() {
    sessionStorage.setItem(
        'isEnteringDetail', 'true');
}

async function fetchAndDisplayStaffs() {
    let staffsWithPaging = await apiGetRequestParams(
        '/api/operationsmanagement/staffs',
        {...baseConfig, ...staffConfig});
    displayView(staffsWithPaging);
}
async function apiGetRequestParams(endpoint, params) {
    try {
        const response = await axios.get(endpoint, {params: params});
        return response.data.data;
    } catch (error) {
        return [];
    }
}
function displayView(staffsWithPaging) {
    updateStatusBar(staffsWithPaging);
    displayCardList(staffsWithPaging);
    displayPagination(staffsWithPaging, $("#staff-pagination"));
}
function updateStatusBar(staffsWithPaging) {
    if (staffConfig.keyword == null || staffConfig.keyword == "") {
        $("#categorize-content").text(
            getStaffLabel(staffConfig.staffType, staffConfig.staffPosition, staffConfig.onlyLeaver)
        );
    } else {
        $("#categorize-content").text(`'${staffConfig.keyword}' 검색결과`);
    }
    $("#number-of-staff")
    .text(`총 인원: ${staffsWithPaging.totalRecords}명`);
}
function getStaffLabel(staffType, position, onlyLeaver) {
    const staffLabelMap = {
        null: {
            null: '전체 관리자 및 강사',
            GENERAL_MANAGER: '총괄관리자',
            COURSE_HEAD: '학과장',
            FULLTIME_INSTRUCTOR: '전임강사'
        },
        ADMINISTRATOR: {
            null: '전체 관리자',
            GENERAL_MANAGER: '총괄관리자',
            COURSE_HEAD: '학과장'
        },
        INSTRUCTOR: {
            null: '전체 강사',
            FULLTIME_INSTRUCTOR: '전임강사'
        }
    };
    const leaverSuffixMap = {
        null: ' (전체)',
        true: ' (퇴사자만)',
        false: ' (재직자만)'
    };
    // staffType과 position이 없는 경우 안전하게 처리
    const typeKey = staffType === null ? 'null' : staffType;
    const posKey = position === null ? 'null' : position;
    const label = staffLabelMap[typeKey]?.[posKey] || '';
    const suffix = leaverSuffixMap[String(onlyLeaver)] || '';
    return label + suffix;
}
function displayCardList(staffsWithPaging) {
    $('#card-list').empty();

    let staffs = staffsWithPaging?.records || [];
    if (!Array.isArray(staffs)) {
        staffs = [];
    }

    if (staffs.length == 0) {
        $("#card-list").removeClass("row-cols-md-4");
        $("#card-list").html(
            "<p class='text-center w-100 pt-3'>교직원이 없습니다.</p>");
        return;
    } else {
        let baseImg = "https://joon-s3upload.s3.ap-northeast-2.amazonaws.com/upload/user/avatar.png";
        $("#card-list").addClass("row-cols-md-4");
        staffs.forEach((staff) => {
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
                    `<p class="card-text">
                    <span style="visibility:hidden;">퇴사일 2000-01-01</span>
                 </p>`;

            let rowHtml = `
                <div class="col mb-4">
                  <div class="card text-center">
                    <img src="${staff.profileImg != null ? staff.profileImg : baseImg}" class="rounded-circle mt-3 mx-auto d-block" style="width: 150px; height: 150px; object-fit: cover;">
                    <div class="card-body">
                      <h5 class="card-title mb-1" style="min-height: 48px;">${staff.fullname} ${positionText}님</h5>
                      <p class="card-text mb-1">${staff.mobile || '-'}</p>
                      <p class="card-text" style="min-height: 48px;">${staff.email}</p>
                      <p class="card-text mt-4 mb-1">입사일 ${staff.hireDate}</p>
                      ${leaveDateHtml}
                      <a href="staffDetail?staffId=${staff.userId}" 
                         class="btn btn-primary w-100" onclick="setFlag();">상세보기</a>
                    </div>
                  </div>
                </div>
            `;
            $('#card-list').append(rowHtml);
        });
    }
}
function displayPagination(data, $selector) {

    // 기록이 없을 때, 페이지네이션도 표시되지 않음
    if (data.totalRecords == 0) {
        $selector.html("");
        return;
    }

    let output = `<ul class="pagination justify-content-center" style="margin:20px 0">`;

    // 이전 버튼
    let prevBlockPage = data.blockStartPage > 1 ? data.blockStartPage - 1 : 1;
    output += `
    <li class="page-item ${data.blockStartPage == 1 ? "disabled" : ""}">
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
    let nextBlockPage = data.blockEndPage < data.lastPage ? data.blockEndPage +
                                                            1 : data.lastPage;
    output += `
    <li class="page-item ${data.blockEndPage == data.lastPage ? "disabled" : ""}">
      <a class="page-link page-btn" href="#" data-page="${nextBlockPage}">다음</a>
    </li></ul>`;

    $selector.html(output);
}

function handleStaffFilterChange() {
    var selectedValue = $('input[name="staff-filter"]:checked').val();

    staffConfig.onlyLeaver = selectedValue == '' ? null : selectedValue;
    staffConfig.pageNo     = 1;
    staffConfig.pageSize   = 8;
    setStatus();

    fetchAndDisplayStaffs();
}
function handleTypeSelectChange() {

    var type = $(this).val();
    var $positionSelect = $('#position-select');
    var $positionOptions = $positionSelect.find('option');

    $positionOptions.show();

    // 타입별로 포지션옵션 필터 후 전체 보이도록
    if (type === 'ADMINISTRATOR') {
        $positionOptions.filter('[value="FULLTIME_INSTRUCTOR"]').hide();
        $positionSelect.val('');
    } else if (type === 'INSTRUCTOR') {
        $positionOptions.filter('[value="GENERAL_MANAGER"], [value="COURSE_HEAD"]').hide();
        $positionSelect.val('');
    } else if (type === null) {
        $positionSelect.val('');
    }
    // 기존 검색과 페이징 초기화
    staffConfig.keyword = null;
    $("#search-input").val("");
    staffConfig.pageNo     = 1;
    staffConfig.pageSize   = 8;
    // type (전체, 관리자, 강사)에 따라 교직원 전체, 관리자 전체, 강사 전체로 필터링
    staffConfig.staffType = type ? type : null;
    staffConfig.staffPosition = null;
    setStatus();

    fetchAndDisplayStaffs();
}
function handlePositionSelectChange() {
    var position = $(this).val();
    var type = $('#type-select').val();
    // 기존 검색과 페이징 초기화
    staffConfig.keyword = null;
    $("#search-input").val("");
    staffConfig.pageNo     = 1;
    staffConfig.pageSize   = 8;
    /* position (전체, 총괄관리자, 학과장, 전임강사)에 따라
    교직원 전체, 총괄관리자 전체, 학과장 전체, 전임강사 전체로 필터링 */
    staffConfig.staffType = type ? type : null;
    staffConfig.staffPosition = position ? position : null;
    setStatus();

    fetchAndDisplayStaffs();
}
function handleSearchButtonClick() {

    // 페이징 초기화 및 검색한 키워드로 검색
    staffConfig.pageNo   = 1;
    staffConfig.pageSize = 8;
    staffConfig.keyword  = $("#search-input").val();

    // 관리자의 경우 전체에서 검색
    staffConfig.staffType = null;
    $("#type-select").val('');
    staffConfig.staffPosition = null;
    $("#position-select").val('');
    /*staffConfig.onlyLeaver = null;
    $("input[name='employee-filter'][value='']").prop('checked', true);*/

    setStatus();
    fetchAndDisplayStaffs();
}
function handlePageButtonClick() {
    staffConfig.pageNo = $(this).data("page");

    setStatus();
    fetchAndDisplayStaffs();
}