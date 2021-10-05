package com.supportportal.app.domain;

import org.springframework.stereotype.Component;


@Component
public class EmailModel {
	
	private String name;
	private String password;
	private String toEmail;
	private String fromEmail;
	private String subject;
	
	public EmailModel() {
		
	}

	public EmailModel(String name, String password, String toEmail, String fromEmail, String subject) {
		this.name = name;
		this.password = password;
		this.toEmail = toEmail;
		this.fromEmail = fromEmail;
		this.subject = subject;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToEmail() {
		return toEmail;
	}
	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}
	public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	

	
}
