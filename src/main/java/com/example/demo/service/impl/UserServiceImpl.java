package com.example.demo.service.impl;

import org.springframework.stereotype.Service;

import com.example.demo.anno.RedisLock;
import com.example.demo.common.util.ThreadTest;
import com.example.demo.service.UserService;
import com.example.demo.vo.RedisLockResult;
@Service
public class UserServiceImpl implements UserService{
	int n = 500;
	@Override
	public String getUser(String name) {
		try {
			System.out.println(name+"=="+Thread.currentThread().getName()+"调用开始了!");
			Thread.sleep(1000);
			System.out.println(name+"=="+Thread.currentThread().getName()+"调用结束了！");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "yes";
	}

	@Override
	@RedisLock(lockName = "process")
	public RedisLockResult<String> process() throws InterruptedException {
		System.out.println("处理开始了!");
		System.out.println("我先睡了");
		Thread.sleep(2_000L);
		System.out.println("处理结束了！");
		return new RedisLockResult<String>("哈哈");
	}

	@Override
	public void testThread() {
		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + "::" + ThreadTest.get());
	}

	@Override
	//@RedisLock(lockName = "test", acquireTimeout = 5, timeOut = 2)
	public RedisLockResult<String> testRedisLock() {
		System.out.println(Thread.currentThread().getName() + "获得了锁");
		System.out.println(--n);
		return new RedisLockResult<>(Thread.currentThread().getName());
	}

	
	
}
