package com.log.download.platform.common;

/**
 * BkConstant
 * 蓝鲸调用出现的Json字段名
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 15:36
 * Modified by:
 */
public class BkConstant {

	// data
	
	/**
	 * 数据
	 */
	private final static String DATA = "data";

	/**
	 * 作业步骤实例id
	 */
	private final static String STEP_INSTANCE_ID = "step_instance_id";

	/**
	 * 作业实例名称
	 */
	private final static String NAME = "name";

	/**
	 * 作业状态码
	 */
	private final static String STATUS = "status";

	/**
	 * 当前步骤下所有IP的日志，按tag或ip分类
	 */
	private final static String STEP_RESULTS = "step_results";

	// step_result

	/**
	 * 主机任务状态码
	 */
	private final static String IP_STATUS = "ip_status";

	/**
	 * job_success/fail返回的标签内容
	 */
	private final static String TAG = "tag";
	
	/**
	 * IP日志内容
	 */
	private final static String IP_LOGS = "ip_logs";

	// ip_logs

	/**
	 * 开始执行时间
	 */
	private final static String START_TIME = "start_time";

	/**
	 * 执行结束时间
	 */
	private final static String END_TIME = "end_time";

	/**
	 * 总耗时 秒
	 */
	private final static String TOTAL_TIME = "total_time";

	/**
	 * 步骤重试次数
	 */
	private final static String RETRY_COUNT = "retry_count";

	/**
	 * 作业中出错码
	 */
	private final static String ERROR_CODE = "error_code";

	/**
	 * shell脚本退出码 0正常 非0异常
	 */
	private final static String EXIT_CODE = "exit_code";

	/**
	 * 云区域id
	 */
	private final static String BK_CLOUD_ID = "bk_cloud_id";

	/**
	 * ip地址
	 */
	private final static String IP = "ip";

	/**
	 * 作业脚本输出日志内容
	 */
	private final static String LOG_CONTENT = "log_content";
	
	// etc.
	
	/**
	 * 脚本执行是否完毕
	 */
	private final static String RESULT = "result";

	/**
	 * 作业是否结束
	 */
	private final static String IS_FINISHED = "is_finished";

	/**
	 * message内容
	 */
	private final static String MESSAGE = "message";

	/**
	 * 容器标识字段
	 */
	private final static String TSF_DEFAULT = "/tsf_default/";

	/**
	 * 作业实例id
	 */
	private final static String JOB_INSTANCE_ID = "job_instance_id";
	
}
