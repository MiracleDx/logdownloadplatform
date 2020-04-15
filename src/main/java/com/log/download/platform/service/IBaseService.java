package com.log.download.platform.service;

import com.log.download.platform.dto.DownLoadDTO;

/**
 * IBaseService
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 9:16
 * Modified by:
 */
public interface IBaseService {
    
    /**
     * 调用蓝鲸快速分发接口
     * @param downLoadDTO
     */
    void fastPushFile(DownLoadDTO downLoadDTO);
}
