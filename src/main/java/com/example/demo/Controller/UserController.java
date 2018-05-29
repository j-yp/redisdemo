package com.example.demo.Controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.service.UserService;
import com.example.demo.vo.RedisLockResult;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	
	@RequestMapping("/set")
	@ResponseBody
	public String getUser() {
		try {
			String name = Thread.currentThread().getName();
			for (int i = 0; i < 10; i++) {
				Thread.sleep(200);
				System.out.println(name);
				Thread thread = new Thread() {
					public void run() {
						userService.getUser(name);
					}
				};
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "success";
	}
	
	@RequestMapping("/get")
	@ResponseBody
	public String setUser(HttpServletRequest request) {
		System.out.println("remoteAddr:"+request.getRemoteAddr());
		String ip = request.getHeader("x-forwarded-for");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }   
        System.out.println("ip----------------->"+ip);
        String header = request.getHeader("Host");
        System.out.println(header);
		return header;
	}
	
	@RequestMapping("/redisLock")
	@ResponseBody
	public String setRedisLock() {
		try {
			RedisLockResult<String> result = userService.process();
			System.out.println("controllerResult:"+result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "success";
	}
}
