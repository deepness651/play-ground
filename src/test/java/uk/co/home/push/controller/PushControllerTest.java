package uk.co.home.push.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.co.home.push.domain.PushNotificationRequest;
import uk.co.home.push.domain.User;
import uk.co.home.push.handler.PushNotificationHandler;
import uk.co.home.push.repository.UserRepository;
import uk.co.home.status.ApiStatus;
import uk.co.home.status.BadRequestException;
	
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class PushControllerTest {
    private static final String EXPIRES = "Expires";
	private static final String NO_CACHE = "no-cache";
	private static final String PRAGMA = "Pragma";
	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String CACHE_HEADER_VALUES = "private, max-age=0, s-maxage=0, no-cache, no-store, must-revalidate";
	private static final String A_MESSAGE = "aMessage";
	private static final String A_TITLE = "aTitle";
	private static final String A_USER = "aUser";

	@Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PushNotificationHandler pushNotificationHandler;

	private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	@Test
	public void itShouldPushNotification() throws Exception {
		var user = newUser(A_USER, "anAccessToken");
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		
		when(userRepository.getUser(eq(A_USER))).thenReturn(user);
		doNothing().when(pushNotificationHandler).pushNotification(eq(user), eq(pushNotificationRequest));
		doNothing().when(userRepository).incrementUserNotificationCount(eq(A_USER));
		
		mockMvc.perform(post("/api/v1/push")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(pushNotificationRequest)))
						.andDo(print())
						.andExpect(status().isOk())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.OK, "Push notification sent"))));

		verify(userRepository).getUser(A_USER);
		verify(pushNotificationHandler).pushNotification(user, pushNotificationRequest);
		verify(userRepository).incrementUserNotificationCount(user.getUsername());
	}

	@Test
	public void itShouldBadRequest_MissingValue() throws Exception {
		var pushNotificationRequest = PushNotificationRequest.Builder
										 .create().withUsername("")
										 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		
		mockMvc.perform(post("/api/v1/push")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(pushNotificationRequest)))
						.andDo(print())
						.andExpect(status().isBadRequest())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.BAD_REQUEST, "Bad request"))));
	}

	@Test
	public void itShouldBadRequest_NoSuchUser() throws Exception {
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();

		when(userRepository.getUser(eq(A_USER))).thenReturn(null);
		
		mockMvc.perform(post("/api/v1/push")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(pushNotificationRequest)))
						.andDo(print())
						.andExpect(status().isBadRequest())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.BAD_REQUEST, "User doesn't exist"))));
		
		verify(userRepository).getUser(A_USER);
	}

	@Test
	public void itShouldBadRequest_PushApi() throws Exception {
		var user = newUser(A_USER, "anAccessToken");
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		
		when(userRepository.getUser(eq(A_USER))).thenReturn(user);
		doThrow(BadRequestException.class).when(pushNotificationHandler).pushNotification(eq(user), eq(pushNotificationRequest));
		mockMvc.perform(post("/api/v1/push")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(pushNotificationRequest)))
						.andDo(print())
						.andExpect(status().isBadRequest())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			            .andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.BAD_REQUEST, null))));

		verify(userRepository).getUser(A_USER);
		verify(pushNotificationHandler).pushNotification(user, pushNotificationRequest);
	}

	@Test
	public void itShouldFail_PushApi() throws Exception {
		var user = newUser(A_USER, "anAccessToken");
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		
		when(userRepository.getUser(eq(A_USER))).thenReturn(user);
		doThrow(RuntimeException.class).when(pushNotificationHandler).pushNotification(eq(user), eq(pushNotificationRequest));
		mockMvc.perform(post("/api/v1/push")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(pushNotificationRequest)))
						.andDo(print())
						.andExpect(status().isInternalServerError())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			            .andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.INTERNAL_SERVER_ERROR, null))));

		verify(userRepository).getUser(A_USER);
		verify(pushNotificationHandler).pushNotification(user, pushNotificationRequest);
	}

	@Test
	public void itShouldFail_Increment() throws Exception {
		var user = newUser(A_USER, "anAccessToken");
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		
		when(userRepository.getUser(eq(A_USER))).thenReturn(user);
		doNothing().when(pushNotificationHandler).pushNotification(eq(user), eq(pushNotificationRequest));
		doThrow(new RuntimeException("Incrementing notification count failure")).when(userRepository).incrementUserNotificationCount(eq(A_USER));
		
		mockMvc.perform(post("/api/v1/push")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(pushNotificationRequest)))
						.andDo(print())
						.andExpect(status().isInternalServerError())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			            .andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.INTERNAL_SERVER_ERROR, "Incrementing notification count failure"))));

		verify(userRepository).getUser(A_USER);
		verify(pushNotificationHandler).pushNotification(user, pushNotificationRequest);
		verify(userRepository).incrementUserNotificationCount(user.getUsername());
	}

    @Test
    public void itWill405OnAnyNonGET_DELETE() throws Exception {
        mockMvc.perform(delete("/api/v1/push"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void itWill405OnAnyNonGET_PUT() throws Exception {
        mockMvc.perform(put("/api/v1/push"))
            .andExpect(status().isMethodNotAllowed());
    }
    
	private User newUser(String username, String token) {
		return User.Builder
			.create()
			.withUsername(username)
			.withAccessToken(token)
			.withCreationTime(new Date())
			.withNumOfNotificationsPushed(0)
			.build();
	}
    
}
