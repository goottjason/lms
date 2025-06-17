package com.goott5.lms.courseboardmaterials.controller;

import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsDetailInfo;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPageDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingRequestDTO;
import com.goott5.lms.courseboardmaterials.domain.CourseBoardMaterialsPagingResponseDTO;
import com.goott5.lms.courseboardmaterials.service.CourseBoardMaterialsService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/courseBoardMaterials")
public class CourseBoardMaterialsController {

    private final CourseBoardMaterialsService service;

    @GetMapping("/materialsList")
    public String materialsList(
        @ModelAttribute("requestDTO") CourseBoardMaterialsPagingRequestDTO courseBoardMaterialsPagingRequestDTO, Model model , HttpSession session) {

        if (courseBoardMaterialsPagingRequestDTO.getPageNo() == 0) { // int 기본값은 0
            courseBoardMaterialsPagingRequestDTO.setPageNo(1);
        }
        if (courseBoardMaterialsPagingRequestDTO.getPagingSize() == 0) {
            courseBoardMaterialsPagingRequestDTO.setPagingSize(10);
        }


        log.info("pagingRequestDTO={}", courseBoardMaterialsPagingRequestDTO);

        CourseBoardMaterialsPagingResponseDTO<CourseBoardMaterialsPageDTO> responseDTO = service.getListWithSearch(courseBoardMaterialsPagingRequestDTO);

        log.info("responseDTO={}", responseDTO.getDtoList());

        if (courseBoardMaterialsPagingRequestDTO.getKeyword() == null || courseBoardMaterialsPagingRequestDTO.getKeyword().isEmpty()) {
            session.setAttribute("keyword", courseBoardMaterialsPagingRequestDTO.getKeyword());
        }

        model.addAttribute("responseDTO", responseDTO);

        model.addAttribute("pagingRequestDTO", courseBoardMaterialsPagingRequestDTO);

        log.info("pageNo={}", courseBoardMaterialsPagingRequestDTO.getPageNo());
        log.info("pagingSize={}", courseBoardMaterialsPagingRequestDTO.getPagingSize());

        // 로그인 사용자 타입 가져오기 (NullPointerException 방지)
        UserVO loginUser = (UserVO) session.getAttribute("loginUser");
        String loginUserType = "ANONYMOUS"; // 기본값: 로그인하지 않은 사용자

        if (loginUser != null && loginUser.getType() != null) {
            loginUserType = loginUser.getType(); // 세션에서 가져온 사용자 타입
        }

        model.addAttribute("loginUserType", loginUserType); // 변수명을 loginUserType 으로 통일
        log.info("loginUserType={}", loginUserType);


        return "courseBoardMaterials/materialsList";

    }

    @GetMapping("/materialsRegister")
    public String materialsRegister() {
        return "courseBoardMaterials/materialsRegister";
    }

    @GetMapping("/materialsDetail")
    public String materialsDetail(@RequestParam("id") int id, Model model, HttpSession session,
        @ModelAttribute("pagingRequestDTO") CourseBoardMaterialsPagingRequestDTO pagingRequestDTO) {

        log.info("상세 페이지 요청 ID : {}", id);
        log.info("상세 페이지 PagingRequestDTO: {}", pagingRequestDTO);

        CourseBoardMaterialsDetailInfo detail = service.getCourseBoardMaterialsDetail(id);

        if (detail == null) {
            log.warn("ID가 NULL {} ", id);
            // 게시글이 없는 경우 에러 페이지 또는 목록 페이지로 리다이렉트
            return "redirect:/courseBoardMaterials/materialsList" + pagingRequestDTO.generateLinkExceptPageNo();
        }

        model.addAttribute("detail", detail);

        // 로그인 사용자 타입 가져오기
        UserVO loginUser = (UserVO) session.getAttribute("loginUser");
        String loginUserType = "ANONYMOUS";

        if (loginUser != null && loginUser.getType() != null) {
            loginUserType = loginUser.getType();
        }
        model.addAttribute("loginUserType", loginUserType);
        model.addAttribute("loginUserId", loginUser != null ? loginUser.getId() : null); // 로그인한 사용자의 ID도 함께 넘김 (댓글 수정/삭제 시 필요)

        log.info("상세 불러오기 : {}", detail);
        log.info("상세 페이지를 불러오는 loginUserType: {}", loginUserType);

        return "courseBoardMaterials/materialsDetail";


    }

    @GetMapping("/materialsModify")
    public String materialsModify() {
        return "courseBoardMaterials/materialsModify";
    }


}


