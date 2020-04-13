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
	public final static String DATA = "data";

	/**
	 * 作业步骤实例id
	 */
	public final static String STEP_INSTANCE_ID = "step_instance_id";

	/**
	 * 作业实例名称
	 */
	public final static String NAME = "name";

	/**
	 * 作业状态码
	 */
	public final static String STATUS = "status";

	/**
	 * 当前步骤下所有IP的日志，按tag或ip分类
	 */
	public final static String STEP_RESULTS = "step_results";

	// step_result

	/**
	 * 主机任务状态码
	 */
	public final static String IP_STATUS = "ip_status";

	/**
	 * job_success/fail返回的标签内容
	 */
	public final static String TAG = "tag";
	
	/**
	 * IP日志内容
	 */
	public final static String IP_LOGS = "ip_logs";

	// ip_logs

	/**
	 * 开始执行时间
	 */
	public final static String START_TIME = "start_time";

	/**
	 * 执行结束时间
	 */
	public final static String END_TIME = "end_time";

	/**
	 * 总耗时 秒
	 */
	public final static String TOTAL_TIME = "total_time";

	/**
	 * 步骤重试次数
	 */
	public final static String RETRY_COUNT = "retry_count";

	/**
	 * 作业中出错码
	 */
	public final static String ERROR_CODE = "error_code";

	/**
	 * shell脚本退出码 0正常 非0异常
	 */
	public final static String EXIT_CODE = "exit_code";

	/**
	 * 云区域id
	 */
	public final static String BK_CLOUD_ID = "bk_cloud_id";

	/**
	 * ip地址
	 */
	public final static String IP = "ip";

	/**
	 * 作业脚本输出日志内容
	 */
	public final static String LOG_CONTENT = "log_content";
	
	// etc.
	
	/**
	 * 脚本执行是否完毕
	 */
	public final static String RESULT = "result";

	/**
	 * 作业是否结束
	 */
	public final static String IS_FINISHED = "is_finished";

	/**
	 * message内容
	 */
	public final static String MESSAGE = "message";

	/**
	 * 容器标识字段
	 */
	public final static String TSF_DEFAULT = "/tsf_default/";

	/**
	 * 作业实例id
	 */
	public final static String JOB_INSTANCE_ID = "job_instance_id";
	
}
