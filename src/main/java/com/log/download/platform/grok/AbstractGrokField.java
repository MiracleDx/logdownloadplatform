package com.log.download.platform.grok;

import lombok.Data;

/**
 * AbstractFiledDetail
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-19 17:29
 * Modified by:
 */
@Data
public abstract class AbstractGrokField {

	@GrokAttributes(regularKey = "TIMESTAMP_ISO8601", errMsg = "sdfasdfkjlasdkfj")
	private String timestamp;
}
