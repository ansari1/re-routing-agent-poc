package com.commerce.agent.client;

import java.net.http.HttpClient;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.commerce.agent.config.ServicesProperties;

/** Single place where every downstream RestClient is configured. */
@Component
public class RestClientFactory {

    private final RestClient.Builder builder;
    private final RemoteErrorTranslator translator;
    private final ServicesProperties properties;

    public RestClientFactory(RestClient.Builder builder, RemoteErrorTranslator translator,
                             ServicesProperties properties) {
        this.builder = builder;
        this.translator = translator;
        this.properties = properties;
    }

    public RestClient create(String serviceName, String baseUrl) {
        var http = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
        var requestFactory = new JdkClientHttpRequestFactory(http);
        requestFactory.setReadTimeout(properties.readTimeout());
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw translator.translate(serviceName, response);
                })
                .build();
    }
}
