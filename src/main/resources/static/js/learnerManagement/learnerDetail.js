const loginUserId = $('#login-user-id').val();
const loginUserType = $('#login-user-type').val();

const queryStrings = new URLSearchParams(window.location.search);

let baseConfig = {
  loginUserId  : loginUserId,
  loginUserType: loginUserType
};

const learnerConfig = {
  pageNo        : null,
  pageSize      : null,
  type          : "userFullname",
  keyword       : null,
  orderBy       : "userFullname",
  orderDirection: "ASC",
  // 필터링
  coIsInProgress: null,
  leCourseId    : null,
  leId          : queryStrings.get("leId")
};

// part 정보 불러오고 페이징하기 위한 전역변수
let learnersWithPaging = null;
let partOverview = [];
let partList = [];
let currentPage = 1;
let pageSize = 5;

$(document).ready(function() {

  const $courseSelector = $("#courseSelector");
  // 강사는 상단셀렉트박스 필요, 선택시 핸들러 필요
  if (loginUserType == "INSTRUCTOR") {
    $courseSelector.empty();
    $courseSelector.append(
        `<option value="">${queryStrings.get('coName')}</option>`);
    $courseSelector.prop("disabled", true);
  } else if (loginUserType == "ADMINISTRATOR") {
    $courseSelector.hide();
  }

  fetchAndDisplayPartView();


  $(document).on('click', '#employ-edit-button', handleEmployEditButtonClick);
  $(document).on('click', '#employ-cancel-button', handleEmployCancelButtonClick);
  $(document).on('click', '#employ-save-button', handleEmploySaveButtonClick);


  $(document).on('click', '#part-detail-button', handlePartDetailButtonClick);

  $(document).on('click', '.page-btn', function(e) {
    e.preventDefault();
    const page = Number($(this).data('page'));
    if (!isNaN(page) && page !== currentPage) {
      currentPage = page;
      renderPartTableAndPagination();
    }
  });
  $(document).on('click', '#drop-button', handleDropButtonClick);
  $(document).on('click', '.part-edit-button', handlePartEditButtonClick);
  $(document).on('click', '.part-save-button', handlePartSaveButtonClick);
  $(document).on('click', '.part-cancel-button', handlePartCancelButtonClick);
});

/* ================================================================================ */

async function fetchAndDisplayPartView() {
  console.log("learnerConfig: ", learnerConfig);
  learnersWithPaging = await apiGetRequestParams(
      "/api/learnermanagement/learners",
      {...baseConfig, ...learnerConfig});
  console.log(learnersWithPaging);
  if (learnersWithPaging.records[0].leCompletionStatus != 'DROPPED'
      && learnersWithPaging.records[0].leCompletionStatus != 'COMPLETED'
      && loginUserType == "ADMINISTRATOR") {
    $('#drop-button').show();
    $('.padding-flag').removeClass('py-3').addClass('py-2');
  }
  displayPartChart();
}
async function apiGetRequestParams(endpoint, params) {
  try {
    const response = await axios.get(endpoint, {params: params});
    return response.data.data;
  } catch (error) {
    return [];
  }
}
function displayPartChart() {
  if (
      learnersWithPaging &&
      Array.isArray(learnersWithPaging.records) &&
      learnersWithPaging.records.length > 0
  ) {
    partOverview = learnersWithPaging.records[0].partOverview || [];
    partList = partOverview.partList;
  }

  console.log(partOverview);

  // statusCount가 없으면 함수 종료
  if (!partOverview.statusCount) {
    $('#partChart').html(`<span>데이터가 없습니다.</span>`);
    return;
  }
  // Google Charts 라이브러리 로드
  google.charts.load('current', {'packages':['corechart']});
  google.charts.setOnLoadCallback(
      function() {
        drawChart(partOverview.statusCount);
      }
  );

}
function drawChart(statusCount) {
  const allStatuses = ['LATE', 'ABSENCE', 'LEAVE_EARLY', 'ATTENDANCE', 'VACATION'];
  const colors = ['#f6c23e', '#e74a3b', '#858796', '#36b9cc', '#4e73df'];
  let chartData = [['상태', '횟수']]; // 헤더 행

  let total = 0;
  allStatuses.forEach(status => {
    const count = statusCount[status] || 0;
    total += count;
    const koreanLabel = `${getKoreanLabel(status)} ${count}`;
    chartData.push([koreanLabel, count]);
  });

  const $chartDiv = $('#partChart');
  $chartDiv.empty(); // 기존 차트/메시지 제거

  if (total === 0) {
    // 데이터가 없을 때 메시지 표시
    $chartDiv.css('position', 'relative').append(
        `<div class="no-data-message" style="
        position: absolute;
        top: 50%; left: 50%;
        transform: translate(-50%, -50%);
        font-size: 1.5em; color: #888; text-align: center;
        width: 100%;
      ">데이터가 없습니다.</div>`
    );
    // 리사이즈 시에도 메시지 유지
    $(window).off('resize.drawChart').on('resize.drawChart', function() {
      $chartDiv.find('.no-data-message').css({
                                               top: '50%',
                                               left: '50%',
                                               transform: 'translate(-50%, -50%)'
                                             });
    });
    return;
  }

  let data = google.visualization.arrayToDataTable(chartData);
  let options = {
    title: '',
    pieHole: 0.4,
    chartArea: { left: 20, top: 10, width: '90%', height: '75%' },
    legend: {
      position: 'bottom',
      alignment: 'center',
      maxLines: 1,
      textStyle: { fontSize: 14 }
    },
    colors: colors,
    sliceVisibilityThreshold: 0
  };

  let chart = new google.visualization.PieChart($chartDiv[0]);
  chart.draw(data, options);

  // 창 크기 변경 시 차트 다시 그리기
  $(window).off('resize.drawChart').on('resize.drawChart', function() {
    chart.draw(data, options);
  });
}
function getKoreanLabel(englishStatus) {
  const statusMap = {
    'LATE': '지각',
    'ABSENCE': '결석',
    'LEAVE_EARLY': '조퇴',
    'ATTENDANCE': '출석',
    'VACATION': '휴가'
  };
  return statusMap[englishStatus] || englishStatus;
}



/* ================================================================================ */

// 중도탈퇴처리
function handleDropButtonClick() {
  Swal.fire({
              title             : "정말 중도탈퇴 처리하시겠습니까?",
              text              : "중도탈퇴 기준에 부합하는지 검토 후 확인을 눌러주세요.",
              icon              : "warning",
              showCancelButton  : true,
              confirmButtonColor: "#3085d6",
              cancelButtonColor : "#d33",
              confirmButtonText : "확인"
            }).then((result) => {
    if (result.isConfirmed) {
      modifyCompletionStatus();
    }
  });
}
async function modifyCompletionStatus() {
  let result = await apiPatchRequestBody(
      `/api/learnermanagement/learners/enrollments/${learnerConfig.leId}`,
      {...baseConfig, leCompletionStatus: 'DROPPED'});
  // 요청 후에 동작은 없음
  if (result != []) {
    await Swal.fire({
                icon: "success",
                title: "수정되었습니다.",
                text: "해당 수강이력이 중도탈퇴로 변경되었습니다.",
                footer: ''
              });
    $('#drop-button').hide();
    $('#learner-completion-status').text('미수료/중도탈퇴');

  } else {
    await Swal.fire({
                icon: "error",
                title: "수정이 불가능합니다.",
                text: "해당 수강이력을 찾을 수 없습니다.",
                footer: ''
              });
  }
}
async function apiPatchRequestBody(endpoint, payload) {
  try {
    const response = await axios.patch(endpoint, payload);
    return response.data.data;
  } catch (error) {
    return [];
  }
}

// 출결현황 상세보기
function handlePartDetailButtonClick() {
  renderPartTableAndPagination();
}
function renderPartTableAndPagination() {

  // 페이징 데이터 생성
  const pagingData = getPagingData(partList, currentPage, pageSize);
  console.log("pagingData: ", pagingData);

  // 현재 페이지에 해당하는 데이터만 추출
  const start = (pagingData.pageNo - 1) * pageSize;
  const end = start + pageSize;
  const pageItems = partList.slice(start, end);
  console.log("pageItems: ", pageItems);

  // 테이블 렌더링
  displayTableBody(pageItems);

  // 페이지네이션 렌더링
  displayPagination(pagingData, $('#part-pagination'));
}
function getPagingData(list, pageNo, pageSize, blockSize = 10) {
  const totalRecords = list.length;
  const lastPage = Math.max(1, Math.ceil(totalRecords / pageSize));
  const page = Math.max(1, Math.min(pageNo, lastPage));
  const currentBlock = Math.ceil(page / blockSize);
  const blockStartPage = (currentBlock - 1) * blockSize + 1;
  const blockEndPage = Math.min(blockStartPage + blockSize - 1, lastPage);
  return {
    totalRecords,
    pageNo: page,
    lastPage,
    blockStartPage,
    blockEndPage
  };
}
function displayTableBody(items) {
  $("#tbody-part").empty();

  let rowHtml = ``;

  if(items.length == 0) {
    rowHtml += `<tr class="text-center"><td colspan="7">데이터가 없습니다.</td></tr>`;
  }
  items.forEach(function (item) {
    rowHtml += `
      <tr class="tr-part" data-id="${item.partId}">
        <td class="text-center align-middle part-date" data-id="${item.partId}">${item.partParticipationDate}</td>
        <td class="text-center align-middle part-check-in" data-id="${item.partId}">${item.partCheckIn ? item.partCheckIn.split('T')[1] : '-'}</td>
        <td class="text-center align-middle part-check-out" data-id="${item.partId}">${item.partCheckOut ? item.partCheckOut.split('T')[1] : '-'}</td>
        <td class="text-center align-middle ${
        item.partStatus == 'LEAVE_EARLY' ? 'text-secondary' :
        item.partStatus == 'VACATION' ? 'text-primary' :
        item.partStatus == 'VACATION_PENDING' ? 'text-primary' :
        item.partStatus == 'LATE' ? 'text-warning' :
        item.partStatus == 'ATTENDANCE' ? 'text-info' :
        item.partStatus == 'ABSENCE' ? 'text-danger' :
        item.partStatus == 'IN_STUDY' ? 'text-secondary' : ''
    } part-status" data-id="${item.partId}">
          ${
        item.partStatus == 'LEAVE_EARLY' ? '조퇴' :
        item.partStatus == 'VACATION' ? '휴가' :
        item.partStatus == 'VACATION_PENDING' ? '휴가(미승인)' :
        item.partStatus == 'LATE' ? '지각' :
        item.partStatus == 'ATTENDANCE' ? '출석' :
        item.partStatus == 'ABSENCE' ? '결석' :
        item.partStatus == 'IN_STUDY' ? '수업중' : ''
    }
        </td>
        <td class="text-center align-middle part-explanation" data-id="${item.partId}">${
        item.partExplanation == null ? '-' : item.partExplanation
    }</td>
        <td class="text-center align-middle part-training-time" data-id="${item.partId}">${item.partTrainingTime}H</td>
        <td class="text-center align-middle">
          <button class="btn btn-sm btn-info btn-icon-split part-edit-button" data-id="${item.partId}">
            <span class="icon text-white-50">
              <i class="fa fa-wrench"></i>
            </span>
            <span class="text">수정</span>
          </button>
          <button class="btn btn-sm btn-primary btn-icon-split part-save-button" data-id="${item.partId}"
                  style="display: none;">
            <span class="icon text-white-50">
              <i class="fa fa-upload"></i>
            </span>
            <span class="text">저장</span>
          </button>
          <button class="btn btn-sm btn-warning btn-icon-split part-cancel-button" data-id="${item.partId}"
                  style="display: none;">
            <span class="icon text-white-50">
              <i class="fa fa-undo"></i>
            </span>
            <span class="text">취소</span>
          </button>
        </td>
      </tr>
    `;
  });
  console.log(rowHtml);
  $("#tbody-part").append(rowHtml);
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
function handlePartEditButtonClick() {

  Swal.fire({
              title             : "출결기록을 임의수정하시겠습니까?",
              html              : `교육기관의 출결정책에 반드시 부합하도록 수정하세요.<br>교육생의 실제 출결현황대로 수정하셔야 합니다.`,
              icon              : "warning",
              showCancelButton  : true,
              confirmButtonColor: "#3085d6",
              cancelButtonColor : "#d33",
              confirmButtonText : "확인"
            }).then((result) => {
    if (result.isConfirmed) {
      modifyPartInfoByPartId($(this).data('id'));
    }
  });
}
function modifyPartInfoByPartId(partId) {

  $('.part-edit-button[data-id="' + partId + '"]').hide();
  $('.part-save-button[data-id="' + partId + '"]').show();
  $('.part-cancel-button[data-id="' + partId + '"]').show();


  // 체크인
  let $checkInTd = $('.part-check-in[data-id="' + partId + '"]');
  let checkInVal = $checkInTd.text().trim();
  let checkInInput = `
    <input type="time" class="form-control part-check-in-input" data-id="${partId}" 
           value="${checkInVal !== '-' ? checkInVal : ''}">`;
  $checkInTd.html(checkInInput);

  // 체크아웃
  let $checkOutTd = $('.part-check-out[data-id="' + partId + '"]');
  let checkOutVal = $checkOutTd.text().trim();
  let checkOutInput = `
    <input type="time" class="form-control part-check-out-input" data-id="${partId}" 
           value="${checkOutVal !== '-' ? checkOutVal : ''}">`;
  $checkOutTd.html(checkOutInput);

  // 상태
  let $statusTd = $('.part-status[data-id="' + partId + '"]');
  let statusVal = $statusTd.text().trim();
  let statusOptions = [
    { value: 'LATE', label: '지각' },
    { value: 'ABSENCE', label: '결석' },
    { value: 'LEAVE_EARLY', label: '조퇴' },
    { value: 'ATTENDANCE', label: '출석' },
    { value: 'VACATION', label: '휴가' },
    { value: 'VACATION_PENDING', label: '휴가(미승인)' },
    { value: 'IN_STUDY', label: '수업중' }
  ];
  let selectHtml = `<select class="form-control part-status-input" data-id="${partId}">`;
  statusOptions.forEach(function(opt) {
    let selected = (opt.label === statusVal) ? 'selected' : '';
    selectHtml += `<option value="${opt.value}" ${selected}>${opt.label}</option>`;
  });
  selectHtml += `</select>`;
  $statusTd.html(selectHtml);

  // 사유
  let $explanationTd = $('.part-explanation[data-id="' + partId + '"]');
  let explanationVal = $explanationTd.text().trim();
  let explanationInput = `
    <input type="text" class="form-control part-explanation-input" data-id="${partId}" 
    value="${explanationVal !== '-' ? explanationVal : ''}">`;
  $explanationTd.html(explanationInput);

  // 인정시간
  let $trainingTimeTd = $('.part-training-time[data-id="' + partId + '"]');
  let trainingTimeVal = parseInt($trainingTimeTd.text());
  let trainingSelect = `<select class="form-control part-training-time-input" data-id="${partId}">`;
  for (let i = 0; i <= 8; i++) {
    let selected = (i === trainingTimeVal) ? 'selected' : '';
    trainingSelect += `<option value="${i}" ${selected}>${i}H</option>`;
  }
  trainingSelect += `</select>`;
  $trainingTimeTd.html(trainingSelect);
}
async function handlePartSaveButtonClick() {
  // 저장 버튼 클릭 시, 해당 partId 추출
  let partId = $(this).data('id');

  // 각 input/select에서 값 추출 (data-id로 해당 partId만 선택)
  let partDateVal = $('.part-date[data-id="' + partId + '"]').text().trim(); // 'YYYY-MM-DD'
  let checkInValOnlyTime = $('.part-check-in-input[data-id="' + partId + '"]').val();
  let checkInVal = checkInValOnlyTime ? (partDateVal + 'T' + checkInValOnlyTime) : null;
  let checkOutValOnlyTime = $('.part-check-out-input[data-id="' + partId + '"]').val();
  let checkOutVal = checkOutValOnlyTime ? (partDateVal + 'T' + checkOutValOnlyTime) : null;
  let statusVal = $('.part-status-input[data-id="' + partId + '"]').val();
  let explanationValRaw = $('.part-explanation-input[data-id="' + partId + '"]').val();
  let explanationVal = explanationValRaw.trim() === "" ? null : explanationValRaw.trim();
  let trainingTimeVal = $('.part-training-time-input[data-id="' + partId + '"]').val();

  const fields = {
    '입실시간': checkInVal,
    '퇴실시간': checkOutVal,
    '출결상태': statusVal,
    '휴가사유': explanationVal,
    '인정시간': trainingTimeVal
  }
  console.log(fields);

  let result = isValidForPartModify(fields);
  console.log(result);
  if (result != "success") {
    await Swal.fire({
                      icon: "error",
                      title: "입력 오류",
                      text: `${result}`,
                      footer: ''
                    });
    return; // 저장 요청 중단
  }

  // partRequest 객체에 값 담기
  let partRequest = {
    partCheckIn: checkInVal,
    partCheckOut: checkOutVal,
    partStatus: statusVal,
    partExplanation: explanationVal,
    partTrainingTime: trainingTimeVal
  };
  console.log(partRequest);
  savePartInfoByPartId($(this).data('id'), partRequest);

  // 요청한 value 그대로 text로 출력

}

function isValidForPartModify(fields) {
  if(fields['입실시간'] > fields['퇴실시간']) {
    return '입실시간 이전에 퇴실시간이 기입될 수 없습니다.'
  }
  if(fields['입실시간'] == null && fields['퇴실시간'] != null) {
    return '입실시간 기입 없이 퇴실시간 기입은 불가능합니다.'
  }
  /*if(fields['휴가사유'] != null && fields['휴가사유'].length > 0 && fields['출결상태'] != 'VACATION') {
    return '휴가사유는 출결사유가 휴가일 때 기입 가능합니다.'
  }*/
  if(fields['휴가사유'] == null && fields['출결상태'] == 'VACATION') {
    return '휴가일 경우, 휴가사유를 기입해주세요.'
  }
  return "success";
}
async function savePartInfoByPartId(partId, partRequest) {

  let result = await apiPatchRequestBody(
      `/api/learnermanagement/learners/participations/${partId}`,
      {...baseConfig, ...partRequest});
  console.log(result);
  // 요청 후에 동작은 없음
  if (result) {
    await Swal.fire({
                position: "top-end",
                icon: "success",
                title: "저장 완료!",
                showConfirmButton: false,
                timer: 1500
              });
    displayTrView(partId);
  } else {
    Swal.fire({
                icon: "error",
                title: "저장이 불가능합니다.",
                text: "해당 출결이력을 찾을 수 없습니다.",
                footer: ''
              });
  }
}
function handlePartCancelButtonClick() {
  let partId = $(this).data('id');
  displayTrView(partId);
}
async function displayTrView(partId) {
  await fetchAndDisplayPartView()
  displayOnlyTr(partList, partId);
}

function displayOnlyTr(partList, partId) {
  console.log(partId, "아이디 가져오니");
  $(`tr[data-id=${partId}]`).empty();
  partList.forEach(function (item) {
    if (item.partId == partId) {
      let trHtml = `
        <td class="text-center align-middle part-date" data-id="${item.partId}">${item.partParticipationDate}</td>
        <td class="text-center align-middle part-check-in" data-id="${item.partId}">${item.partCheckIn ? item.partCheckIn.split('T')[1] : '-'}</td>
        <td class="text-center align-middle part-check-out" data-id="${item.partId}">${item.partCheckOut ? item.partCheckOut.split('T')[1] : '-'}</td>
        <td class="text-center align-middle ${
          item.partStatus == 'LEAVE_EARLY' ? 'text-secondary' :
          item.partStatus == 'VACATION' ? 'text-primary' :
          item.partStatus == 'VACATION_PENDING' ? 'text-primary' :
          item.partStatus == 'LATE' ? 'text-warning' :
          item.partStatus == 'ATTENDANCE' ? 'text-info' :
          item.partStatus == 'ABSENCE' ? 'text-danger' :
          item.partStatus == 'IN_STUDY' ? 'text-secondary' : ''
      } part-status" data-id="${item.partId}">
              ${
          item.partStatus == 'LEAVE_EARLY' ? '조퇴' :
          item.partStatus == 'VACATION' ? '휴가' :
          item.partStatus == 'VACATION_PENDING' ? '휴가(미승인)' :
          item.partStatus == 'LATE' ? '지각' :
          item.partStatus == 'ATTENDANCE' ? '출석' :
          item.partStatus == 'ABSENCE' ? '결석' :
          item.partStatus == 'IN_STUDY' ? '수업중' : ''
      }
            </td>
            <td class="text-center align-middle part-explanation" data-id="${item.partId}">${
          item.partExplanation == null ? '-' : item.partExplanation
      }</td>
            <td class="text-center align-middle part-training-time" data-id="${item.partId}">${item.partTrainingTime}H</td>
            <td class="text-center align-middle">
              <button class="btn btn-sm btn-info btn-icon-split part-edit-button" data-id="${item.partId}">
                <span class="icon text-white-50">
                  <i class="fa fa-wrench"></i>
                </span>
                <span class="text">수정</span>
              </button>
              <button class="btn btn-sm btn-primary btn-icon-split part-save-button" data-id="${item.partId}"
                      style="display: none;">
                <span class="icon text-white-50">
                  <i class="fa fa-upload"></i>
                </span>
                <span class="text">저장</span>
              </button>
              <button class="btn btn-sm btn-warning btn-icon-split part-cancel-button" data-id="${item.partId}"
                      style="display: none;">
                <span class="icon text-white-50">
                  <i class="fa fa-undo"></i>
                </span>
                <span class="text">취소</span>
              </button>
            </td>
    `;
      $(`tr[data-id=${partId}]`).append(trHtml);
    }

  });
}


// 취업관리
function handleEmployEditButtonClick() {
  // 수정 버튼 가리고, 저장-취소 버튼 보이기
  $('#employ-edit-button').hide();
  $('#employ-save-button').show();
  $('#employ-cancel-button').show();

  $('input[type="text"]').prop('readonly', false);
  $('input[type="radio"]').prop('readonly', false).removeAttr('readonly')
    .css('pointer-events', 'auto')
    .removeAttr('onclick');
}
async function handleEmploySaveButtonClick() {

  let companyName = $('#company-name').val().trim();
  let companyPhone = $('#company-phone').val().trim();
  let companyAddress = $('#company-address').val().trim();
  let counselingDetails = $('#counseling-details').val().trim();

  const fields = [
    { value: companyName, label: "업체명" },
    { value: companyPhone, label: "업체전화번호" },
    { value: companyAddress, label: "업체주소" },
    { value: counselingDetails, label: "메모" }
  ];

  // 유효성 검사
  for (const field of fields) {
    const result = isBlankOrOverLimit(field.value, 100);
    if (!result.valid) {
      await Swal.fire({
                        icon: "error",
                        title: "입력 오류",
                        text: `${field.label}: ${result.message}`,
                        footer: ''
                      });
      return; // 저장 요청 중단
    }
  }

  let reqDTO = {
    esId: $('#es-id').val(),
    employmentStatus: $('input[name="status"]:checked').val(),
    companyName: companyName,
    companyPhone: companyName,
    companyAddress: companyAddress,
    isCounselingReceived: $('input[name="is-counseling"]:checked').val() === '1',
    counselingDetails: counselingDetails
  };
  let payload = {
    loginUserId: Number(loginUserId),
    loginUserType: loginUserType,
    leId: queryStrings.get('leId'),
    reqDTO: reqDTO
  };

  let data = await apiPostRequest(
    '/api/learner-employment-support', payload
  )
}
async function apiPostRequest(endpoint, payload = {}, additionalParams = {}) {
  try {
    const response = await axios.post(endpoint, payload);
    console.log(response)
    await Swal.fire({
      icon: "success",
      title: "저장되었습니다!",
      text: "성공적으로 저장되었습니다.",
      footer: ''
    });
    location.reload();
    return response.data;
  } catch (error) {
    console.log(error);
    Swal.fire({
      icon: "error",
      title: "수정불가능!",
      text: "취업관리 DB 테이블을 확인해주세요.",
      footer: ''
    });
    return [];
  }
}
function isBlankOrOverLimit(value, maxLength = 100) {
  // 100자 초과 여부 확인
  if (value.length > maxLength) {
    return { valid: false, message: `${maxLength}자를 초과할 수 없습니다.` };
  }
  return { valid: true };
}
function handleEmployCancelButtonClick() {
  // 새로고침
  location.reload();
}


