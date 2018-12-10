package uk.co.home.push.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import uk.co.home.push.domain.CreateUserRequest;
import uk.co.home.push.repository.InMemoryUserRepository;
import uk.co.home.push.status.AlreadyRegisteredException;
import uk.co.home.push.status.DoesNotExistException;

@RunWith(JUnitPlatform.class)
public class InMemoryUserRepositoryTest {
	private static final String ANOTHER_TOKEN = "anotherToken";
	private static final String ANOTHER_USER = "anotherUser";
	private static final String A_TOKEN = "aToken";
	private static final String A_USER = "aUser";
	private InMemoryUserRepository underTest;

	@BeforeEach
    public void setUp() throws Exception {
    	underTest = new InMemoryUserRepository();
    }
    
	@Test
	public void testGetUsersNoUsers() {
		var users = underTest.getUsers();
		Assertions.assertEquals(0, users.size());
	}

	@Test
	public void testCreateUser() {
		CreateUserRequest createUserRequest = CreateUserRequest.Builder
												.create()
												.withUsername(A_USER)
												.withAccessToken(A_TOKEN)
												.build();

		var user = underTest.createUser(createUserRequest);
		Assertions.assertEquals(A_USER, user.getUsername());

		var users = underTest.getUsers();
		Assertions.assertEquals(1, users.size());
	}

	@Test
	public void testGetUsersWithTwoUsers() {
		CreateUserRequest createUserRequest = CreateUserRequest.Builder
												.create()
												.withUsername(A_USER)
												.withAccessToken(A_TOKEN)
												.build();

		var user = underTest.createUser(createUserRequest);
		Assertions.assertEquals(A_USER, user.getUsername());

		createUserRequest = CreateUserRequest.Builder
									.create()
									.withUsername(ANOTHER_USER)
									.withAccessToken(ANOTHER_TOKEN)
									.build();

		user = underTest.createUser(createUserRequest);
		Assertions.assertEquals(ANOTHER_USER, user.getUsername());

		var users = underTest.getUsers();
		Assertions.assertEquals(2, users.size());
	}
	
	@Test
	public void testCreateUserFailDuplicateAttempt() {
		CreateUserRequest createUserRequest = CreateUserRequest.Builder
												.create()
												.withUsername(A_USER)
												.withAccessToken(A_TOKEN)
												.build();

		var user = underTest.createUser(createUserRequest);
		Assertions.assertEquals(A_USER, user.getUsername());

		Executable closureContainingCodeToTest = () -> underTest.createUser(createUserRequest);
		assertThrows(AlreadyRegisteredException.class, closureContainingCodeToTest, "This user is already registered");
	}

	@Test
	public void testGetUserWithNoUsers() {
		var userTest = underTest.getUser(A_USER);
		Assertions.assertNull(userTest);
	}

	@Test
	public void testGetUserWithTwoUsers() {
		CreateUserRequest createUserRequest = CreateUserRequest.Builder
												.create()
												.withUsername(A_USER)
												.withAccessToken(A_TOKEN)
												.build();

		var aUser = underTest.createUser(createUserRequest);
		Assertions.assertEquals(A_USER, aUser.getUsername());

		createUserRequest = CreateUserRequest.Builder
									.create()
									.withUsername(ANOTHER_USER)
									.withAccessToken(ANOTHER_TOKEN)
									.build();

		var anotherUser = underTest.createUser(createUserRequest);
		Assertions.assertEquals(ANOTHER_USER, anotherUser.getUsername());

		var userTest = underTest.getUser(A_USER);
		Assertions.assertSame(aUser, userTest);
	}

	@Test
	public void testIncrementNotificationCount() {
		CreateUserRequest createUserRequest = CreateUserRequest.Builder
												.create()
												.withUsername(A_USER)
												.withAccessToken(A_TOKEN)
												.build();

		var user = underTest.createUser(createUserRequest);
		Assertions.assertEquals(A_USER, user.getUsername());

		underTest.incrementUserNotificationCount(A_USER);
		var userTest = underTest.getUser(A_USER);
		
		Assertions.assertNotSame(user, userTest);
		Assertions.assertTrue((user.getNumOfNotificationsPushed() + 1) == userTest.getNumOfNotificationsPushed());
	}

	@Test
	public void testIncrementNotificationCountNoUsers() {
		Executable closureContainingCodeToTest = () -> underTest.incrementUserNotificationCount(A_USER);
		assertThrows(DoesNotExistException.class, closureContainingCodeToTest, "The user doesn't exist");
	}

}
