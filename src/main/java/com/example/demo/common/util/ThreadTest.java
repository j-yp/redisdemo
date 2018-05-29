package com.example.demo.common.util;

public class ThreadTest {
	private static ThreadLocal<String> threadLocal = new ThreadLocal<>();
	
	public static void set(String param) {
		threadLocal.set(param);
	}
	
	public static String get() {
		return threadLocal.get();
	}
}
