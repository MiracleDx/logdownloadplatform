package com.log.download.platform.controller;

import com.log.download.platform.dto.NoticeDTO;
import com.log.download.platform.response.ResponseResult;
import com.log.download.platform.service.NoticeService;
import com.log.download.platform.support.Resubmit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * NoticeController
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-14 15:00
 * Modified by:
 */
@RestController
@ResponseResult
public class NoticeController {
	
	@Resource
	private NoticeService noticeService;
	
	@Resubmit(delaySeconds = 10)
	@PostMapping("/editNotice")
	public void editNotice(@RequestBody NoticeDTO noticeDTO) {
		noticeService.editNotice(noticeDTO);
	}

	@GetMapping("/getNotice")
	public List<String> getNotice() {
		return noticeService.getNotice();
	}
	
	
}
