package uk.co.home.push.repository;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import uk.co.home.push.domain.CreateUserRequest;
import uk.co.home.push.domain.User;
import uk.co.home.push.status.AlreadyRegisteredException;
import uk.co.home.push.status.DoesNotExistException;

@Component
public class InMemoryUserRepository implements UserRepository {
	private static final Logger logger = LogManager.getLogger(InMemoryUserRepository.class);
	private final ConcurrentMap<String, User> userMap;
	
	public InMemoryUserRepository() {
		this.userMap = new ConcurrentHashMap<>(1000);
	}
		
    @Override
    public User getUser(String username) {
        return userMap.get(username);
    }

    @Override
    public List<User> getUsers() {
        return userMap.values().stream().collect(Collectors.toList());
    }

    @Override
    @Retryable(exclude = {DoesNotExistException.class}, value = { RuntimeException.class, IllegalArgumentException.class }, 
    				maxAttempts = 5, backoff = @Backoff(delay = 50))
    public void incrementUserNotificationCount(String username) {
    	User user = userMap.get(username);
    	if (user == null) {
    		throw new DoesNotExistException("The user doesn't exist");
    	}
    	User modifiedUser = User.Builder
        					.createFrom(user)
        					.withNumOfNotificationsPushed(user.getNumOfNotificationsPushed() + 1)
        					.build();
    	
    	boolean modified = userMap.replace(username, user, modifiedUser);
    	logger.info("Incrementing notification count for user {} is {} ", username, modified? "successful": "failed");
    	if (!modified) {
    		throw new RuntimeException("Incrementing notification count failure");
    	}
    }

    @Override
    public User createUser(CreateUserRequest createUserRequest) {
    	var user = User.Builder
			    			.create()
			    			.withUsername(createUserRequest.getUsername())
			    			.withAccessToken(createUserRequest.getAccessToken())
			    			.withCreationTime(new Date())
			    			.withNumOfNotificationsPushed(0)
			    			.build();
    	
    	if (userMap.putIfAbsent(createUserRequest.getUsername(), user) != null) {
    		throw new AlreadyRegisteredException("The user is already registered");
    	}
    	logger.info("Created user {} ", user.getUsername());
    	return user;
    }

}