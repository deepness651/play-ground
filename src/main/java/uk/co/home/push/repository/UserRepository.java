package uk.co.home.push.repository;

import java.util.List;

import uk.co.home.push.domain.CreateUserRequest;
import uk.co.home.push.domain.User;

public interface UserRepository {

    User getUser(String username);
    
    void incrementUserNotificationCount(String username);
    
    List<User> getUsers();

	User createUser(CreateUserRequest createUserRequest);
}