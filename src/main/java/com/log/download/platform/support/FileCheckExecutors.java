package com.log.download.platform.support;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * FileExecutors
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-20 13:56
 * Modified by:
 */
public class FileCheckExecutors {
	
	private static final Logger logger = LoggerFactory.getLogger(FileCheckExecutors.class);
	
	private FileCheckExecutors() {
		
	}
	
	private static class SingletonInstance {
		private static final FileCheckExecutors INSTANCE = new FileCheckExecutors();
	}

	public static FileCheckExecutors getInstance() {
		return FileCheckExecutors.SingletonInstance.INSTANCE;
	}
	
	public ThreadPoolExecutor getExecutor() {
		printStats(executor);
		return executor;
	}
	
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(3,
			3, 60,
			TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(1000),
			new ThreadFactoryBuilder()
			.setNameFormat("FileThread-%d")
			.setUncaughtExceptionHandler((thread, throwable) -> logger.error("ThreadPool {} got exception", thread, throwable))
			.build(),
			new ThreadPoolExecutor.AbortPolicy()
			);
	
	private void printStats(ThreadPoolExecutor threadPool) {
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			logger.info("Pool Size: {}, Active Threads: {}, Number of Tasks Completed: {}, Number of Tasks in Queue: {}", 
					threadPool.getPoolSize(),
					threadPool.getActiveCount(),
					threadPool.getCompletedTaskCount(),
					threadPool.getQueue().size());
		}, 0, 1, TimeUnit.SECONDS);
	}
}
