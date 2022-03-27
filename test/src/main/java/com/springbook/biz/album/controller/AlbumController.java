package com.springbook.biz.album.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springbook.biz.album.AlbumService;
import com.springbook.biz.album.AlbumVO;
import com.springbook.biz.albumComment.AlbumCommentService;
import com.springbook.biz.albumComment.AlbumCommentVO;
import com.springbook.biz.board.PageVO;
import com.springbook.biz.user.UserVO;

@Controller
public class AlbumController {
	@Autowired
	AlbumService albumService;

	@Autowired
	AlbumCommentService albumcommentService;

	@RequestMapping("insertAlbum.do")
	public String insertAlbum(AlbumVO vo, Model model, HttpServletRequest request) {
		if (request.getSession().getAttribute("user") != null) {
			if (vo.getAlb_img().getSize() != 0) {

				// 저장할 경로 가져오기
				String path = request.getSession().getServletContext().getRealPath("/");

				File file = new File(path); // 경로생성용 파일 생성

				// 만약 uploadFiles 폴더가 없으면 생성해라 라는뜻
				if (!file.exists())
					file.mkdirs();

				// 업로드할 폴더 설정
				String originFileName = vo.getAlb_img().getOriginalFilename(); // 원래
																				// 파일이름
				String ext = originFileName.substring(originFileName.lastIndexOf("."));
				String ranFileName = UUID.randomUUID().toString() + ext; // 랜덤변수가
																			// 붙은
																			// 파일이름

				File changeFile = new File(path + "/" + ranFileName); // 파일생성

				// 파일업로드
				try {
					vo.getAlb_img().transferTo(changeFile); // 파일 업로드
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();

					return "/insertAlbum.do";
				}

				
				vo.setAlb_img_path(ranFileName); // VO갱신

			}
			
//			UserVO userVO = (UserVO) request.getSession().getAttribute("user");
//			vo.setUser_id(userVO.getUser_Id());
//			vo.setAlb_writer(userVO.getName());
//			System.out.println(userVO.getUser_Id());
//			albumService.insertAlbum(vo);
//			return "home.do";
			
			UserVO userVO = (UserVO) request.getSession().getAttribute("user");
			vo.setUser_id(userVO.getUser_Id());
			vo.setAlb_writer(userVO.getName());
			System.out.println(userVO.getUser_Id());
			int party_id=vo.getParty_id();
			albumService.insertAlbum(vo);
			return "redirect:getAlbumList.do?party_id="+party_id;
		} else {
			return "login.do";
		}

	}

	@RequestMapping(value = "/getAlbumList.do")
	public String getAlbumList(AlbumVO vo, Model model, PageVO page) {
		// vo.getPartId();
		int count = albumService.getAlbumCnt(vo);
		String pageNo = page.getPageNo();
		System.out.println(pageNo);
		int currentPage = 1;
		int listSize = 10;
		int pageSize = 5;
		if (pageNo != null) {
			currentPage = Integer.parseInt(pageNo);
		}
		int startRow = (currentPage - 1) * listSize;

		vo.setStartRow(startRow);
		vo.setListSize(listSize);
		System.out.println("시작번호" + startRow);

		PageVO pages = new PageVO(count, currentPage, listSize, pageSize);
		System.out.println(pages.getTotal());
		System.out.println(count);

		List<AlbumVO> getList = albumService.getAlbumList(vo);
		System.out.println(getList.toString());
		model.addAttribute("albumList", getList);
		model.addAttribute("pages", pages);
		return "albumList.jsp";
	}

	@RequestMapping("getAlbum.do")
	public String getAlbum(AlbumVO vo, AlbumCommentVO vo2, Model model, HttpSession session) {
		UserVO userVO = (UserVO) session.getAttribute("user"); 
		session.setAttribute("user", userVO); 
		model.addAttribute("album", albumService.getAlbum(vo));
		Map<String, AlbumVO> likeList = new HashMap<String, AlbumVO>();

		for (AlbumVO albumVO : albumService.getLikeList(vo)) {
			likeList.put(albumVO.getUser_id(), albumVO);
		}
		System.out.println("맵 테스트 :" + likeList);
		model.addAttribute("likeList", likeList);
		System.out.println("앨범좋아요 리스트 : " + albumService.getLikeList(vo).toString());

		model.addAttribute("commentList", albumcommentService.getAlbumCommentList(vo2));
		System.out.println("댓글리스트 : " + albumcommentService.getAlbumCommentList(vo2));

		return "readAlbum.jsp";
	}

	@RequestMapping("likeUp.do")
	public String likeUp(AlbumVO vo, Model model, HttpSession session) {
		UserVO userVO = (UserVO) session.getAttribute("user");
		if (userVO != null) {
			vo.setUser_id(userVO.getUser_Id());
			albumService.likeUp(vo);
			return "getAlbum.do?alb_id=" + vo.getAlb_id();
		} else {
			return "login.do";
		}
	}

	@RequestMapping("likeDown.do")
	public String likeDown(AlbumVO vo, Model model, HttpSession session) {
		UserVO userVO = (UserVO) session.getAttribute("user");
		vo.setUser_id(userVO.getUser_Id());
		albumService.likeDown(vo);
		return "getAlbum.do?alb_id=" + vo.getAlb_id();
	}

	@RequestMapping("writeAlbumComment.do")
	public String writeAlbumComment(AlbumCommentVO vo, Model model, HttpSession session) {
		UserVO userVO = (UserVO) session.getAttribute("user");
		if (userVO != null) {
			vo.setUser_id(userVO.getUser_Id());
			vo.setUser_name(userVO.getName());
			albumcommentService.writeAlbumComment(vo);
			return "getAlbum.do?alb_id=" + vo.getAlb_id();
		} else {
			return "login.do";
		}
	}

	@RequestMapping("deleteAlbumComment.do")
	public String deleteAlbumComment(AlbumCommentVO vo, Model model, HttpSession session) {
		UserVO userVO = (UserVO) session.getAttribute("user");
		vo.setUser_id(userVO.getUser_Id());
		albumcommentService.deleteAlbumComment(vo);
		return "getAlbum.do?alb_id=" + vo.getAlb_id();
	}

	@RequestMapping("modifyAlbumComment.do")
	public String modifyAlbumComment(AlbumCommentVO vo, Model model, HttpSession session) {
		System.out.println(vo.getAlb_comment_content());
		UserVO userVO = (UserVO) session.getAttribute("user");
		vo.setUser_id(userVO.getUser_Id());
		albumcommentService.modifyAlbumComment(vo);
		return "getAlbum.do?alb_id=" + vo.getAlb_id();
	}

}
