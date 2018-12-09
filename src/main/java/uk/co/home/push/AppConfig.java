package uk.co.home.push;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
@EnableRetry
public class AppConfig {
	private final static String API_URL = "https://api.pushbullet.com";
	
    @Bean
    WebClient webClient() {
		return WebClient.builder()
    					 .baseUrl(API_URL)
    					 .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
    					 .build();
    }
}