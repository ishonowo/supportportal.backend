package com.supportportal.app.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import static com.supportportal.app.constant.EmailConstant.FROM_EMAIL;
import static com.supportportal.app.constant.EmailConstant.EMAIL_SUBJECT;

import com.supportportal.app.domain.EmailModel;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class SpringEmailService {

	private JavaMailSenderImpl mailSender;
	private Configuration freemarkerConfig;
	private Environment environment;
	private EmailModel emailModel;
	
	@Autowired
	public SpringEmailService(JavaMailSenderImpl mailSender, Configuration freemarkerConfig,
								Environment environment, EmailModel emailModel) {
		this.mailSender= mailSender;
		this.freemarkerConfig=freemarkerConfig;
		this.environment=environment;
		this.emailModel= emailModel;
	}

	
	public void sendNewPasswordByEmail(String name, String password, String toEmail){
		//initialization
		emailModel.setName(name);
		emailModel.setPassword(password);
		emailModel.setToEmail(toEmail);
		emailModel.setFromEmail(FROM_EMAIL);
		emailModel.setSubject(EMAIL_SUBJECT);
		
		mailSender = new JavaMailSenderImpl();

		mailSender.setHost(environment.getProperty("spring.mail.host"));
		mailSender.setPort(Integer.parseInt(environment.getProperty("spring.mail.port")));
		mailSender.setUsername(environment.getProperty("spring.mail.username"));
		mailSender.setPassword(environment.getProperty("spring.mail.password"));


		try {
		
		MimeMessage msg = mailSender.createMimeMessage();
    	//MimeMessageHelper helper = new MimeMessageHelper(msg, true);
    	MimeMessageHelper helper = new MimeMessageHelper(msg,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);
    	
    	Map<String,EmailModel> emailTemplateModel= new HashMap<String, EmailModel>();
    	emailTemplateModel.put("emailModel",emailModel);
    	helper.setFrom(emailModel.getFromEmail());
    	helper.setTo(emailModel.getToEmail());
    	helper.setSubject(emailModel.getSubject());

    	Template t;
    	String htmlBody="";
		try {
			t = freemarkerConfig.getTemplate("email-template.ftl");
			htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(t, emailTemplateModel );
		} catch (IOException  | TemplateException e) {
			e.printStackTrace();
		}
		System.out.println("The template is" + htmlBody);
		
        helper.setText(htmlBody, true);
        //helper.setText(emailIssue.getBody(),true);
		mailSender.send(msg);
    	} catch(MessagingException ex){
    		Logger.getLogger(SpringEmailService.class.getName()).log(Level.SEVERE, null, ex);
    	}
    }

	
}
