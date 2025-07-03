const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();

const queryStrings = new URLSearchParams(window.location.search);
const staffId = queryStrings.get('staffId');

let baseConfig = {
    loginUserId: loginUserId,
    loginUserType: loginUserType
}

let historyPageConfig = {
    pageNo: 1,
    pageSize: 3,
    type: null,
    keyword: null,
    orderBy: null,
    orderDirection: null,
    staffId: staffId,
    staffType: null,
}

$(document).ready(function() {




    getHistories();
    $('#leave-btn').on('click', handleLeaveBtnClick);
    $(document).on('click', '.page-link', handlePageBtnClick);
    $(document).on('click', '#leave-save-btn', handleLeaveSaveBtnClick);
    console.log(baseConfig);

});
function handlePageBtnClick() {
    historyPageConfig.pageNo = $(this).data('page');
    getHistories();
}
function handleLeaveSaveBtnClick() {
    let leaveDate = $('.leave-date-input').val();

    Swal.fire({
                  title             : "정말 퇴사처리하시겠습니까?",
                  text              : "",
                  icon              : "warning",
                  showCancelButton  : true,
                  confirmButtonColor: "#3085d6",
                  cancelButtonColor : "#d33",
                  confirmButtonText : "저장"
              }).then((result) => {
        if (result.isConfirmed) {
            if (leaveDate == '') {
                leaveDate = 'NULL';
                console.log(leaveDate);
            }
            updateLeaveDateByStaffId(leaveDate);
        }
    });
}
async function updateLeaveDateByStaffId(leaveDate) {

    let result = await apiPatchRequestAboutStaff(
        '/api/management/staff', leaveDate
    );
    if(result == true){
        window.location.href = '/operationsManagement/staffDetail?staffId=' + staffId;
    } else {
        return;
    }

}
function handleLeaveBtnClick() {
    $('#leave-btn').hide();
    $('#leave-save-btn').show();

    var $leaveInput = $('.leave-date-input');
    var hasLeaveDate = $leaveInput.val().trim() !== '' && $leaveInput.val() !== '-';

    // input 보이기
    $leaveInput.show();
    // 기존 span 숨기기
    $('.leave-date-view').hide();

    // leaveDate가 있으면 readonly 해제 (수정 가능)
    $leaveInput.prop('readonly', false);

    // 만약 leaveDate가 없으면 오늘 날짜로 세팅
    if (!hasLeaveDate) {
        var today = new Date().toISOString().split('T')[0];
        $leaveInput.val(today);
    }
}
async function getHistories() {
    let historyWithPagination = await apiGetRequestAboutStaff(
        '/api/management/staffhistories');
    console.log(historyWithPagination);
    let histories = historyWithPagination?.staffHistories || [];
    if (!Array.isArray(histories)) histories = [];
    console.log(histories);
    displayTables(histories);
    displayPagination(historyWithPagination);
}
function displayTables(histories) {
    $("#table-body").empty();
    if (histories == null || histories.length === 0) {
        $("#table-body").html(
            "<tr><td colspan='4' class='text-center pt-3'>과정이력이" +
            " 없습니다.</td></tr>");
    }
    histories.forEach(history => {
        let rowHtml = `
          <tr>
            <td class="text-center align-middle">
${history.courseIsInProgress ? '진행중' : '종료'}</td>
            <td class="title align-middle">${history.courseName}</td>
            <td class="text-center align-middle">${history.courseStartDate} ~ ${history.courseEndDate} </td>
            <td class="text-center align-middle">${history.courseNumberOfLearner}명</td>
          </tr>
        `;
        $("#table-body").append(rowHtml);
    })
}
function displayPagination(data) {
    // 기록이 없을 때, 페이지네이션도 표시되지 않음
    if (data.totalRecords == 0) {
        $("#history-pagination").html("");
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

    $("#history-pagination").html(output);
}

async function apiGetRequestAboutStaff(endpoint, additionalParams = {}) {
    try {
        console.log(historyPageConfig.staffId)
        const response = await axios.get(endpoint, {
            params: { ...baseConfig, ...historyPageConfig }
        });
        console.log(response.data);
        return response.data.data;
    } catch (error) {
        console.error(`${endpoint} 요청 오류:`, error);
        return [];
    }
}

async function apiPatchRequestAboutStaff(endpoint, leaveDate) {
    try {
        const response = await axios.patch(
            endpoint,
            null,
            {
                params: {
                    loginUserId: loginUserId,
                    loginUserType: loginUserType,
                    staffId: staffId,
                    leaveDate: leaveDate
                }
            }
        );
        console.log(response.data);
        Swal.fire({
                      icon: "success",
                      title: "저장완료!",
                      text: "성공적으로 저장되었습니다.",
                      footer: ''
                  });
        return true;
    } catch (error) {
        Swal.fire({
                      icon: "error",
                      title: "저장실패!",
                      text: "다시 시도해주세요.",
                      footer: ''
                  });
        console.error(`${endpoint} 요청 오류:`, error);
        return false;
    }
}