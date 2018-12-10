package uk.co.home.push.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.co.home.push.domain.PushNotificationRequest;
import uk.co.home.push.domain.User;
import uk.co.home.push.status.BadRequestException;

@ExtendWith(MockitoExtension.class)
class PushNotificationHandlerTest {

	private static final String A_MESSAGE = "aMessage";
	private static final String A_TITLE = "aTitle";
	private static final String A_USER = "aUser";
	private static final String BODY_SOME_BODY = "{\"body\": \"some body\"}";
	private static final String STATUS_SOME_ERROR = "{\"status\": \"some error\"}";

	private MockWebServer pushApiMock;
	
	private PushNotificationHandler underTest;
	
	@BeforeEach
	void setUp() throws Exception {
		pushApiMock = new MockWebServer();
		pushApiMock.start();
		String hostAndPort = String.format("http://%s:%s", pushApiMock.getHostName(), pushApiMock.getPort());

        WebClient client = WebClient.builder()
        								.baseUrl(hostAndPort)
        								.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE).build();

        underTest = new PushNotificationHandler(client);
	}

    @AfterEach
    void tearDown() throws IOException {
    	pushApiMock.shutdown();
    }

	@Test
	void testPushNotificationSuccess() throws Exception {
		var user = newUser(A_USER, "anAccessToken");
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		pushApiMock.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
											  .setBody(BODY_SOME_BODY));
		
		underTest.pushNotification(user, pushNotificationRequest);

        StepVerifier
        	.create(Mono.just(ResponseEntity.ok(BODY_SOME_BODY)))
        	.expectNext(ResponseEntity.ok(BODY_SOME_BODY))
        	.verifyComplete();
        
        assertRequest();
	}

	@Test
	void testPushNotificationFailsWith4xx() throws Exception {
		var user = newUser(A_USER, "anAccessToken");
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		
		pushApiMock.enqueue(new MockResponse().setResponseCode(401).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
											  .setBody(STATUS_SOME_ERROR));
		
		Executable closureContainingCodeToTest = () -> underTest.pushNotification(user, pushNotificationRequest);
		assertThrows(BadRequestException.class, closureContainingCodeToTest);

        StepVerifier
        	.create(Mono.just(ResponseEntity.status(401).body(STATUS_SOME_ERROR)))
        	.expectNext(ResponseEntity.status(401).body(STATUS_SOME_ERROR))
        	.verifyComplete();
        
        assertRequest();
	}

	@Test
	void testPushNotificationFailsWith5xx() throws Exception {
		var user = newUser(A_USER, "anAccessToken");
		var pushNotificationRequest = PushNotificationRequest.Builder
															 .create().withUsername(A_USER)
															 .withTitle(A_TITLE).withBody(A_MESSAGE).build();
		
		pushApiMock.enqueue(new MockResponse().setResponseCode(500).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
											  .setBody(STATUS_SOME_ERROR));
		
		Executable closureContainingCodeToTest = () -> underTest.pushNotification(user, pushNotificationRequest);
		assertThrows(RuntimeException.class, closureContainingCodeToTest);

        StepVerifier
        	.create(Mono.just(ResponseEntity.status(500).body(STATUS_SOME_ERROR)))
        	.expectNext(ResponseEntity.status(500).body(STATUS_SOME_ERROR))
        	.verifyComplete();
        
        assertRequest();
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

    private void assertRequest() throws InterruptedException {
        RecordedRequest request = pushApiMock.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("/v2/pushes", request.getPath());
    }

}
