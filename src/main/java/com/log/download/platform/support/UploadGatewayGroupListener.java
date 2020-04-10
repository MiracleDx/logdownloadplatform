package com.log.download.platform.support;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.log.download.platform.entity.DeploymentGroup;
import com.log.download.platform.entity.GatewayGroup;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * UploadGatewayGroupListener
 * 模版读取类
 * @author Dongx
 * Description:
 * Created in: 2020-03-13 11:09
 * Modified by:
 */
public class UploadGatewayGroupListener extends AnalysisEventListener<GatewayGroup> {

	/**
	 * 读取到的信息
	 */
	@Getter
	private List<GatewayGroup> list = new ArrayList<>();

	/**
	 * 每一次数据解析都调用
	 * @param gatewayGroup
	 * @param analysisContext
	 */
	@Override
	public void invoke(GatewayGroup gatewayGroup, AnalysisContext analysisContext) {
		list.add(gatewayGroup);
	}

	/**
	 * 所有数据解析完成后调用
	 * @param analysisContext
	 */
	@Override
	public void doAfterAllAnalysed(AnalysisContext analysisContext) {

	}
}
