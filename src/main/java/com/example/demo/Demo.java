package com.example.demo;

import java.util.List;

import com.wisdom.util.ConfigureUtil;

public class Demo {
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		List<String> propertyKeyList = ConfigureUtil.getPropertyKeyList();
		
		ThreadLocal<String> tl = new ThreadLocal<>();
		tl.set("123131312");
		System.out.println(getTl());
	}
	
	@SuppressWarnings("unused")
	private static String getTl() {
		ThreadLocal<String> th = new ThreadLocal<>();
		Thread thread = Thread.currentThread();
		return th.get();
	}

	class NewThread extends Thread{
		
	}
}
