package com.log.download.platform.controller;

import com.log.download.platform.entity.DeploymentGroup;
import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;

/**
 * UploadController
 * 上传控制器
 * @author Dongx
 * Description:
 * Created in: 2020-03-13 10:37
 * Modified by:
 */
@Slf4j
@Controller
@CrossOrigin
public class UploadController {

	@Value("${excel.location}")
	private String excelLocation;
	
	@Value("${excel.name}")
	private String excelName;
	
	@Resource
	private MenuService menuService;
	
	@PostMapping("upload")
	@ResponseBody
	public ServerResponse<DeploymentGroup> upload(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			log.error("文件上传失败, 上传文件为空");
			return ServerResponse.failure("上传失败");
		}
		
		if (!StringUtils.equals(file.getOriginalFilename(), excelName)) {
			log.error("文件上传失败, 上传文件不正确, {}", file.getOriginalFilename());
			return ServerResponse.failure("上传失败, 请上传正确的文件");
		}

		try (OutputStream outputStream = new FileOutputStream(excelLocation + excelName)) {
			outputStream.write(file.getBytes());
			log.info("默认文件更新成功");
		}
		menuService.getMenu(file.getInputStream());
		return ServerResponse.success();
	}
	
}
