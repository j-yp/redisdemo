package com.example.demo.aspect;

import java.util.Objects;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.example.demo.common.util.JedisUtil;
import com.example.demo.vo.RedisLockResult;
import com.example.demo.vo.RedisLockResult.RedisLockStatus;

@Aspect
@Component
public class RedisLockAspect {
	@Pointcut(value = "@annotation(com.example.demo.anno.RedisLock)")
	public static void controllerAspect() {}
	
    @SuppressWarnings("unchecked")
	@Around("controllerAspect()")
    public RedisLockResult<String> aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    	RedisLockResult<String> redisLockResult = null;
    	String uuid = UUID.randomUUID().toString().replace("-", "");
		Long setnx = JedisUtil.setnx("redisLock", uuid);
		if(Objects.equals(setnx, 1L)) {
			JedisUtil.expire("redisLock", 10);
			redisLockResult = (RedisLockResult<String>) joinPoint.proceed();
		}
    	
    	String keyValue = JedisUtil.get("redisLock");
		if(Objects.equals(uuid, keyValue)) {
			JedisUtil.del("redisLock");
		}
    	System.out.println("结束!");
    	redisLockResult.setRedisLockStatus(RedisLockStatus.SUCCESS.getCode());
    	return redisLockResult;
    }
}
