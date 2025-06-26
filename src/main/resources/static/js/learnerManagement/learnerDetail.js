const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();
const params = new URLSearchParams(window.location.search);
const leId = params.get('leId');

const config = {
  pageNo        : 1,
  pageSize      : 5,
  type          : null,
  keyword       : null,
  orderBy       : null,
  orderDirection: null,
};



$(document).ready(function() {

  $("#courseSelector").hide();

  let str = $('#status-count-map').val();

  const trimmed = str.slice(1, -1);
  const pairs = trimmed.split(/\s*,\s*/);
  let obj = {};
  pairs.forEach(pair => {
    const [key, value] = pair.split('=');
    obj[key] = Number(value);
  });
  console.log(obj);
  if (Object.keys(obj).length === 0 || (Object.keys(obj).length === 1 && Object.keys(obj)[0] === "")) {
    obj = {
      ATTENDANCE: 0,
      ABSENCE: 0,
      VACATION: 0,
      LATE: 0,
      LEAVE_EARLY: 0
    };
    $(".chart-pie").html(`<span class="text-center">데이터가 없습니다.</span>`);
  }
  displayChartValues(obj);

  // Pie Chart 데이터
  let $ctx = $("#myPieChart");
  let data = [obj.ATTENDANCE, obj.ABSENCE, obj.VACATION, obj.LATE, obj.LEAVE_EARLY];
  var myPieChart = new Chart($ctx, {
    type   : "doughnut",
    data   : {
      labels  : ["출석", "결석", "휴가", "지각", "조퇴"],
      datasets: [{
        data                : data,
        backgroundColor     : ["#36b9cc", "#e74a3b", "#4e73df", "#f6c23e", "#858796"],
        hoverBackgroundColor: ["#2ca4b5", "#c0392b", "#3b5cc9", "#d9a820", "#6e707f"],
        hoverBorderColor    : "rgba(234, 236, 244, 1)",
      }],
    },
    options: {
      maintainAspectRatio: false,
      tooltips           : {
        backgroundColor: "rgb(255,255,255)",
        bodyFontColor  : "#858796",
        borderColor    : "#dddfeb",
        borderWidth    : 1,
        xPadding       : 15,
        yPadding       : 15,
        displayColors  : false,
        caretPadding   : 10,
      },
      legend             : {
        display: false
      },
      cutoutPercentage   : 75,
    },
  });





  $(document).on('click', '#edit-button', handleEditBtnClick);
  $(document).on('click', '#save-button', handleSaveBtnClick);
  $(document).on('click', '#prticipation-modal-button', handleParticipationModalBtnClick);
  $(document).on("click", ".page-link", handlePageBtnClick);
  $(document).on("click", "#to-learner-list", handlePageBtnClick);


});

function displayChartValues(obj) {
  setTextIfNullOrEmpty("#ATTENDANCE", obj.ATTENDANCE);
  setTextIfNullOrEmpty("#ABSENCE", obj.ABSENCE);
  setTextIfNullOrEmpty("#VACATION", obj.VACATION);
  setTextIfNullOrEmpty("#LATE", obj.LATE);
  setTextIfNullOrEmpty("#LEAVE_EARLY", obj.LEAVE_EARLY);
}
function setTextIfNullOrEmpty(selector, value) {
  value = (value === null || value === "" || value === undefined) ? "0" : value;
  $(selector).text(value);
}
function handlePageBtnClick() {
  config.pageNo = $(this).data("page");
  fetchAndDisplayView();
}

function handleParticipationModalBtnClick() {
  config.pageNo = 1;
  fetchAndDisplayView();
}

async function fetchAndDisplayView() {

  let participationsWithPagination = await apiGetRequest(
    "/api/participations",
    {
      loginUserId  : loginUserId,
      loginUserType: loginUserType,
      leId : leId
    });
  let participations
    = participationsWithPagination?.respDTOS || [];
  if (!Array.isArray(participations)) {
    participations = [];
  }
  console.log("RESP", participations);
  renderCourseTable(participations);
  renderCoursePagination(participationsWithPagination);
}

async function apiGetRequest(endpoint, additionalParams = {}) {
  try {
    const response = await axios.get(endpoint, {
      params: {...config, ...additionalParams}
    });
    console.log(response.data);
    return response.data;
  } catch (error) {
    console.error(`${endpoint} 요청 오류:`, error);
    return [];
  }
}

function renderCourseTable(participations) {
  $("#tbody-participation").empty();
  let rowHtml = ``;
  if(participations.length == 0) {
    rowHtml += `<tr><td class="text-center" colspan="4">데이터가 없습니다.</td></tr>`;
  }
  participations.forEach(function (item) {
    rowHtml += `
  <tr>
    <td class="text-center align-middle">${item.pparticipationDate}</td>
    <td class="text-center align-middle ${
      item.pstatus == 'LEAVE_EARLY' ? 'text-secondary' :
        item.pstatus == 'VACATION' ? 'text-primary' :
          item.pstatus == 'VACATION_PENDING' ? 'text-primary' :
            item.pstatus == 'LATE' ? 'text-warning' :
              item.pstatus == 'ATTENDANCE' ? 'text-info' :
                item.pstatus == 'ABSENCE' ? 'text-danger' : ''
    }">
      ${
      item.pstatus == 'LEAVE_EARLY' ? '조퇴' :
        item.pstatus == 'VACATION' ? '휴가' :
          item.pstatus == 'VACATION_PENDING' ? '휴가(미승인)' :
            item.pstatus == 'LATE' ? '지각' :
              item.pstatus == 'ATTENDANCE' ? '출석' :
                item.pstatus == 'ABSENCE' ? '결석' :
                  item.pstatus
    }
    </td>
    <td class="text-center align-middle">${
      item.prExplanation == null ? '-' : item.prExplanation
    }</td>
    <td class="text-center align-middle">${item.ptrainingTime}H</td>
  </tr>
`;
  });
  $("#tbody-participation").append(rowHtml);
}
function renderCoursePagination(data) {
  // 기록이 없을 때, 페이지네이션도 표시되지 않음
  if (data.totalRecords == 0) {
    $("#participation-pagination").html("");
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

  $("#participation-pagination").html(output);
}



function handleEditBtnClick() {
  $('#edit-button').hide();
  $('#save-button').show();
  $('input[type="text"]').prop('readonly', false);
  $('input[type="radio"]').prop('readonly', false).removeAttr('readonly')
    .css('pointer-events', 'auto')
    .removeAttr('onclick');
}

async function handleSaveBtnClick() {
  $('#save-button').hide();
  $('#edit-button').show();

  // es가 붙은 필드만 reqDTO로 묶기
  let reqDTO = {
    esId: $('#es-id').val(),
    employmentStatus: $('input[name="status"]:checked').val(),
    companyName: $('#company-name').val(),
    companyPhone: $('#company-phone').val(),
    companyAddress: $('#company-address').val(),
    isCounselingReceived: $('input[name="is-counseling"]:checked').val() === '1',
    counselingDetails: $('#counseling-details').val()
  };
  console.log(reqDTO);
  // loginUserId, loginUserType은 각각 따로 보내기
  let payload = {
    loginUserId: Number(loginUserId),
    loginUserType: loginUserType,
    leId: leId,
    reqDTO: reqDTO
  };

  let data = await apiPostRequest(
    '/api/learner-employment-support', payload
  )

  $('input[type="text"]').prop('readonly', true);
  $('input[type="radio"]').prop('readonly', true).attr('readonly', 'readonly')
    .css('pointer-events', 'none')
    .attr('onclick', 'return false;');
}
// 저장되었습니다 모달 만들기
async function apiPostRequest(endpoint, payload = {}, additionalParams = {}) {
  try {
    const response = await axios.post(endpoint, payload);
    Swal.fire({
      icon: "success",
      title: "저장되었습니다!",
      text: "성공적으로 저장되었습니다.",
      footer: ''
    });
    return response.data;
  } catch (error) {
    Swal.fire({
      icon: "error",
      title: "수정불가능!",
      text: "취업관리 DB 테이블을 확인해주세요.",
      footer: ''
    });
    return [];
  }
}