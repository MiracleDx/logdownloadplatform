package com.log.download.platform.util;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.log.download.platform.entity.LogInfo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * UploadListener
 * 模版读取类
 * @author Dongx
 * Description:
 * Created in: 2020-03-13 11:09
 * Modified by:
 */
public class UploadListener extends AnalysisEventListener<LogInfo> {

	/**
	 * 读取到的信息
	 */
	@Getter
	private List<LogInfo> list = new ArrayList<>();

	/**
	 * 每一次数据解析都调用
	 * @param logInfo
	 * @param analysisContext
	 */
	@Override
	public void invoke(LogInfo logInfo, AnalysisContext analysisContext) {
		list.add(logInfo);
	}

	/**
	 * 所有数据解析完成后调用
	 * @param analysisContext
	 */
	@Override
	public void doAfterAllAnalysed(AnalysisContext analysisContext) {

	}
}
