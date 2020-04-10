package com.log.download.platform.util;

import com.alibaba.fastjson.JSONObject;
import com.log.download.platform.common.BkEnum;
import com.log.download.platform.dto.HostDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * BkUtil
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 10:29
 * Modified by:
 */
public class BkUtil {

	/**
	 * BKAPP
	 */
	private final String BK_APP_CODE = "logdownder";

	/**
	 * BK秘钥
	 */
	private final String BK_APP_SECRET = "855d69c9-2ed4-4d08-9a88-7a56a2564e12";

	/**
	 * BK用户名
	 */
	private final String BK_USERNAME = "admin";

	/**
	 * 共享服务器地址
	 */
	private final String SHARED_SERVER = "10.155.27.48";

	/**
	 * 调用蓝鲸执行脚本url
	 */
	private final String FAST_EXECUTE_SCRIPT_URL = "http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/fast_execute_script/";

	/**
	 * 调用蓝鲸查询脚本结果url
	 */
	private final String GET_JOB_INSTANCE_LOG_URL = "http://paas.aio.zb.zbyy.piccnet/api/c/compapi/v2/job/get_job_instance_log/";

	
	private BkUtil() {
		
	}
	
	private static class SingletonInstance {
		private static final BkUtil instance = new BkUtil();
	}
	
	public static BkUtil getInstance() {
		return SingletonInstance.instance;
	}
	
	
	/**
	 * 调用蓝鲸接口
	 * @param url
	 * @param params
	 * @param restTemplate
	 * @return
	 */
	public JSONObject requestBkInterface(String url, String params, RestTemplate restTemplate) {
		//请求头
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(params, headers);
		//发送请求调用接口
		ResponseEntity<String> request = restTemplate.postForEntity(url, httpEntity, String.class);
		return JSONObject.parseObject(request.getBody());
	}

	/**
	 * 获取快速执行脚本入参
	 * @param label
	 * @param ips
	 * @param param
	 * @param scriptId
	 * @return
	 */
	public String getFastExecuteScriptParams(String label, String[] ips, String param, int scriptId) {
	
		BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
		int bkBizId = bkEnum.getCode();
		byte[] content = param.getBytes();
		String scriptParam = Base64.getEncoder().encodeToString(content);

		String params = "{\n" +
				"\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
				"\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
				"\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
				"\t\"bk_biz_id\": " + bkBizId + ",\n" +
				"\t\"script_id\": " + scriptId + ",\n" +
				"\t\"script_param\": \"" + scriptParam + "\",\n" +
				"\t\"script_timeout\": 1000,\n" +
				"\t\"account\": \"ubuntu\",\n" +
				"\t\"is_param_sensitive\": 0,\n" +
				"}";
		List<Map<String, Object>> list = new ArrayList<>();
		HostDTO hostDTO = new HostDTO();
		for (String ip : ips) {
			Map<String, Object> hostMap = new HashMap<>();
			hostMap.put("bk_cloud_id", 0);
			hostMap.put("ip", ip);
			list.add(hostMap);
			hostDTO.setIp_list(list);
		}

		String ipListJson = JSONObject.toJSON(hostDTO).toString();
		// 合并两段JSON
		JSONObject json = JSONObject.parseObject(params);
		JSONObject ipJson = JSONObject.parseObject(ipListJson);
		JSONObject resultParam = new JSONObject();
		resultParam.putAll(json);
		resultParam.putAll(ipJson);

		return resultParam.toJSONString();
	}

	/**
	 * 获取脚本执行结果入参
	 * @param label
	 * @param jobInstanceId
	 * @return
	 */
	public String getJobInstanceLogParams(String label, int jobInstanceId) {
		BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
		int bkBizId = bkEnum.getCode();
		String params = "{\n" +
				"\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
				"\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
				"\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
				"\t\"bk_biz_id\": " + bkBizId + ",\n" +
				"\t\"job_instance_id\": " + jobInstanceId + "\n" +
				"}";
		return params;

	}

	/**
	 * 拼接快速分发文件的接口参数
	 * @param label
	 * @param ip
	 * @param path
	 * @param cvmIp
	 * @return
	 */
	public String getFastPushFile(String label, String ip, String path, String cvmIp) {
		BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
		int bkBizId = bkEnum.getCode();
		int end = path.lastIndexOf("/");
		String params = "{\n" +
				"\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
				"\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
				"\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
				"\t\"bk_biz_id\": " + bkBizId + ",\n" +
				"\t\"file_target_path\": \"/tmp/0_" + cvmIp + "/" + path.substring(0, end) + "\",\n" +
				"\t\"account\": \"root\",\n" +
				"\t\"ip_list\": [{\"bk_cloud_id\": 0,\"ip\": \"" + SHARED_SERVER + "\"}],\n" +
				"\t\"file_source\": [{\n" +
				"\t\t\"files\":[\"" + path + "\"],\n" +
				"\t\t\"account\": \"ubuntu\",\n" +
				"\t\t\"ip_list\": [\n";
		String iplist = "\t\t{\n" +
				"\t\t\t\"bk_cloud_id\": 0,\n" +
				"\t\t\t\"ip\": \"" + ip + "\"\n" +
				"\t\t}\n";

		params += iplist;
		params += "\t\t]\n\t}\n]}";
		return params;
	}

	/**
	 * 获取查询容器入参
	 * @param label
	 * @param ips
	 * @param path
	 * @param scriptId
	 * @return
	 */
	public String getContainerScriptParams(String label, String ip, String[] ips, String path, int scriptId) {
		BkEnum bkEnum = BkEnum.valueOf(label.toUpperCase());
		int bk_biz_id = bkEnum.getCode();
		String[] arr = path.split("/");
		path = arr[arr.length - 1];
		String[] paths = path.split("-");
		String namespace = paths[1] + "-" + paths[3];
		String group = paths[1] + "-" + paths[2] + "-" +paths[3] + "-" +paths[4];
		String flag = group + "-" + paths[5] + "-" + paths[6];
		String param = namespace + " " + group + " " + flag + " " + path;
		byte[] content = param.getBytes();
		String script_param = Base64.getEncoder().encodeToString(content);
		String params = "{\n" +
				"\t\"bk_app_code\": \"" + BK_APP_CODE + "\",\n" +
				"\t\"bk_app_secret\": \"" + BK_APP_SECRET + "\",\n" +
				"\t\"bk_username\": \"" + BK_USERNAME + "\",\n" +
				"\t\"bk_biz_id\": " + bk_biz_id + ",\n" +
				"\t\"script_id\": " + scriptId + ",\n" +
				"\t\"script_param\": \"" + script_param + "\",\n" +
				"\t\"script_timeout\": 1000,\n" +
				"\t\"account\": \"ubuntu\",\n" +
				"\t\"is_param_sensitive\": 0,\n" +
				"\t\"ip_list\": [\n" +
				"\t\t{\n" +
				"\t\t\t\"bk_cloud_id\":0,\n" +
				"\t\t\t\"ip\":\""+ ip +"\"\n" +
				"\t\t}\n" +
				"\t]\n" +
				"}";
		return params;
	}
}
