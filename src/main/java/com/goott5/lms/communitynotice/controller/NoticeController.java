package com.goott5.lms.communitynotice.controller;

import com.goott5.lms.common.domain.FileSelectDTO;
import com.goott5.lms.common.service.UtilService;
import com.goott5.lms.communitynotice.domain.NoticeDTO;
import com.goott5.lms.communitynotice.domain.NoticePagingRequestDTO;
import com.goott5.lms.communitynotice.domain.NoticePagingResponseDTO;
import com.goott5.lms.communitynotice.service.NoticeService;
import com.goott5.lms.user.domain.UserVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/communityNotice")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

  private final NoticeService noticeService;
  private final UtilService utilService;
  private static final String TABLE_NAME = "community_notice";

  @GetMapping("/noticeList")
  public String noticeList(@ModelAttribute("pagingRequestDTO") NoticePagingRequestDTO pagingRequestDTO, Model model, HttpSession session) {
    NoticePagingResponseDTO<NoticeDTO> responseDTO = noticeService.getNoticeList(pagingRequestDTO);
    model.addAttribute("response", responseDTO);

    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (loginUser != null) {
      model.addAttribute("userType", loginUser.getType());
    }
    return "communityNotice/noticeList";
  }

  @GetMapping("/noticeDetail/{id}")
  public String noticeDetail(@PathVariable int id,
      @ModelAttribute("pagingRequestDTO") NoticePagingRequestDTO pagingRequestDTO,
      Model model, HttpSession session) {

    // 서비스의 조회수 로직 호출
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    noticeService.increaseViews(id, loginUser);

    NoticeDTO notice = noticeService.getNotice(id);
    if (notice == null) {
      return "redirect:/communityNotice/noticeList";
    }
    List<FileSelectDTO> files = utilService.selectFileList(TABLE_NAME, id);
    model.addAttribute("notice", notice);
    model.addAttribute("files", files);

    if (loginUser != null) {
      model.addAttribute("userType", loginUser.getType());
    }
    return "communityNotice/noticeDetail";
  }

  @GetMapping("/noticeRegister")
  public String noticeRegisterForm(Model model, HttpSession session, RedirectAttributes rttr) {
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    // 서비스의 isAdmin 메소드 사용
    if (!noticeService.isAdmin(loginUser)) {
      rttr.addFlashAttribute("errorMessage", "권한이 없습니다.");
      return "redirect:/communityNotice/noticeList";
    }
    model.addAttribute("noticeDTO", new NoticeDTO());
    model.addAttribute("pinnedCount", noticeService.getPinnedCount());
    return "communityNotice/noticeRegister";
  }

  @PostMapping("/noticeRegister")
  @ResponseBody
  public ResponseEntity<?> noticeRegisterAjax(@Validated @ModelAttribute NoticeDTO noticeDTO,
      BindingResult bindingResult,
      @RequestParam(value = "files", required = false) List<MultipartFile> files,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");

    if (!noticeService.isAdmin(loginUser)) {
      response.put("message", "권한이 없습니다.");
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    if (bindingResult.hasErrors()) {
      Map<String, String> errors = new HashMap<>();
      for (FieldError error : bindingResult.getFieldErrors()) {
        errors.put(error.getField(), error.getDefaultMessage());
      }
      response.put("message", "입력 값을 확인해주세요.");
      response.put("errors", errors);
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    try {
      noticeDTO.setWriterId(loginUser.getId());
      int result = noticeService.registerNotice(noticeDTO, files);

      if (result == -1) {
        response.put("message", "고정글은 최대 5개까지만 가능합니다.");
        Map<String, String> errors = new HashMap<>();
        errors.put("isFixed", "* 고정글은 최대 5개까지만 가능합니다.");
        response.put("errors", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
      }

      response.put("message", "공지사항이 성공적으로 등록되었습니다.");
      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (IOException e) {
      log.error("파일 업로드 실패", e);
      response.put("message", "파일 업로드 중 오류가 발생했습니다.");
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/noticeModify/{id}")
  public String noticeModifyForm(@PathVariable int id, Model model, HttpSession session, RedirectAttributes rttr) {
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (!noticeService.isAdmin(loginUser)) {
      rttr.addFlashAttribute("errorMessage", "권한이 없습니다.");
      return "redirect:/communityNotice/noticeList";
    }
    NoticeDTO notice = noticeService.getNotice(id);
    List<FileSelectDTO> files = utilService.selectFileList(TABLE_NAME, id);
    model.addAttribute("notice", notice);
    model.addAttribute("files", files);
    model.addAttribute("pinnedCount", noticeService.getPinnedCount());
    return "communityNotice/noticeModify";
  }

  @PostMapping("/noticeModify")
  @ResponseBody
  public ResponseEntity<?> noticeModifyAjax(@Validated @ModelAttribute("notice") NoticeDTO noticeDTO,
      BindingResult bindingResult,
      @RequestParam(value = "addFiles", required = false) List<MultipartFile> addFiles,
      @RequestParam(value = "deleteFiles", required = false) List<Integer> deleteFileNos,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (!noticeService.isAdmin(loginUser)) {
      response.put("message", "권한이 없습니다.");
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    if (bindingResult.hasErrors()) {
      Map<String, String> errors = new HashMap<>();
      for (FieldError error : bindingResult.getFieldErrors()) {
        errors.put(error.getField(), error.getDefaultMessage());
      }
      response.put("message", "입력 값을 확인해주세요.");
      response.put("errors", errors);
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    try {
      int result = noticeService.modifyNotice(noticeDTO, addFiles, deleteFileNos);

      if (result == -1) {
        response.put("message", "고정글은 최대 5개까지만 가능합니다.");
        Map<String, String> errors = new HashMap<>();
        errors.put("isFixed", "* 고정글은 최대 5개까지만 가능합니다.");
        response.put("errors", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
      }

      response.put("message", "공지사항이 성공적으로 수정되었습니다.");
      response.put("noticeId", noticeDTO.getId());
      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (IOException e) {
      log.error("파일 수정 실패", e);
      response.put("message", "파일 처리 중 오류가 발생했습니다.");
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/noticeDelete/{id}")
  public String noticeDelete(@PathVariable int id, HttpSession session, RedirectAttributes rttr) {
    UserVO loginUser = (UserVO) session.getAttribute("loginUser");
    if (!noticeService.isAdmin(loginUser)) {
      rttr.addFlashAttribute("errorMessage", "권한이 없습니다.");
      return "redirect:/communityNotice/noticeList";
    }
    try {
      noticeService.removeNotice(id);
      rttr.addFlashAttribute("successMsg", "공지사항이 삭제되었습니다.");
    } catch (Exception e) {
      log.error("게시글 삭제 중 오류 발생: noticeId={}", id, e);
      rttr.addFlashAttribute("errorMsg", "게시글 삭제 중 오류가 발생했습니다.");
    }
    return "redirect:/communityNotice/noticeList";
  }
}