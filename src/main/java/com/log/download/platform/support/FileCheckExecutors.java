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
		executor = new ThreadPoolExecutor(3,
				6, 10,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(15000),
				new ThreadFactoryBuilder()
						.setNameFormat("FileThread-%d")
						.setUncaughtExceptionHandler((thread, throwable) -> logger.error("ThreadPool {} got exception", thread, throwable))
						.build(),
				new ThreadPoolExecutor.AbortPolicy()
		);
		// 启动所有核心线程
		executor.prestartAllCoreThreads();
		// 打印监控数据
		printStats(executor);
	}
	
	private static class SingletonInstance {
		private static final FileCheckExecutors INSTANCE = new FileCheckExecutors();
	}

	public static FileCheckExecutors getInstance() {
		return FileCheckExecutors.SingletonInstance.INSTANCE;
	}

	private static ThreadPoolExecutor executor;
	
	public void execute(Runnable command) {
		executor.execute(command);
	}

	public Future<?> submit(Runnable command) {
		return executor.submit(command); 
	}
	
	private void printStats(ThreadPoolExecutor threadPool) {
		if (logger.isDebugEnabled()) {
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> 
				logger.debug("Pool Size: {}, Active Threads: {}, Number of Tasks Completed: {}, Number of Tasks in Queue: {}",
						threadPool.getPoolSize(),
						threadPool.getActiveCount(),
						threadPool.getCompletedTaskCount(),
						threadPool.getQueue().size())
			, 0, 1, TimeUnit.SECONDS);
		}
	}
}
