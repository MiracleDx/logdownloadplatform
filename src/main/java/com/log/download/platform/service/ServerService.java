package com.log.download.platform.service;

import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.dto.FindMirrorDTO;
import com.log.download.platform.dto.QueryLogDetailDTO;
import com.log.download.platform.vo.LogDetailVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ServerService
 * 微服务日志
 * @author Dongx
 * Description:
 * Created in: 2020-04-13 10:17
 * Modified by:
 */
@Service
public class ServerService implements IBaseService {
	@Override
	public List<LogDetailVO> queryLogDetails(QueryLogDetailDTO queryLogDetailDTO) {
		return null;
	}

	@Override
	public Boolean findMirror(FindMirrorDTO findMirrorDTO) {
		return null;
	}

	@Override
	public void fastPushFile(DownLoadDTO downLoadDTO) {

	}
}
