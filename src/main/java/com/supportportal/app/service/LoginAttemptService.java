package com.supportportal.app.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.ExecutionException;

@Service
public class LoginAttemptService {
	private static final int MAXIUM_NUMBER_OF_ATTEMPTS=5;
	private static final int ATTEMPT_INCREMENT=1;	
	private LoadingCache<String, Integer> loginAttemptCache;
	
	//Initializing Cache
	public LoginAttemptService() {
		super();
		loginAttemptCache= CacheBuilder.newBuilder().expireAfterWrite(15,MINUTES)
				.maximumSize(100).build(new CacheLoader<String,Integer>(){
					public Integer load(String key) {
						return 0;
					}
				});
	}
	
	public void removeUserFromLoginAttemptCache(String username) {
		loginAttemptCache.invalidate(username);
	}
	
	public void addUserToLoginAttemptCache(String username){
		int attempts=0;
		try {
			attempts=ATTEMPT_INCREMENT + loginAttemptCache.get(username);
			loginAttemptCache.put(username,attempts);
		}catch(ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean hasExceedMaxAttempts(String username){
		try{
			return loginAttemptCache.get(username) >= MAXIUM_NUMBER_OF_ATTEMPTS;
		} catch(ExecutionException e){
			e.printStackTrace();
		}
		return false;
	}


}
