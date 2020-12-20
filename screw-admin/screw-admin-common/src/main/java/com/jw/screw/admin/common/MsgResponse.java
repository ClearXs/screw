package com.jw.screw.admin.common;

import java.time.LocalDateTime;

/**
 * 统一返回结果
 * @author jiangw
 * @date 2020/12/20 15:50
 * @since 1.0
 */
public class MsgResponse<T> {

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 返回消息
	 */
	private String msg;

	/**
	 * HTTP 状态码
	 */
	private int code;

	/**
	 * 调用时间
	 */
	private LocalDateTime time;

	/**
	 * 接口数据
	 */
	private T data;

	public MsgResponse() {
		this.time = LocalDateTime.now();
	}

	public MsgResponse(boolean success, int code, String message) {
		this.success = success;
		this.code = code;
		this.msg = message;
		this.time = LocalDateTime.now();
	}

	public MsgResponse(boolean success, int code, T data) {
		this.success = success;
		this.code = code;
		this.data = data;
		this.time = LocalDateTime.now();
	}

	public MsgResponse(boolean success, int code, String msg, T data) {
		this.success = success;
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.time = LocalDateTime.now();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public LocalDateTime getTime() {
		return time;
	}
}
