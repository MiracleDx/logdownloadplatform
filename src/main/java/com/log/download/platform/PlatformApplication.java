package com.log.download.platform;

import com.log.download.platform.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;

@Slf4j
@SpringBootApplication
public class PlatformApplication implements ApplicationRunner {

	@Value("${excel.location}")
	private String excelLocation;
	
	@Value("${excel.name}")
	private String excelName;
	
	@Resource
	private MenuService menuService;
	
	public static void main(String[] args) {
		SpringApplication.run(PlatformApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws IOException {
		// 没有初始化菜单时，加载指定位置模版
		if (menuService.menu == null || menuService.menu.size() == 0) {
			InputStream in = null;
			File file = new File(excelLocation + excelName);
			if (file.exists()) {
				try {
					in = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				menuService.getMenu(in);
				log.info("默认模版文件读取成功");
			}
		} else {
			log.error("未读取到默认模版文件");
		}
	}
}
