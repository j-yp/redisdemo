package com.example.demo.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class JedisUtil {
	
	private static JedisPool jedisPool;
	
	@SuppressWarnings("static-access")
	@Autowired
	private void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}
	
	public static String get(String key) {
		String result = null;
		try(Jedis jedis = jedisPool.getResource()){
			result = jedis.get(key);
		}
		return result;
	}
	
	public static String set(String key, String value) {
		String result = null;
		try(Jedis jedis = jedisPool.getResource()){
			result = jedis.getSet(key, value);
		}
		return result;
	}
	
	public static Long setnx(String key, String value) {
		Long result = null;
		try(Jedis jedis = jedisPool.getResource()){
			result = jedis.setnx(key, value);
		}
		return result;
	}
	
	public static Long del(String key) {
		Long result = null;
		try(Jedis jedis = jedisPool.getResource()) {
			result = jedis.del(key);
		}
		return result;
	}
	
	public static Long expire(String key, int seconds) {
		Long result = null;
		try(Jedis jedis = jedisPool.getResource()) {
			result = jedis.expire(key, seconds);
		}
		return result;
	}

}
