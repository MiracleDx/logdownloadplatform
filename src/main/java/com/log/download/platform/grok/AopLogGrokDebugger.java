package com.log.download.platform.grok;

import org.springframework.stereotype.Service;

/**
 * AopLogGrokDebugger
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-19 15:20
 * Modified by:
 */
@Service
public class AopLogGrokDebugger extends AbstractGrokDebugger {
	
	@Override
	public String logExample() {
		return "[2020-01-09 11:25:07,630] [http-nio-19010-exec-2] [INFO] [LAPTOP/10.1.71.XX] [http://10.1.71.1XX:19010] [0201050201] [] [http://localhost:19010/importHealthQuickCaseClaim] [] [35845] [2020-01-09 11:24:31,784] [0:0:0:0:0:0:0:1] [740BM9tQLSgqLZazBNOUA] [0] [740BM9tQLSgqLZazBNOUA]";
	}
	
}
