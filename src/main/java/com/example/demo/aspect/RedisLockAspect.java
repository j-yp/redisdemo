package com.example.demo.aspect;

import java.util.List;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.demo.anno.RedisLock;
import com.example.demo.common.util.JedisUtil;
import com.example.demo.vo.RedisLockResult;
import com.example.demo.vo.RedisLockResult.RedisLockStatus;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

@Aspect
@Component
public class RedisLockAspect<T> {
	/**
	 * 这里为@RedisLock注解配置了环绕增强的方法，实现分布式锁的功能，只要添加@RedisLock即可实现分布式锁
	 * @param joinPoint
	 * @param redisLock
	 * @return
	 * @throws Throwable
	 */
    @SuppressWarnings("unchecked")
	@Around("@annotation(redisLock)")
    public RedisLockResult<T> aroundMethod(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
    	RedisLockResult<T> redisLockResult = null;
    	String lockName = "lock" + redisLock.lockName();
    	String retIdentifier = lockWithTimeout(lockName, redisLock.acquireTimeout(), redisLock.timeOut());
    	if(retIdentifier != null) {
    		System.out.println("[redis-lock]: get lock success. The lock is " + retIdentifier);
    		/*
    		 * 执行增强方法代码
    		 */
    		redisLockResult = (RedisLockResult<T>) joinPoint.proceed();
    		if(releaseLock(lockName, retIdentifier)) {
    			redisLockResult.setRedisLockStatus(RedisLockStatus.SUCCESS.name());
    			System.out.println("[redis-lock]: release lock successfully. ");
    		}else {
    			redisLockResult.setRedisLockStatus(RedisLockStatus.ERROR.name());
    			System.out.println("[redis-lock]: release lock unsuccessfully. ");
    		}
    	}else {
    		System.out.println("[redis-lock]: get lock unsuccessfully. ");
    		redisLockResult = RedisLockResult.error();
    	}
    	return redisLockResult;
    }
    
    /**
     * 带超时时间的锁获取
     * @param lockName
     * @param acquireTimeout
     * @param timeout
     * @return
     */
    public String lockWithTimeout(String lockName, int acquireTimeout, int timeout) {
        String retIdentifier = null;
        // 随机生成一个value
        String identifier = UUID.randomUUID().toString().replaceAll("-", "");
        // 获取锁的超时时间，超过这个时间则放弃获取锁
        long end = System.currentTimeMillis() + acquireTimeout * 1000;
        while (System.currentTimeMillis() < end) {
            if (JedisUtil.setnx(lockName, identifier) == 1) {
                JedisUtil.expire(lockName, timeout);
                // 返回value值，用于释放锁时间确认
                retIdentifier = identifier;
                return retIdentifier;
            }
            // 返回-1代表key没有设置超时时间，为key设置一个超时时间
            if (JedisUtil.ttl(lockName) == -1) {
            	JedisUtil.expire(lockName, timeout);
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return retIdentifier;
    }
    
    /**
     * 释放锁
     * @param lockName
     * @param identifier
     * @return
     */
    public boolean releaseLock(String lockName, String identifier) {
    	boolean retFlag = false;
    	try(Jedis jedis = JedisUtil.getJedis();){
	    	while (true) {
	            // 监视lock，准备开始事务
	    		jedis.watch(lockName);
	            // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
	            if (identifier.equals(JedisUtil.get(lockName))) {
	                Transaction transaction = jedis.multi();
	                transaction.del(lockName);
	                List<Object> results = transaction.exec();
	                if (results == null) {
	                    continue;
	                }
	                retFlag = true;
	            }
	            jedis.unwatch();
	            break;
	        }
    	}
        return retFlag;
    }
    
}
