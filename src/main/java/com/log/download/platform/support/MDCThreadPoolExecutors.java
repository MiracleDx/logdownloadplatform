package com.log.download.platform.support;

import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * MDCThreadPoolExecutors
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 16:09
 * Modified by:
 */
public class MDCThreadPoolExecutors extends ThreadPoolTaskExecutor {

	private boolean useFixedContext = false;
	
	private Map<String, String> fixedContext;

	public MDCThreadPoolExecutors() {
		super();
	}


	private Map<String, String> getContextForTask() {
		return useFixedContext ? fixedContext : MDC.getCopyOfContextMap();
	}

	@Override
	public void execute(Runnable command) {
		super.execute(wrapExecute(command, getContextForTask()));
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return super.submit(wrapSubmit(task, getContextForTask()));
	}

	private <T> Callable<T> wrapSubmit(Callable<T> task, final Map<String, String> context) {
		return () -> {
			Map<String, String> previous = MDC.getCopyOfContextMap();
			if (context == null) {
				MDC.clear();
			} else {
				MDC.setContextMap(context);
			}
			try {
				return task.call();
			} finally {
				if (previous == null) {
					MDC.clear();
				} else {
					MDC.setContextMap(previous);
				}
			}
		};
	}

	private Runnable wrapExecute(final Runnable runnable, final Map<String, String> context) {
		return () -> {
			Map<String, String> previous = MDC.getCopyOfContextMap();
			if (context == null) {
				MDC.clear();
			} else {
				MDC.setContextMap(context);
			}
			try {
				runnable.run();
			} finally {
				if (previous == null) {
					MDC.clear();
				} else {
					MDC.setContextMap(previous);
				}
			}
		};
	}
}
