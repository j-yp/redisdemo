package com.example.demo.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.example.demo.dao.UserDao;
import com.example.demo.entity.UserEntity;

@Component
public class UserDaoImpl implements UserDao {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void saveUser(UserEntity user) {
		mongoTemplate.save(user);
	}
	
	@Override
	public void insertUser(UserEntity user) {
		mongoTemplate.insert(user);
	}
	
	@Override
	public List<UserEntity> findUserEntityByName(String userName){
		Query query = new Query(Criteria.where("userName").regex(userName));
		return mongoTemplate.find(query, UserEntity.class);
	}
	
	@Override
	public List<UserEntity> findAllUser(){
		return mongoTemplate.findAll(UserEntity.class);
	}
}
