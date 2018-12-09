package uk.co.home.push.repository;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;

import uk.co.home.push.domain.CreateUserRequest;

@RunWith(ConcurrentTestRunner.class)
public class InMemoryUserRepositoryConcurrencyTest {
	
	private InMemoryUserRepository underTest;
	private final static int THREAD_COUNT = 4;
	private static final long start = System.currentTimeMillis();

	@Before
    public void setUp() throws Exception {
    	underTest = new InMemoryUserRepository();
    	CreateUserRequest createUserRequest = CreateUserRequest.Builder
    			.create()
    			.withUsername("aUser")
    			.withAccessToken("aToken")
    			.build();
    	
    	var user = underTest.createUser(createUserRequest);
    	assertEquals("aUser", user.getUsername());
    }
    
	@Test
	@ThreadCount(THREAD_COUNT)
	public void testConcurrencyWithIncrementNotificationCount() throws Exception {
    	var user = underTest.getUser("aUser");
    	println("user " + user);
    	underTest.incrementUserNotificationCount("aUser");
    	user = underTest.getUser("aUser");
        println("value incremented " + user);
	}
	
	@After
	public void countNotifications() throws Exception {
		Thread.sleep(500);
    	var user = underTest.getUser("aUser");
    	assertEquals(THREAD_COUNT, user.getNumOfNotificationsPushed().intValue());
	}
	
    private static void println(String msg) {
        System.out.printf("%s (%d): %s\n", Thread.currentThread().getName(), System.currentTimeMillis()-start, msg);
    }
}
