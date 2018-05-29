package com.example.demo.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {
	String lockName();
	//锁超时时间
	int timeOut() default 5;
	//获取超时时间
	int acquireTimeout() default 5;
}
