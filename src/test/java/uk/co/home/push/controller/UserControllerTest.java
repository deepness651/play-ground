package uk.co.home.push.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

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

import uk.co.home.push.domain.CreateUserRequest;
import uk.co.home.push.domain.User;
import uk.co.home.push.repository.UserRepository;
import uk.co.home.status.AlreadyRegisteredException;
import uk.co.home.status.ApiStatus;
	
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final String EXPIRES = "Expires";
	private static final String NO_CACHE = "no-cache";
	private static final String PRAGMA = "Pragma";
	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String CACHE_HEADER_VALUES = "private, max-age=0, s-maxage=0, no-cache, no-store, must-revalidate";
	private static final String ANOTHER_USER = "anotherUser";
	private static final String AN_ACCESS_TOKEN = "anAccessToken";
	private static final String A_USER = "aUser";

	@Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    
	private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	@Test
	public void itShouldReturnGivenUsers() throws Exception {
		var users = List.of(newUser(A_USER, AN_ACCESS_TOKEN), newUser(ANOTHER_USER, AN_ACCESS_TOKEN));
		
		when(userRepository.getUsers()).thenReturn(users);
		mockMvc.perform(get("/api/v1/users").contentType(MediaType.APPLICATION_JSON))
						.andDo(print())
						.andExpect(status().isOk())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andExpect(content().string(mapper.writeValueAsString(users)));
		
		verify(userRepository).getUsers();
	}

	@Test
	public void itShouldReturnNoUsers() throws Exception {
		List<User> users = List.of();
		
		when(userRepository.getUsers()).thenReturn(users);
		mockMvc.perform(get("/api/v1/users").contentType(MediaType.APPLICATION_JSON))
						.andDo(print())
						.andExpect(status().isNoContent())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.NO_CONTENT, "No users just yet"))));

		verify(userRepository).getUsers();
	}

	@Test
	public void itShouldCreateNewUser() throws Exception {
		var createRequest = CreateUserRequest.Builder
								.create()
								.withUsername(A_USER).withAccessToken(AN_ACCESS_TOKEN)
								.build();
		var newUser = newUser(A_USER, AN_ACCESS_TOKEN);
		when(userRepository.createUser(eq(createRequest))).thenReturn(newUser);
		
		mockMvc.perform(post("/api/v1/users/create")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(createRequest)))
						.andDo(print())
						.andExpect(status().isCreated())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andExpect(content().string(mapper.writeValueAsString(newUser)));
		
		verify(userRepository).createUser(createRequest);
	}

	@Test
	public void itShouldFail_BadRequest() throws Exception {
		var createRequest = CreateUserRequest.Builder
				.create()
				.withUsername("").withAccessToken(AN_ACCESS_TOKEN)
				.build();

		mockMvc.perform(post("/api/v1/users/create")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(createRequest)))
						.andDo(print())
						.andExpect(status().isBadRequest())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			            .andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.BAD_REQUEST, "Bad request"))));
		
		verify(userRepository, never()).createUser(createRequest);
	}

	@Test
	public void itShouldFail_Conflict() throws Exception {
		var createRequest = CreateUserRequest.Builder
				.create()
				.withUsername(A_USER).withAccessToken(AN_ACCESS_TOKEN)
				.build();
		
		when(userRepository.createUser(eq(createRequest))).thenThrow(new AlreadyRegisteredException("The user is already registered"));
		
		mockMvc.perform(post("/api/v1/users/create")
							.contentType(MediaType.APPLICATION_JSON)
							.content(mapper.writeValueAsString(createRequest)))
						.andDo(print())
						.andExpect(status().isConflict())
			            .andExpect(header().string(CACHE_CONTROL, CACHE_HEADER_VALUES))
			            .andExpect(header().string(PRAGMA, NO_CACHE))
			            .andExpect(header().string(EXPIRES, "0"))
			            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			            .andExpect(content().string(mapper.writeValueAsString(new ApiStatus(HttpStatus.CONFLICT, "The user is already registered"))));
		
		verify(userRepository).createUser(createRequest);
	}

    @Test
    public void itWill405OnAnyNonGET_DELETE() throws Exception {
        mockMvc.perform(delete("/api/v1/users"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void itWill405OnAnyNonGET_PUT() throws Exception {
        mockMvc.perform(put("/api/v1/users"))
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
