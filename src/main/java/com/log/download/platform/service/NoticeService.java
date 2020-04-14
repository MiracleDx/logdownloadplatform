package com.log.download.platform.service;

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
			File file = new File(noticeLocation);
			String content = noticeDTO.getMessage().get(0);
			try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				try {
					fileOutputStream.write(content.getBytes());
				} catch (IOException e) {
					log.error("write notice file error", e);
				}
			} catch (FileNotFoundException e) {
				log.error("notice file not found", e);
			} catch (IOException e) {
				log.error("write notice file error", e);
			}
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
					try {
						try (FileInputStream fileInputStream = new FileInputStream(noticeLocation);
							 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {
							String line = "";
							List<String> str = new ArrayList();
							while ((line = bufferedReader.readLine()) != null) {
								str.add(line);
							}
							NOTICE.add(String.join("", str));
						} catch (FileNotFoundException e) {
							log.error("read notice file not found", e);
						}
					} catch (IOException e) {
						log.error("read notice file error", e);
					}
				}
			}
		}
		return NOTICE;
	}
}
