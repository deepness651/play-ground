package uk.co.home.push.handler;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import uk.co.home.push.domain.PushNotificationRequest;
import uk.co.home.push.domain.SimplePushNotification;
import uk.co.home.push.domain.User;
import uk.co.home.push.status.BadRequestException;

@Component
public class PushNotificationHandler {
    private static final Logger logger = LogManager.getLogger(PushNotificationHandler.class);
    private final WebClient webClient;

    @Autowired
    public PushNotificationHandler(WebClient webClient) {
        this.webClient = webClient;
    }

    public void pushNotification(User user, PushNotificationRequest pushNotificationRequest) throws ExecutionException, InterruptedException {
        SimplePushNotification simplePushNotification = newSimplePushNotification(pushNotificationRequest);

        logger.info("Sending a push notification to pushbullet API for user {} with title {} ", user.getUsername(),
                simplePushNotification.getTitle());
        Mono<ResponseEntity<String>> pushNotificationResponse = webClient.post().uri("/v2/pushes")
                .body(Mono.just(simplePushNotification), SimplePushNotification.class).header("Access-Token", user.getAccessToken())
                .accept(MediaType.APPLICATION_JSON_UTF8).exchange().flatMap(response -> response.toEntity(String.class)).log();

        ResponseEntity<String> response = pushNotificationResponse.toFuture().get();
        if (response.getStatusCode().is4xxClientError()) {
            throw new BadRequestException(
                    "Push API -> Status : " + response.getStatusCode() + ", Error : " + response.getStatusCode().getReasonPhrase());
        } else if (response.getStatusCode().is5xxServerError()) {
            throw new RuntimeException(
                    "Push API -> Status : " + response.getStatusCode() + ", Error : " + response.getStatusCode().getReasonPhrase());
        }
    }

    private SimplePushNotification newSimplePushNotification(PushNotificationRequest pushNotificationRequest) {
        return SimplePushNotification.Builder.create().withTitle(pushNotificationRequest.getTitle()).withBody(pushNotificationRequest.getBody())
                .build();
    }

}
