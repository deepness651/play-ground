package uk.co.home.push.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.co.home.push.domain.PushNotificationRequest;
import uk.co.home.push.handler.PushNotificationHandler;
import uk.co.home.push.repository.UserRepository;
import uk.co.home.push.status.ApiStatus;
import uk.co.home.push.status.BadRequestException;
import uk.co.home.push.status.DoesNotExistException;

@RestController
@RequestMapping(path = "api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.POST })
public class PushController {
    private static final Logger logger = LogManager.getLogger(PushController.class);
    private final UserRepository userRepository;
    private final PushNotificationHandler pushNotificationHandler;

    @Autowired
    public PushController(UserRepository userRepository, PushNotificationHandler pushNotificationHandler) {
        this.userRepository = userRepository;
        this.pushNotificationHandler = pushNotificationHandler;
    }

    @PostMapping(path = "/push")
    public ResponseEntity<?> pushNotification(@RequestBody PushNotificationRequest pushNotificationRequest) {
        final MultiValueMap<String, String> standardCacheHeaders = getCacheHeaders();
        try { // validate request
            if (pushNotificationRequest.getUsername().isBlank() || pushNotificationRequest.getTitle().isBlank()
                    || pushNotificationRequest.getBody().isBlank()) {
                throw new BadRequestException("Bad request");
            }

            var user = userRepository.getUser(pushNotificationRequest.getUsername());
            if (user == null) { // user doesn't exist
                throw new DoesNotExistException("User doesn't exist");
            }

            pushNotificationHandler.pushNotification(user, pushNotificationRequest);
            userRepository.incrementUserNotificationCount(user.getUsername());
            return new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.OK, "Push notification sent"), standardCacheHeaders, HttpStatus.OK);
        } catch (DoesNotExistException | BadRequestException e) {
            logger.error("Status : {}, Error : {}", HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
            return new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()), standardCacheHeaders,
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Status : {}, Error : {}", HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
            return new ResponseEntity<ApiStatus>(new ApiStatus(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage()), standardCacheHeaders,
                    HttpStatus.INTERNAL_SERVER_ERROR);
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
