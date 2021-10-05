package com.supportportal.app.domain;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class HttpResponse {

	@JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone="Africa/Lagos")
	private Date timeStamp;
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	private int httpStatusCode;
	private HttpStatus httpStatus;
	private String reason;
	private String message;
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStaus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public HttpResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {
		this.timeStamp=new Date();
		this.httpStatusCode = httpStatusCode;
		this.httpStatus = httpStatus;
		this.reason = reason;
		this.message = message;
	}
	
	
	
}
