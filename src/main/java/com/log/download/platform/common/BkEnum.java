package com.log.download.platform.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BkEnum
 * 蓝鲸业务id枚举类
 * @author Dongx
 * Description:
 * Created in: 2020-03-17 15:45
 * Modified by:
 */
@Getter
@AllArgsConstructor
public enum BkEnum {
	
	/**
	 * 业务编码，业务id
	 */
	C001("c001", 12),
	C002("c002", 14),
	C006("c006", 81),
	C007("c007", 13),
	C008("c008", 11),
	C009("c009", 6),
	C010("c010", 7),
	C011("c011", 82),
	C012("c012", 16),
	C013("c013", 10),
	C014("c014", 8),
	C015("c015", 84),
	C016("c016", 85),
	P01("p01", 15),
	P02("p02", 19),
	UA("ua", 18);
	
	private String business;
	
	private Integer code;
}
