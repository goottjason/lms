package com.goott5.lms.courseboardmaterials.controller;

import com.goott5.lms.common.domain.FileDTO;
import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.domain.ReadCountLog;
import com.goott5.lms.common.mapper.UtilMapper;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.common.util.S3Uploader;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPageDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingResponseDTO;
import com.goott5.lms.courseboardmaterials.domain.MyResponseWithData;
import com.goott5.lms.courseboardmaterials.mapper.CourseBoardMaterialsMapper;
import com.goott5.lms.courseboardmaterials.service.CourseBoardMaterialsService;
import com.goott5.lms.coursemanagement.domain.CommonReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseReqDTO;
import com.goott5.lms.coursemanagement.domain.CourseRespDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseReqDTO;
import com.goott5.lms.coursemanagement.domain.PageCourseRespDTO;
import com.goott5.lms.coursemanagement.service.CourseManagementService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/courseBoardMaterials")
public class CourseBoardMaterialsController {

    private final CourseBoardMaterialsService courseBoardMaterialsService;
    private final UtilMapper utilMapper;
    private final UtilService utilService;
    private final S3Uploader s3Uploader;
    private final CourseManagementService courseManagementService;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;
    @Value("${cloud.aws.region.static}")
    private String region;

    // 리스트 페이지
    @GetMapping("/materialsList")
    public String getMaterialsList(
        @ModelAttribute("requestDTO") CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO, Model model , HttpSession session,
        @RequestParam(required = false) String courseName) {

        if(courseName != null){
            courseBoardMaterialsPagingRequestDTO.setCourseName(courseName);
            log.info("courseName:{}", courseName);
        }

        // 로그인 사용자 타입 가져오기 (NullPointerException 방지)
        UserVO loginUser = (UserVO) session.getAttribute("loginUser");
        Integer loginUserId = loginUser.getId();
        String loginUserType = loginUser.getType(); // 기본값: 로그인하지 않은 사용자

        if (courseBoardMaterialsPagingRequestDTO.getPageNo() == 0) { // int 기본값은 0
            courseBoardMaterialsPagingRequestDTO.setPageNo(1);
        }
        if (courseBoardMaterialsPagingRequestDTO.getPagingSize() == 0) {
            courseBoardMaterialsPagingRequestDTO.setPagingSize(10);
        }


        if (courseBoardMaterialsPagingRequestDTO.getCourseId() == null && !"ADMINISTRATOR".equals(loginUserType)) {
        CommonReqDTO commonReqDTO = CommonReqDTO.builder()
            .loginUserId(loginUserId)
            .loginUserType(loginUserType)
            .build();

        PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = PageCourseReqDTO.<CourseReqDTO>builder()
            .pageNo(null)
            .pageSize(null)
            .type(null)
            .keyword(null)
            .orderBy("name")
            .orderDirection("ASC")
            .build();

        PageCourseRespDTO<CourseRespDTO> courses = courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

        log.info("courses={}", courses);

            CommonReqDTO commonReqDTO = CommonReqDTO.builder()
                .loginUserId(loginUser.getId())
                .loginUserType(loginUserType)
                .build();
            PageCourseReqDTO<CourseReqDTO> pageCourseReqDTO = PageCourseReqDTO.<CourseReqDTO>builder()
                .orderBy("name").orderDirection("ASC").build();


            PageCourseRespDTO<CourseRespDTO> courses = courseManagementService.findCoursesAllorOne(commonReqDTO, pageCourseReqDTO);

            // 조회된 과정이 있을 경우에만 첫 번째 과정 ID를 기본값으로 설정
            if (courses != null && !courses.getRespDTOS().isEmpty()) {
                Integer defaultCourseId = courses.getRespDTOS().get(0).getId();
                courseBoardMaterialsPagingRequestDTO.setCourseId(defaultCourseId);
                log.info("사용자 기본 과정 ID를 설정합니다: {}", defaultCourseId);
            }
        }

      CourseBoardMaterialsPagingResponseDTO<CourseBoardMaterialsPageDTO> responseDTO = courseBoardMaterialsService.getListWithSearch(courseBoardMaterialsPagingRequestDTO);

        log.info("responseDTO={}", responseDTO.getDtoList());

        if (courseBoardMaterialsPagingRequestDTO.getKeyword() == null || courseBoardMaterialsPagingRequestDTO.getKeyword().isEmpty()) {
            session.setAttribute("keyword", courseBoardMaterialsPagingRequestDTO.getKeyword());
        }

        model.addAttribute("responseDTO", responseDTO);

        model.addAttribute("pagingRequestDTO", courseBoardMaterialsPagingRequestDTO);

        log.info("pageNo={}", courseBoardMaterialsPagingRequestDTO.getPageNo());
        log.info("pagingSize={}", courseBoardMaterialsPagingRequestDTO.getPagingSize());



        if (loginUser != null && loginUser.getType() != null) {
            loginUserType = loginUser.getType(); // 세션에서 가져온 사용자 타입
        }


        model.addAttribute("loginUserType", loginUserType); // 변수명을 loginUserType 으로 통일
        log.info("loginUserType={}", loginUserType);


        return "courseBoardMaterials/materialsList";

    }

    // 등록 페이지(GET)
    @GetMapping("/materialsRegister")
    public String getMaterialsRegister(
        @RequestParam(required = false) Integer courseId,
        Model model) {

        CourseBoardMaterialsDTO dto = new CourseBoardMaterialsDTO();
        if (courseId != null) {
            dto.setCourseId(courseId); // 목록에서 받은 courseId를 DTO에 설정
        }

        model.addAttribute("courseBoardMaterialsDTO", dto);
        return "courseBoardMaterials/materialsRegister";
    }

    // 등록 처리(POST)
    @PostMapping("/materialsRegister")
    public ResponseEntity<MyResponseWithData> insertMaterials(@Valid @ModelAttribute CourseBoardMaterialsDTO courseBoardMaterialsDTO
    , BindingResult bindingResult, @RequestParam(required = false) List<MultipartFile> files, HttpSession session)
        throws IOException {
        log.info("등록된 DTO={}", courseBoardMaterialsDTO);

        UserVO loginUser = (UserVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body(new MyResponseWithData(401,"로그인이 필요합니다!",null));
        }
        courseBoardMaterialsDTO.setWriterId(loginUser.getId());

        String content = courseBoardMaterialsDTO.getContent();
        if (content == null || content.isBlank()) {
            bindingResult.addError(
                new FieldError("courseBoardMaterialsDTO", "content", "글자를 입력해주세요."));
        } else {
            int contentLength = content.getBytes(StandardCharsets.UTF_8).length;
            if (contentLength > 1000 || contentLength < 20) {
                bindingResult.addError(new FieldError("courseBoardMaterialsDTO", "content",
                    "글자 20자 이상 1000자 이하여야 합니다."));
            }
        }

        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();

            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return ResponseEntity.badRequest()
                .body(new MyResponseWithData(400, "에러 발생!!!!", errorMap));
        }
        int courseBoardMaterialsFile = courseBoardMaterialsService.insertCourseBoardMaterials(
            courseBoardMaterialsDTO);


        if (courseBoardMaterialsFile != -1) {
            log.info("등록 성공!!!={}", courseBoardMaterialsDTO);
        } else {
            log.info("등록실패!!");
        }

        log.info("files={}", files);

        if (files != null && !files.isEmpty()) {

            log.info("파일 확인 ={}", files);

            for (MultipartFile file : files) {
                if (file.isEmpty()){
                    log.info("파일 없다. ={}",file.isEmpty());
                    continue;
                }

                // 첨부파일 서버에 저장 + 경로 저장
                String insertPath = s3Uploader.uploadFile("upload/course_notice",file.getInputStream(),
                    file.getOriginalFilename());

                log.info("파일 저장 성공!!");

                // 받은 파일 dto에 세팅
                // db 에서 해당 테이블의 게시글 id 다시 받아오기
                FileDTO fileDTO = FileDTO.builder()
                    .originalName(file.getOriginalFilename())
                    .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
                    .path(insertPath)
                    .size((int) file.getSize())
                    .tableName("course_notice")
                    .tableId(courseBoardMaterialsFile)
                    .build();

                // 첨부 파일 db에 저장
                int fileInsert = utilService.insertService(fileDTO);
                if (fileInsert == 1) {
                    log.info("파일 db에 저장 성공:{}", fileDTO);
                }
                ;
            }

        } else {
            log.info("파일 전달 안됨.");
            return ResponseEntity.ok(
                new MyResponseWithData(200, "글 작성이 완료되었습니다.", courseBoardMaterialsDTO));

        }

        return ResponseEntity.ok(
            new MyResponseWithData(200, "글 작성이 완료되었습니다.", courseBoardMaterialsDTO));

    }

    // 상세 페이지
    @GetMapping("/materialsDetail")
    public String getMaterialsDetail(@RequestParam("id") Integer id, Model model, HttpSession session,
        @ModelAttribute("pagingRequestDTO") CourseBoardMaterialsPagingRequestDTO pagingRequestDTO) {

        // 메소드 시작 시 사용자 정보를 한 번만 가져옵니다.
        UserVO loginUser = (UserVO) session.getAttribute("loginUser");

        // 로그인 여부 확인
        if (loginUser == null) {
            return "redirect:/";
        }

        // 파라미터로 받은 id null 체크
        if (id == null) {
            return "courseBoardMaterials/materialsList";
        }

        log.info("상세 페이지 요청 ID : {}", id);

        CourseBoardMaterialsDetailInfo detail = courseBoardMaterialsService.getCourseBoardMaterialsDetail(id);

        // 조회된 데이터가 없는 경우 목록으로 리다이렉트
        if (detail == null) {
            log.warn("ID {} 에 해당하는 상세 정보를 찾을 수 없습니다.", id);
            return "redirect:/courseBoardMaterials/materialsList?" + pagingRequestDTO.getLink();
        }

        // 모델에 데이터 추가
        model.addAttribute("detail", detail);
        model.addAttribute("loginUserType", loginUser.getType());
        model.addAttribute("loginUserId", loginUser.getId());
        model.addAttribute("currentCourseId", detail.getCourseId());
        model.addAttribute("pagingRequestDTO", pagingRequestDTO);
        model.addAttribute("fileDTOList", detail.getAttachments()); // detail 객체에서 직접 가져옵니다.

        // 파일 조회 처리
        List<FileSelectDTO> fileDTOList = utilMapper.selectFileFrom("course_notice",id);
        if (fileDTOList != null) {
            log.info("fileDTOList : {}", fileDTOList);
            model.addAttribute("fileDTOList", fileDTOList);
        }

        // 조회수 증가 로직 처리
        ReadCountLog readCountLog = ReadCountLog.builder()
            .tableName("course_notice")
            .tableId(id)
            .userId(loginUser.getId()) // 처음에 가져온 loginUser 변수 사용
            .build();

        boolean result = courseBoardMaterialsService.updateReadCount(readCountLog);

        if (!result) {
            log.info("조회수 증가 처리 중 오류 또는 이미 오늘 조회한 사용자.");
        } else {
            log.info("조회수가 성공적으로 업데이트 되었습니다.");
        }

        log.info("상세 정보 불러오기 성공 : {}", detail);


        return "courseBoardMaterials/materialsDetail";
    }

    // 수정 페이지(GET)
    @GetMapping("/materialsModify")
    public String getMaterialsModify(@RequestParam(required = false) int id, Model model,HttpSession session) {

        List<FileSelectDTO> beforeFile = utilService.selectFileList("course_notice",id);

        if (beforeFile != null && beforeFile.size() > 0) {
            model.addAttribute("beforeFile", beforeFile);
        }

        UserVO loginUser = (UserVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/";
        }

        CourseBoardMaterialsDetailInfo detail = courseBoardMaterialsService.getCourseBoardMaterialsDetail(id);

        CourseBoardMaterialsDTO dto = new CourseBoardMaterialsDTO();
        dto.setId(detail.getId());
        dto.setCourseId(detail.getCourseId()); // (추가) courseId를 DTO에 설정
        dto.setTitle(detail.getTitle());
        dto.setContent(detail.getContent());
        dto.setIsFixed(detail.getIsFixed());

        model.addAttribute("currentCourseId", dto.getCourseId());

        model.addAttribute("courseBoardMaterialsDTO", dto);
        model.addAttribute("attachments", detail.getAttachments());

        return "courseBoardMaterials/materialsModify";
    }

    // 수정 처리(POST)
    @PostMapping("/materialsModify")
    public ResponseEntity<MyResponseWithData> postMaterialsModify(
        @Valid @ModelAttribute CourseBoardMaterialsDTO courseBoardMaterialsDTO,
        BindingResult bindingResult,
        @RequestParam(value = "files", required = false) List<MultipartFile> files, // 새로 첨부된 파일
        @RequestParam(required = false) List<Integer> deleteFiles) throws IOException {

        log.info("전송 받은 DTO = {}", courseBoardMaterialsDTO);

        log.info("DTO에 바인딩된 게시글 ID: {}", courseBoardMaterialsDTO.getId());

        if (files != null) {
            log.info("전송 받은 files: {}", files);
        }

        if (deleteFiles != null) {
            log.info("전송 받은 deleteFiles: {}", deleteFiles);
        }

        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();

            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(new MyResponseWithData(400,"에러 발생!!", errorMap));
        }
        log.info("전송 성공 DTO ={}", courseBoardMaterialsDTO);
        log.info("전송 성공 files ={}", files);
        log.info("전송 성공 deleteFiles ={}", deleteFiles);

        // 게시글 수정
        int result = courseBoardMaterialsService.updateCourseBoardMaterials(courseBoardMaterialsDTO);

        if (result != 1) {
            return ResponseEntity.ok(new MyResponseWithData(500,"전송 실패!!",courseBoardMaterialsDTO));
        }

        List<FileSelectDTO> deleteFileList = new ArrayList<>();
        FileSelectDTO fileSelectDTO = null;

        if (deleteFiles != null) {
            if (!deleteFiles.isEmpty()) {

                for (Integer num : deleteFiles) {
                    fileSelectDTO = utilMapper.selectFileById(num);
                    deleteFileList.add(fileSelectDTO);
                }

                log.info("삭제할 deleteFiles ={}", deleteFileList);

                for (FileSelectDTO selectDTO : deleteFileList) {
                    log.info("selectDTO.getPath:{}", selectDTO.getPath());
                    s3Uploader.deleteFile("upload/course_notice/" + selectDTO.getNewName());
                    log.info("파일 서버 삭제 성공?"); // 성공 못함

                    if (utilService.deleteFileById(selectDTO.getId()) == 1) {
                        log.info("파일 db 삭제 성공"); // 성공
                    }
                }
            }
        }

        // 수정시 생성된 파일
        if (files != null && !files.isEmpty()) {

            log.info(">>>>> 파일 처리 블록에 진입했습니다. 감지된 파일 개수: {}개 <<<<<", files.size());

            if (courseBoardMaterialsDTO.getId() == 0) {
                log.error("게시글 ID가 없어 파일을 저장할 수 없습니다. DTO: {}", courseBoardMaterialsDTO);
                return ResponseEntity.internalServerError()
                    .body(new MyResponseWithData(500, "오류로 인해 파일 저장에 실패했습니다.", null));
            }

                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue;
                    }
                    // 첨부 파일 서버에 저장 + 경로 저장
                    String insertPath = s3Uploader.uploadFile("upload/course_notice", file.getInputStream(),
                        file.getOriginalFilename());
                    log.info("파일 서버 저장 성공");

                    // 받은 파일 dto에 세팅
                    FileDTO fileDTO = FileDTO.builder()
                        .originalName(file.getOriginalFilename())
                        .newName(insertPath.substring(insertPath.lastIndexOf("/") + 1))
                        .path(insertPath)
                        .size((int) file.getSize())
                        .tableName("course_notice")
                        .tableId(courseBoardMaterialsDTO.getId())
                        .build();

                    // 첨부 파일 db에 저장
                    int fileInsert = utilService.insertService(fileDTO);
                    if (fileInsert == 1) {
                        log.info("파일 db에 저장 성공: {}", fileDTO);
                    } else {
                        log.warn("<<<<< 파일 DB 저장 실패! service의 반환값: {}, 저장하려던 정보: {} >>>>>", fileInsert, fileDTO);
                    }
                }

        } else {
            // 만약 파일을 받지 못했다면 이 로그가 찍힐 것입니다.
            log.warn(">>>>> 전송된 파일이 없어 파일 처리 블록을 건너뜁니다. 'files' 파라미터가 비어있습니다. <<<<<");
        }

        return ResponseEntity.ok(new MyResponseWithData(200, "수정 완료", courseBoardMaterialsDTO));
    }
    @DeleteMapping("/{materialId}")
    @ResponseBody
    public ResponseEntity<MyResponseWithData> deleteMaterial(@PathVariable("materialId") int materialId) {
        try {
            // 서비스 계층에 구현한 삭제 로직 호출
            courseBoardMaterialsService.deleteCourseBoardMaterials(materialId);

            // 성공 시 JSON 형태로 응답
            return ResponseEntity.ok(new MyResponseWithData(200,"게시글이 삭제되었습니다.",materialId));

        } catch (Exception e) {
            log.error("게시글 삭제 중 오류 발생: id={}", materialId, e);
            // 실패 시 서버 에러 응답
            return ResponseEntity.internalServerError().body(new MyResponseWithData(500,"삭제 중 오류가 발생했습니다.",materialId));
        }
    }

}


