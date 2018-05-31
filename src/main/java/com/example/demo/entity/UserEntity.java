package com.example.demo.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8921566333766370902L;
	private Object id;
	private String userName;
	private Integer age;
}
