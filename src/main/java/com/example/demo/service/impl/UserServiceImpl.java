package com.example.demo.service.impl;

import org.springframework.stereotype.Service;

import com.example.demo.anno.RedisLock;
import com.example.demo.service.UserService;
import com.example.demo.vo.RedisLockResult;
@Service
public class UserServiceImpl implements UserService{

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
	@RedisLock
	public RedisLockResult<String> process() throws InterruptedException {
		System.out.println("处理开始了!");
		System.out.println("我先睡了");
		Thread.sleep(2_000L);
		System.out.println("处理结束了！");
		return new RedisLockResult<String>("哈哈");
	}

}
