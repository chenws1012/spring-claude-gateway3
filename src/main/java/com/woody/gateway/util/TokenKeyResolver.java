package com.woody.gateway.util;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.woody.gateway.filter.CheckTokenFilter.AUTHHEADER;
import static com.woody.gateway.filter.CheckTokenFilter.USER_ID_KEY;

/**
 * Created by chenwenshun on 2022/8/26
 */
@Component
public class TokenKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_KEY);
        String aud    = exchange.getRequest().getHeaders().getFirst(Claims.AUDIENCE);
        if(StringUtils.isEmpty(userId)){
            return Mono.empty();
        }
        return Mono.just(new StringBuilder(aud).append(".").append(userId).toString());
    }
}
