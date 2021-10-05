package com.supportportal.app.filter;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportportal.app.domain.HttpResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.supportportal.app.constant.SecurityConstant.ACCESS_DENIED_MESSAGE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException exception) throws IOException, ServletException {
		
		HttpResponse httpResponse= new HttpResponse(HttpStatus.UNAUTHORIZED.value(),HttpStatus.UNAUTHORIZED,
				HttpStatus.UNAUTHORIZED.getReasonPhrase().toUpperCase(),ACCESS_DENIED_MESSAGE);
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(UNAUTHORIZED.value());
		OutputStream outputStream =response.getOutputStream();
		ObjectMapper mapper= new ObjectMapper();
		mapper.writeValue(outputStream,httpResponse);
		outputStream.flush();
	}

}
