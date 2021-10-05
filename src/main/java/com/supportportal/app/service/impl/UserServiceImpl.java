package com.supportportal.app.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.supportportal.app.constant.FileConstant.USER_FOLDER;
import static com.supportportal.app.constant.FileConstant.DIRECTORY_CREATED;
import static com.supportportal.app.constant.FileConstant.EXTENSION;
import static com.supportportal.app.constant.FileConstant.FILE_SAVED_IN_FILE_SYSTEM;
import static com.supportportal.app.constant.FileConstant.USER_IMAGE_PATH;
import static com.supportportal.app.constant.FileConstant.FORWARD_SLASH;
import static com.supportportal.app.constant.FileConstant.BACK_SLASH;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import javax.transaction.Transactional;
import static com.supportportal.app.constant.UserImplConstant.NO_USER_FOUND_BY_EMAIL;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.supportportal.app.service.LoginAttemptService;
import com.supportportal.app.service.SpringEmailService;
import com.supportportal.app.domain.User;
import com.supportportal.app.domain.UserPrincipal;
import com.supportportal.app.enumeration.Role;
import com.supportportal.app.exception.domain.EmailExistException;
import com.supportportal.app.exception.domain.EmailNotFoundException;
import com.supportportal.app.exception.domain.UserNotFoundException;
import com.supportportal.app.exception.domain.UsernameExistException;
import com.supportportal.app.repository.UserRepository;
import static com.supportportal.app.enumeration.Role.*;
import com.supportportal.app.service.UserService;
import static com.supportportal.app.constant.UserImplConstant.*;
import static com.supportportal.app.constant.FileConstant.DEFAULT_USER_IMAGE_PATH;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService{
	
	private Logger LOGGER= LoggerFactory.getLogger(getClass()); 
	private UserRepository userRepo;
	private BCryptPasswordEncoder passwordEncoder;
	private LoginAttemptService loginAttemptService; 
	private SpringEmailService emailService;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepo, BCryptPasswordEncoder passwordEncoder,
			LoginAttemptService loginAttemptService, SpringEmailService emailService) {
		this.userRepo= userRepo;
		this.passwordEncoder=passwordEncoder;
		this.loginAttemptService= loginAttemptService;
		this.emailService= emailService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepo.findUserByUsername(username);
		
		if(user==null) {
			LOGGER.error(NO_USER_FOUND_BY_USERNAME+username);
			throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME+username);
		}
		else {
			validateLoginAttempt(user);
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			userRepo.save(user);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			LOGGER.info(USER_FOUND+username);
			return userPrincipal;
		}
		
	}

	private void validateLoginAttempt(User user) {
		if(user.isNotLocked()) {
			if(loginAttemptService.hasExceedMaxAttempts(user.getUsername())) {
				user.setNotLocked(false);
			} else {
				user.setNotLocked(true);
			}
		}else {
			loginAttemptService.removeUserFromLoginAttemptCache(user.getUsername());
		}
	}

	@Override
	public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException,UsernameExistException,EmailExistException{
		validateNewUsernameAndEmail(EMPTY,username,email);
		
		User user = new User();
		
		user.setUserId(generateUserId());
		String password= generateUserPassword();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setSignUpDate(new Date());
		user.setPassword(encodePassword(password));
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(ROLE_USER.name());
		user.setAuthorities(ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageURL(username));
		
		userRepo.save(user);
		
		LOGGER.info("The new user password is "+password);
		emailService.sendNewPasswordByEmail(firstName+' '+lastName,password,email);
		
		return user;
	}

	private String getTemporaryProfileImageURL(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH
				+ username)
				.toUriString();
	}

	private String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}

	private String generateUserPassword() {
		return RandomStringUtils.randomAlphanumeric(10);
	}

	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);
	}

	private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
											throws UserNotFoundException,UsernameExistException,EmailExistException{
		User newUserByUsername= findUserByUsername(newUsername);
		User newUserByEmail= findUserByEmail(newEmail);
		if(isNotBlank(currentUsername)) {
			User currentUser = findUserByUsername(currentUsername);
			if(currentUser==null) {
				throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME+currentUsername);
			}
			
			if(newUserByUsername != null && !currentUser.getId().equals(newUserByUsername.getId())) {
				throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
			}
			
			if(newUserByEmail != null && !currentUser.getId().equals(newUserByEmail.getId())) {
				throw new EmailExistException(EMAIL_ALREADY_EXISTS);
			}
			return currentUser;
		}
		else {//for new user creation
			if(newUserByUsername!= null) {
				throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
			}
			if(newUserByEmail != null) {
				throw new EmailExistException(EMAIL_ALREADY_EXISTS);
			}
			return null;
		}
		
	}

	@Override
	public List<User> getUsers() {
		return userRepo.findAll();
	}

	@Override
	public User findUserByUsername(String username) {
		return userRepo.findUserByUsername(username);
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepo.findUserByEmail(email);
	}

	@Override
	public User addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
		validateNewUsernameAndEmail(EMPTY, username, email);	
				
		User user = new User();
		
		String password= generateUserPassword();
		user.setUserId(generateUserId());
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setSignUpDate(new Date());
		user.setPassword(encodePassword(password));
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(getRoleEnumName(role).name());
		user.setAuthorities(getRoleEnumName(role).getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		
		userRepo.save(user);
		saveProfileImage(user, profileImage);
		
		return user;
		
	}

	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
		if (profileImage != null) {
			Path userFolder= Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
			if(Files.exists(userFolder)) {
				Files.createDirectories(userFolder);
				LOGGER.info(DIRECTORY_CREATED + userFolder);
			}
			Files.deleteIfExists(Paths.get(userFolder + user.getUsername()+EXTENSION));
			Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername()+EXTENSION),
					REPLACE_EXISTING);
			user.setProfileImageUrl(setProfileImageUri(user.getUsername()));
			userRepo.save(user);
			LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
		}
	}

	private String setProfileImageUri(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username
				+ "/" + username + EXTENSION).toUriString() ;
	}

	private String getTemporaryProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username)
				.toUriString() ;
	}

	private Role getRoleEnumName(String role) {
		return Role.valueOf(role.toUpperCase());
	}

	@Override
	public User updateUser(String currentUsername, String newFirstName, String newLastname, String newUsername,
			String newEmail, String newRole, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException{

		User currentUser= validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);	
		
		currentUser.setFirstName(newFirstName);
		currentUser.setLastName(newLastname);
		currentUser.setUsername(newUsername);
		currentUser.setEmail(newEmail);
		currentUser.setActive(true);
		currentUser.setNotLocked(true);
		currentUser.setRole(getRoleEnumName(newRole).name());
		currentUser.setAuthorities(getRoleEnumName(newRole).getAuthorities());
		
		userRepo.save(currentUser);
		saveProfileImage(currentUser, profileImage);
		
		return currentUser;
		
	}

	@Override
	public void deleteUser(long id) {
		userRepo.deleteById(id);
	}

	@Override
	public void resetPassword(String email) throws EmailNotFoundException {
		User user= userRepo.findUserByEmail(email);
		if(user== null) {
			throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
		}
		String password= generateUserPassword();
		user.setPassword(encodePassword(password));
		userRepo.save(user);
		emailService.sendNewPasswordByEmail(user.getFirstName()+' '+user.getLastName(), password,
				user.getEmail());
	}

	@Override
	public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
		User user=  validateNewUsernameAndEmail(username, null, null);
		saveProfileImage(user,profileImage);
		return user;
	}
	
	

}
