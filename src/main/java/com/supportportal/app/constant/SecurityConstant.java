package com.supportportal.app.constant;

public class SecurityConstant {

	public static final long EXPIRATION_TIME= 172_800_000;
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String JWT_TOKEN_HEADER = "Jwt-Token";
	public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified.";
	public static final String INFINITY_INC = "Infinity INC";
	public static final String ATM_ADMIN = "Issue Resolver Portal";
	public static final String AUTHORITIES = "authorities";
	public static final String FORBIDDEN_MESSAGE = "Kindly login.";
	public static final String ACCESS_DENIED_MESSAGE= "This user is not permitted to use this resource.";
	public static final String OPTIONS_HTTP_METHOD= "OPTIONS";
	public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "user/passwordreset/**",
												"/user/image/**", "/user/home"};//,"**"};
}
