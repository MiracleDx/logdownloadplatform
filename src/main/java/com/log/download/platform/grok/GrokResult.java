package com.log.download.platform.grok;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * GrokDebugger
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-20 10:25
 * Modified by:
 */
@Data
public class GrokResult {
	
	private Boolean status;
	
	private List<ErrorMsg> errorMsgs;
}
