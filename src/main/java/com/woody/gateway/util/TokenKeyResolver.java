package com.woody.gateway.util;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.woody.gateway.filter.CheckTokenFilter.AUTHHEADER;

/**
 * Created by chenwenshun on 2022/8/26
 */
@Component
public class TokenKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String token = exchange.getRequest().getHeaders().getFirst(AUTHHEADER);
        if(StringUtils.isEmpty(token)){
            return Mono.empty();
        }
        int index = token.lastIndexOf(".");
        return Mono.just(token.substring(index + 1));
    }
}
