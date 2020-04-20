package com.log.download.platform.service;

import cn.hutool.core.io.FileUtil;
import com.log.download.platform.dto.NoticeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NoticeService
 * 公告功能
 * @author Dongx
 * Description:
 * Created in: 2020-04-14 15:28
 * Modified by:
 */
@Slf4j
@Service
public class NoticeService {

	private static final List<String> NOTICE = new ArrayList<>();
	
	@Value("${notice.location}")
	private String noticeLocation;

	/**
	 * 编辑公告栏
	 * @param noticeDTO
	 */
	public void editNotice(NoticeDTO noticeDTO) {
		synchronized (NOTICE) {
			NOTICE.clear();
			NOTICE.addAll(noticeDTO.getMessage());
			FileUtil.writeString(noticeDTO.getMessage().get(0), noticeLocation, "utf-8");
		}
	}

	/**
	 * 获取公告栏内容
	 * @return
	 */
	public List<String> getNotice() {
		if (!new File(noticeLocation).exists()) {
			return new ArrayList<>();
		}
		synchronized (NOTICE) {
			if (NOTICE.size() == 0) {
				synchronized (NOTICE) {
					List<String> strings = FileUtil.readLines(noticeLocation, "utf-8");
					NOTICE.add(String.join("", strings));
				}
			}
		}
		return NOTICE;
	}
}
