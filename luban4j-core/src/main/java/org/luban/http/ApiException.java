package org.luban.http;

import org.luban.common.Assert;

public class ApiException extends Exception {
	private String code;

	public ApiException(String message) {
		super(message);
		Assert.notNull(message,"ApiException 'message'不能为空");
	}

	public ApiException(String code, String message) {
		super(message);
		this.code = code;
		Assert.notNull(message,"ApiException 'message'不能为空");
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
