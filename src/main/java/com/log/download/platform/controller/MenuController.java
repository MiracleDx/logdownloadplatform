package com.log.download.platform.controller;

import com.log.download.platform.response.ServerResponse;
import com.log.download.platform.vo.MenuVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuController
 * 菜单控制器
 * @author Dongx
 * Description:
 * Created in: 2020-03-16 9:40
 * Modified by:
 */
@RestController
public class MenuController {

	/**
	 * 菜单树
	 */
	public List<MenuVO> menu = new ArrayList<>();
	
	
	@GetMapping("/getMenu")
	public ServerResponse<List<MenuVO>> getMenu() {
		if (menu == null || menu.size() == 0) {
			return ServerResponse.failure("菜单加载失败，请联系部署组，上传正确的日志清单文件");
		}
		return ServerResponse.success(menu);
	}
}
