package com.example.demo.service;

import com.example.demo.vo.RedisLockResult;

public interface UserService {
	String getUser(String name);

	RedisLockResult<String> process() throws InterruptedException;
}
