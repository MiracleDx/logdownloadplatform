package com.log.download.platform.support;

import java.lang.annotation.*;

/**
 * Resubmit
 *
 * @author Dongx
 * Description:
 * Created in: 2020-04-10 16:46
 * Modified by:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Resubmit {

	/**
	 * 延时时间 在延时多久后可以再次提交
	 *
	 * @return Time unit is one second
	 */
	int delaySeconds() default 10;
}
