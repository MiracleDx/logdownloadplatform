package com.log.download.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.bo.JobStatusBO;
import com.log.download.platform.common.BkConstant;
import com.log.download.platform.common.StatusEnum;
import com.log.download.platform.dto.DownLoadDTO;
import com.log.download.platform.exception.DataNotFoundException;
import com.log.download.platform.exception.RemoteAccessException;
import com.log.download.platform.response.ResponseCode;
import com.log.download.platform.service.IBaseService;
import com.log.download.platform.util.BkUtil;
import com.log.download.platform.util.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.regex.Pattern;

/**
 * ServerService
 * 微服务日志
 * @author Dongx
 * Description:
 * Created in: 2020-04-13 10:17
 * Modified by:
 */
@Service("server")
public class ServerServiceImpl implements IBaseService {
	
	@Resource
	private RestTemplate restTemplate;
	
	@Value("${server_container_scriptId}")
	private Integer serverContainerScriptId;

	@Value("${REGEX_IP_ADDR}")
	private String regexIpAddr;

	@Override
	public void fastPushFile(DownLoadDTO downLoadDTO) {
		String label = downLoadDTO.getLabel();
		String ip = downLoadDTO.getIp();
		String path = downLoadDTO.getPath();
		String cvmIp = downLoadDTO.getCvmIp();

		// 获取落盘日志类型
		LogUtil.LogEnum logType = LogUtil.getInstance().logType(path);

		// 蓝鲸工具类实例
		BkUtil bkUtil = BkUtil.getInstance();
		
		// 获取蓝鲸业务ID
		int bkBizId = bkUtil.getBkBizId(label);

		// 容器日志
		if (LogUtil.LogEnum.server_container == logType) {
			// 获取蓝鲸 查询容器IP的参数
			String queryIpParams = bkUtil.getContainerScriptParams(label, ip ,path, serverContainerScriptId);
			Integer jobInstanceId = bkUtil.getJobInstanceId(queryIpParams, restTemplate);
			// 获取脚本执行状态和执行结果
			JobStatusBO queryIpJobStatus = bkUtil.getJobStatus(bkBizId, jobInstanceId, restTemplate);

			// 脚本执行完毕
			if (queryIpJobStatus.getIsFinished()) {
				// 获取容器IP
				JSONObject result = queryIpJobStatus.getResult();
				String containerIp = result.getJSONArray(BkConstant.DATA).getJSONObject(0)
						.getJSONArray(BkConstant.STEP_RESULTS).getJSONObject(0)
						.getJSONArray(BkConstant.IP_LOGS).getJSONObject(0).getString(BkConstant.LOG_CONTENT);
				containerIp = containerIp.replaceAll("\n", "");
				// 校验IP
				if (Pattern.matches(regexIpAddr, containerIp)) {
					ip = containerIp;
					//调用落盘脚本
					String placeParams = bkUtil.getContainerScriptParams(label, ip, path, serverContainerScriptId);
					Integer jobInstanceId1 = bkUtil.getJobInstanceId(placeParams, restTemplate);
					// 获取脚本执行状态和执行结果
					JobStatusBO placeJobStatus = bkUtil.getJobStatus(bkBizId, jobInstanceId1, restTemplate);
					//确定日志是否落盘
					if (!placeJobStatus.getIsFinished()) {
						throw new RemoteAccessException(ResponseCode.REQUEST_TIMEOUT ,"蓝鲸落盘超时");
					}
				} else {
					throw new DataNotFoundException(ResponseCode.DATA_IS_WRONG, "蓝鲸获取容器所在ip错误：" + ip);
				}
			} else {
				throw new RemoteAccessException(ResponseCode.REQUEST_TIMEOUT, "蓝鲸调用执行查询任务超时");
			}
		}
		
		// 容器落盘执行完毕后 和 正常日志处理流程一致
		// 调用日志分发的脚本
		String fastPushFileParams = bkUtil.getFastPushFileParams(bkBizId, ip, path, cvmIp);
		JSONObject jsonObject = bkUtil.requestFastPushFile(fastPushFileParams, restTemplate);
		Integer jobInstanceId = bkUtil.getJobInstanceId(jsonObject);
		// 查询作业脚本的执行状态
		JobStatusBO jobStatus = bkUtil.getJobStatus(bkBizId, jobInstanceId, restTemplate);
		
		// 如果执行完毕
		if (jobStatus.getIsFinished()) {
			JSONObject result = jobStatus.getResult();
			JSONObject data = result.getJSONArray(BkConstant.DATA).getJSONObject(0);
			JSONObject stepResults = data.getJSONArray(BkConstant.STEP_RESULTS).getJSONObject(0);
			String logContent = stepResults.toString();
			if (data.getIntValue(BkConstant.STATUS) == StatusEnum.A3.getCode()) {
				// 判断是否找到文件
				String notExist = "is not exist";
				if (logContent.contains(notExist)) {
					throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND, "蓝鲸能够未查询到该文件");			
				}
			}
		} else {
			throw new DataNotFoundException(ResponseCode.DATA_NOT_FOUND, jobStatus.getResult().getString("message"));
		}
	}
}
