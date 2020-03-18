package com.log.download.platform.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
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

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<String> ips;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String cluster;
	
	private List<MenuVO> children;
}
