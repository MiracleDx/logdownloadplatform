package com.log.download.platform.vo;

import lombok.Data;

import java.util.List;

/**
 * MenuVO
 * 菜单
 * @author Dongx
 * Description:
 * Created in: 2020-03-13 11:22
 * Modified by:
 */
@Data
public class MenuVO {
	
	private String label;
	
	private List<MenuVO> children;
}
