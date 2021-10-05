package com.supportportal.app.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.supportportal.app.domain.User;
import com.supportportal.app.exception.domain.EmailExistException;
import com.supportportal.app.exception.domain.EmailNotFoundException;
import com.supportportal.app.exception.domain.UserNotFoundException;
import com.supportportal.app.exception.domain.UsernameExistException;

public interface UserService {

	User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException;
	
	List<User> getUsers();
	
	User findUserByUsername(String username);
	
	User findUserByEmail(String email);
	
	User addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;
	
	User updateUser(String currenUsername, String newFirstName,String newLastname, String newUsername,
			String newEmail, String newRole, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;
	
	void deleteUser(long id);
	
	void resetPassword(String email) throws EmailNotFoundException;
	
	User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;
	
}
