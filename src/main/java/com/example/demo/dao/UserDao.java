package com.example.demo.dao;

import java.util.List;

import com.example.demo.entity.UserEntity;

public interface UserDao {

	void saveUser(UserEntity user);

	void insertUser(UserEntity user);

	List<UserEntity> findUserEntityByName(String userName);

	List<UserEntity> findAllUser();

}
