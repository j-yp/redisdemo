package com.example.demo.vo;

public class RedisLockResult<T> {
	private String redisLockStatus;
	private T result;

	public String getRedisLockStatus() {
		return redisLockStatus;
	}

	public void setRedisLockStatus(String redisLockStatus) {
		this.redisLockStatus = redisLockStatus;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public RedisLockResult(String redisLockStatus, T result) {
		this.redisLockStatus = redisLockStatus;
		this.result = result;
	}
	
	public RedisLockResult(T result) {
		super();
		this.result = result;
	}

	@SuppressWarnings("rawtypes")
	public static <T> RedisLockResult success(T result) {
		return new RedisLockResult<>(RedisLockStatus.SUCCESS.getCode(), result);
	}
	
	@SuppressWarnings("rawtypes")
	public static <T> RedisLockResult error(T result) {
		return new RedisLockResult<>(RedisLockStatus.ERROR.getCode(), result);
	}
	
	public enum RedisLockStatus{
		SUCCESS("success"),ERROR("error");
		private String code;
		private RedisLockStatus(String code) {
			this.code = code;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
	}
}
