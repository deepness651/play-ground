package uk.co.home.push;

import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;
import reactor.netty.tcp.TcpClient;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
@EnableRetry
public class AppConfig {
    private static final Logger logger = LogManager.getLogger(AppConfig.class);
    private final static String API_URL = "https://api.pushbullet.com";

    @Bean
    WebClient webClient() {
        var proxyHost = System.getProperty("https.proxyHost");
        var proxyPort = System.getProperty("https.proxyPort");

        TcpClient tcpClient;
        if (proxyHost != null) {
            logger.error("Setting proxy configuration for : {}", proxyHost);
            tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
                    .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP).address(new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort))))
                    .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(10)).addHandlerLast(new WriteTimeoutHandler(10)));
        } else {
            tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000).noProxy()
                    .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(10)).addHandlerLast(new WriteTimeoutHandler(10)));
        }

        return WebClient.builder().baseUrl(API_URL).clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE).build();
    }
}
