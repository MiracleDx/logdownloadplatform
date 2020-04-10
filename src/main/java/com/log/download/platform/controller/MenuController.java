package com.log.download.platform.controller;

import com.log.download.platform.exception.DataNotFoundException;
import com.log.download.platform.response.ResponseCode;
import com.log.download.platform.response.ResponseResult;
import com.log.download.platform.service.MenuService;
import com.log.download.platform.vo.MenuVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * MenuController
 * 菜单控制器
 * @author Dongx
 * Description:
 * Created in: 2020-03-16 9:40
 * Modified by:
 */
@Slf4j
@ResponseResult
@RestController
public class MenuController {

	@Resource
	private MenuService menuService;
	
	@GetMapping("/getMenu")
	public List<MenuVO> getMenu() {
		if (menuService.menu == null || menuService.menu.size() == 0) {
			throw new DataNotFoundException(ResponseCode.MENU_EXCEL_NOT_FOUND);
		}
		return menuService.menu;
	}
}
