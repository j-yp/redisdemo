package com.example.demo.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisPoolConf {
	@Value("${jedis.host}")
	private String host;//服务ip
	@Value("${jedis.port}")
	private int port;//端口号
	@Value("${jedis.max-total}")
	private int maxTotal;//最大连接数  
	@Value("${jedis.max-idle}")
    private int maxIdle;//最大空闲连接数  
	@Value("${jedis.min-idle}")
    private int minIdle;//最小空闲连接数  
	@Value("${jedis.test-on-borrow}")
    private boolean testOnBorrow;//在取连接时测试连接的可用性  
	@Value("${jedis.test-on-return}")
    private boolean testOnReturn;//再还连接时不测试连接的可用性  
	
	
	@Bean(name = "jedisPool")
	public JedisPool initJedisPool() {
		JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setBlockWhenExhausted(true);
        return new JedisPool(config, host, 6379, 5000);
	}
}
