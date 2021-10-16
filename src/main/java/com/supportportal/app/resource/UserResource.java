package com.supportportal.app.resource;

import com.supportportal.app.exception.domain.UserNotFoundException;
import com.supportportal.app.exception.domain.UsernameExistException;
import com.supportportal.app.service.UserService;
import com.supportportal.app.utility.JWTTokenProvider;

import freemarker.template.TemplateException;

import com.supportportal.app.domain.HttpResponse;
import com.supportportal.app.domain.User;
import com.supportportal.app.domain.UserPrincipal;
import com.supportportal.app.exception.domain.EmailExistException;
import com.supportportal.app.exception.domain.EmailNotFoundException;
import com.supportportal.app.exception.domain.ExceptionHandling;
import static com.supportportal.app.constant.SecurityConstant.JWT_TOKEN_HEADER;

import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpStatus.OK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import static com.supportportal.app.constant.EmailConstant.EMAIL_SENT;
import static com.supportportal.app.constant.UserImplConstant.USER_DELETED;
import static com.supportportal.app.constant.FileConstant.USER_FOLDER;
import static com.supportportal.app.constant.FileConstant.FORWARD_SLASH;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static com.supportportal.app.constant.FileConstant.TEMP_PROFILE_IMAGE_BASE_URL;

@RestController
@RequestMapping(path={"/","/user"})
public class UserResource extends ExceptionHandling{
	
	
	private UserService userService;
	private AuthenticationManager loginAuthManager;
	private JWTTokenProvider jwtTokenProvider;
	
	@Autowired
	public UserResource(UserService userService,AuthenticationManager loginAuthManager,JWTTokenProvider jwtTokenProvider) {
		this.userService=userService;
		this.loginAuthManager=loginAuthManager;
		this.jwtTokenProvider=jwtTokenProvider;
	}

	@GetMapping("/index")
	public String showUser() {
		return "Root of application.";
	}
	
	@GetMapping("/home")
	public String exceptionThrow() throws UserNotFoundException{
		throw new UserNotFoundException("The user was not found.");
	}
	
	@PostMapping("/register")
	public ResponseEntity<User> registerUser(@RequestBody User user) throws UserNotFoundException, UsernameExistException, EmailExistException{
		User newUser= userService.register(user.getFirstName(),user.getLastName(),user.getUsername(),user.getEmail());
		return new ResponseEntity<>(newUser,OK);
	}
	
	@PostMapping("/add")
	public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,@RequestParam("lastName") String lastName,
			@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("role") String role,
			@RequestParam("isNonLocked") boolean isNonLocked, @RequestParam("isActive") boolean isActive,
			@RequestParam(value="profileImage", required=false ) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException{

		User newUser = userService.addNewUser(firstName, lastName, username, email, role, isNonLocked, isActive,
												profileImage);
		return new ResponseEntity<>(newUser, OK);
		
	}
	
	
	@PutMapping("/update")
	public ResponseEntity<User> updateUser(@RequestParam("currentUsername") String currentUsername,@RequestParam("newFirstName") String newFirstName,
			@RequestParam("newLastName") String newLastName, @RequestParam("newUsername") String newUsername,
			@RequestParam("newEmail") String newEmail, @RequestParam("newRole") String newRole,
			@RequestParam("isNonLocked") boolean isNonLocked, @RequestParam("isActive") boolean isActive,
			@RequestParam(value="profileImage", required=false ) MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException{

		
		User updatedUser = userService.updateUser(currentUsername, newFirstName, newLastName, newUsername,
				newEmail, newRole, isNonLocked, isActive, profileImage);
		return new ResponseEntity<>(updatedUser, OK);
		
	}
	
	@GetMapping("/find/{username}")
	public ResponseEntity<User> getUserByUsername(@PathVariable("username") String username){
		User user= userService.findUserByUsername(username);
		return new ResponseEntity<>(user, OK);
	}

	@GetMapping("/findEmail/{email}")
	public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email){
		User user= userService.findUserByEmail(email);
		return new ResponseEntity<>(user, OK);
	}

	@GetMapping("/find/all")
	public ResponseEntity<List<User>> getUsers(){
		List<User> users= userService.getUsers();
		return new ResponseEntity<>(users, OK);
	}

	@GetMapping("/resetPassword/{email}")
	public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException{
		userService.resetPassword(email);
		return response(OK, EMAIL_SENT+email);
	}
	
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasAnyAuthority('user:delete')")
	public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id){
		userService.deleteUser(id);
		return response(NO_CONTENT, USER_DELETED);
	}
	
	private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message){
		return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, 
				httpStatus.getReasonPhrase().toUpperCase(), message.toUpperCase()),httpStatus);
	}

	@PutMapping("/updateProfileImage")
	public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username,
			@RequestParam("profileImage") MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException{
		User user= userService.updateProfileImage(username,profileImage);
		return new ResponseEntity<>(user, OK);
		
	}
	
	@GetMapping(path="/image/{username}/{fileName}", produces= IMAGE_JPEG_VALUE)
	public byte[] getProfileImage(@PathVariable("username") String username,
			@PathVariable("fileName") String fileName) throws IOException {
		return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH+ fileName));
	}
	
	@GetMapping(path="/image/profile/{username}", produces= IMAGE_JPEG_VALUE)
	public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
		URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL+ username);
		ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
		try (InputStream inputStream= url.openStream()){
			int bytesRead;
			byte[] chunk= new byte[1024];
			while((bytesRead= inputStream.read(chunk)) > 0) {
				byteArrayOutputStream.write(chunk,0,bytesRead);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return byteArrayOutputStream.toByteArray();
	}
	
	@PostMapping("/login")
	public ResponseEntity<User> loginUser(@RequestBody User user){
		authenticate(user.getUsername(), user.getPassword());
		User loginUser= userService.findUserByUsername(user.getUsername());
		UserPrincipal userPrincipal= new UserPrincipal(loginUser);
		HttpHeaders jwtHeader= getJwtHeader(userPrincipal);
		return new ResponseEntity<>(loginUser,jwtHeader,OK);
	}

	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
		HttpHeaders headers= new HttpHeaders();
		headers.add(JWT_TOKEN_HEADER,jwtTokenProvider.createJwtToken(userPrincipal));
		return headers;
	}

	private void authenticate(String username, String password) {
		loginAuthManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
	}

}
