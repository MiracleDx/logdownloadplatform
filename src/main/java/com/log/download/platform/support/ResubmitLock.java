package com.log.download.platform.support;

import com.log.download.platform.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * ResubmitLock
 * 重复提交锁
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 16:42
 * Modified by:
 */
public class ResubmitLock {
	
	private static final Logger logger = LoggerFactory.getLogger(ResubmitLock.class);
	
	private static final ConcurrentHashMap<String, Object> LOCK_CACHE = new ConcurrentHashMap<>(200);

	/**
	 * 线程池
	 */
	private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(
			1, (r) -> new Thread(r, "resubmit-thread"), new ThreadPoolExecutor.DiscardPolicy());

	private ResubmitLock() {
	}

	/**
	 * 静态内部类 单例模式
	 *
	 * @return
	 */
	private static class SingletonInstance {
		private static final ResubmitLock INSTANCE = new ResubmitLock();
	}

	public static ResubmitLock getInstance() {
		return SingletonInstance.INSTANCE;
	}


	public static String handleKey(String param) throws NoSuchAlgorithmException {
		return MD5Util.getMD5(param == null ? "" : param);
	}

	/**
	 * 加锁 putIfAbsent 是原子操作保证线程安全
	 *
	 * @param key   对应的key
	 * @param value
	 * @return
	 */
	public boolean lock(final String key, Object value) {
		return Objects.isNull(LOCK_CACHE.putIfAbsent(key, value));
	}

	/**
	 * 延时释放锁 用以控制短时间内的重复提交
	 *
	 * @param lock         是否需要解锁
	 * @param key          对应的key
	 * @param delaySeconds 延时时间
	 */
	public void unLock(final boolean lock, final String key, final int delaySeconds) {
		if (lock) {
			EXECUTOR.schedule(() -> {
				LOCK_CACHE.remove(key);
				logger.info("remove resubmit key {}", key);
			}, delaySeconds, TimeUnit.SECONDS);
		}
	}
}
