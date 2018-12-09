package uk.co.home.push.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.co.home.push.domain.CreateUserRequest;
import uk.co.home.push.domain.User;
import uk.co.home.push.repository.UserRepository;
import uk.co.home.status.AlreadyRegisteredException;
import uk.co.home.status.ApiStatus;

@RestController
@RequestMapping(path = "api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = {RequestMethod.GET, RequestMethod.POST})
public class UserController {
	private final UserRepository userRepository;
	

	@Autowired
	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

    @GetMapping(path = "/users")
    public ResponseEntity<?> getUsers() {
    	final MultiValueMap<String, String> standardCacheHeaders = getCacheHeaders();
    	try {
    		var users = userRepository.getUsers();
    		return users.isEmpty()? new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.NO_CONTENT, "No users just yet"), standardCacheHeaders, HttpStatus.NO_CONTENT)
    					: new ResponseEntity<List<User>>(users, standardCacheHeaders, HttpStatus.OK);
		} catch (Exception e) {
	    	return new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage()), 
	    													standardCacheHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }

    @PostMapping(path = "/users/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest) {
    	final MultiValueMap<String, String> standardCacheHeaders = getCacheHeaders();
    	try {
    		// validate request
    		if (createUserRequest.getUsername().isBlank() || createUserRequest.getAccessToken().isBlank()) {
    			return new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.BAD_REQUEST, "Bad request"), 
    															standardCacheHeaders, HttpStatus.BAD_REQUEST);
    		}
    		
    		var user = userRepository.createUser(createUserRequest);
    		return new ResponseEntity<User>(user, standardCacheHeaders, HttpStatus.CREATED);
		} catch (AlreadyRegisteredException e) {
			return new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.CONFLICT, e.getLocalizedMessage()), 
														standardCacheHeaders, HttpStatus.CONFLICT);
	    } catch (Exception e) {
	    	return new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage()), 
	    												standardCacheHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
    }

    private MultiValueMap<String, String> getCacheHeaders() {
    	var headerMap = new LinkedMultiValueMap<String, String>(); 
    	
    	headerMap.add(HttpHeaders.CACHE_CONTROL, "private, max-age=0, s-maxage=0, no-cache, no-store, must-revalidate");
    	headerMap.add(HttpHeaders.PRAGMA, "no-cache");
    	headerMap.add(HttpHeaders.EXPIRES, "0");    	
    	headerMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
    	
    	return headerMap;
    }
}
