package com.example.demo.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.entity.UserEntity;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDaoTest {
	@Autowired
	private UserDao userDao;
	
	@Test
	public void testSaveUser() {
		UserEntity userEntity = new UserEntity();
		userEntity.setAge(20);
		userEntity.setId(100000002L);
		userEntity.setUserName("Mike");
		userDao.saveUser(userEntity);
		System.out.println("-----------------------end");
	}
	
	
	@Test
	public void testInsertUser() {
		UserEntity userEntity = new UserEntity();
		userEntity.setAge(23);
		userEntity.setId(100000001L);
		userEntity.setUserName("xiaogang");
		userDao.insertUser(userEntity);
		System.out.println("-----------------------end");
	}
	
	@Test
	public void testFindUserByName() {
		List<UserEntity> list = userDao.findUserEntityByName("iao");
		list.stream().forEach(item -> System.out.println(item));
		System.out.println("-----------------------end");
	}
	
	@Test
	public void testFindAll() {
		List<UserEntity> list = userDao.findAllUser();
		list.stream().forEach(item -> System.out.println(item));
		System.out.println("-----------------------end");
	}
}
