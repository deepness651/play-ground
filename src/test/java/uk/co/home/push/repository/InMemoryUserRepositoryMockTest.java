package uk.co.home.push.repository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.co.home.push.domain.User;

@ExtendWith(MockitoExtension.class)
public class InMemoryUserRepositoryMockTest {
    private static final String A_TOKEN = "aToken";
    private static final String A_USER = "aUser";
    
    @Mock
    private ConcurrentMap<String, User> userMap;
    
    @InjectMocks
    private InMemoryUserRepository underTest;

    @BeforeEach
    public void setUp() throws Exception {
        //underTest = new InMemoryUserRepository();
    }

    @Test
    public void testGetUsers() {
        User aUser = newUser(A_USER, A_TOKEN);
        when(userMap.values()).thenReturn(List.of(aUser));
        var users = underTest.getUsers();
        verify(userMap).values();
        Assertions.assertEquals(1, users.size());
    }

    private User newUser(String username, String token) {
        return User.Builder.create().withUsername(username).withAccessToken(token).withCreationTime(new Date()).withNumOfNotificationsPushed(0)
                .build();
    }


}
