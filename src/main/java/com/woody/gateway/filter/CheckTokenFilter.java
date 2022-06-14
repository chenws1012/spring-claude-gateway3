package com.woody.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Created by chenwenshun on 2022/6/14
 */
@Component
public class CheckTokenFilter implements GlobalFilter, Ordered {

    public static final String AUTHHEADER = "authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String token = request.getHeaders().getFirst(AUTHHEADER);
        if(token == null){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            String body = " {\n" +
                    "  \"code\": 401,\n" +
                    "  \"message\": \"Unauthorized\"\n" +
                    "}";
            return getVoidMono(response, request, body);
        }
        //todo token校验逻辑
        return chain.filter(exchange);
    }

    private Mono<Void> getVoidMono(ServerHttpResponse serverHttpResponse, ServerHttpRequest httpRequest, String body) {
        HttpHeaders headers = serverHttpResponse.getHeaders();
        headers.add("Access-Control-Allow-Origin", Optional.ofNullable(httpRequest.getHeaders().getFirst("origin")).orElse("*") );
        headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, *");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Content-Type", "application/json;charset=UTF-8");
        
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(body.getBytes());
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
