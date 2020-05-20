package com.log.download.platform.grok;

import java.lang.annotation.*;

/**
 * Order
 *
 * @author Dongx
 * Description:
 * Created in: 2020-05-19 17:22
 * Modified by:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
@interface GrokAttributes {
	
	int order() default -1;
	
	String regularKey() default "";

	String errMsg() default "";
}
