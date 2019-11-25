package com.polaris.core.dto;

import java.io.Serializable;

public class MessageDto implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 属于组件，状态值
	 */
	private String code;


	/**
	 * 消息内容
	 */
	private String message;

	 public  MessageDto(){
		 
	 }
	 
	 public  MessageDto(String code,String message){
	    	this.code = code;
	    	this.message=message;
	  }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

	
}
