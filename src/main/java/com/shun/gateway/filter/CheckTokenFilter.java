package com.shun.gateway.filter;

import com.shun.gateway.config.MyFilterConfiguration;
import com.shun.gateway.util.CheckTokenUtil;
import com.shun.gateway.util.CircleBloomFilter;
import com.shun.gateway.util.TokenParse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by chenwenshun on 2022/6/14
 */
@Component
@RequiredArgsConstructor
public class CheckTokenFilter implements GlobalFilter, Ordered {

    public static final String AUTHHEADER = "authorization";
    public static final String USER_ID_KEY = "userId";
    public static final String USER_NAME_KEY = "userName";
    public static final String TRACE_ID = "traceId";

    static final String BODY_401 = " {\n" +
            "  \"code\": 401,\n" +
            "  \"message\": \"Unauthorized\"\n" +
            "}";

    static final String BODY_403 = " {\n" +
            "  \"code\": 403,\n" +
            "  \"message\": \"token expired\"\n" +
            "}";

    private final CheckTokenUtil checkTokenUtil;
    private final TokenParse tokenParse;
    private final CircleBloomFilter circleBloomFilter;
    public static final String PASSED_PREFIX = "passed";
    public static final String STOPPED_PREFIX = "stopped";
    public static final String EXPIRED_PREFIX = "expired";

    private final MyFilterConfiguration myFilterConfiguration;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if (request.getMethod() == HttpMethod.OPTIONS){
            return chain.filter(exchange);
        }
        String traceId = Optional.ofNullable(request.getHeaders().getFirst(TRACE_ID)).orElseGet(this::generateTraceId);
        request.mutate().header(TRACE_ID, traceId);
        //请求路径白名单 判断
        boolean isWhite = checkWhitePath(request.getPath().value());

        String token = request.getHeaders().getFirst(AUTHHEADER);

        if(token == null){
            HttpCookie jwtCookie = request.getCookies().getFirst("jwt");
            token = Optional.ofNullable(jwtCookie).map(HttpCookie::getValue).orElse(null);
        }

        if(token == null){
            if (isWhite){
                return chain.filter(exchange);
            }else {
                return getVoidMono(response, request, HttpStatus.UNAUTHORIZED, BODY_401);
            }
        }

        if (circleBloomFilter.exists(STOPPED_PREFIX.concat(token))){
            if (isWhite){
                return chain.filter(exchange);
            }else {
                return getVoidMono(response, request, HttpStatus.UNAUTHORIZED, BODY_401);
            }
        }

        if (circleBloomFilter.exists(EXPIRED_PREFIX.concat(token))){
            if (isWhite){
                return chain.filter(exchange);
            }else {
                return getVoidMono(response, request, HttpStatus.FORBIDDEN, BODY_403);
            }
        }
        Claims claims = null;
        if (circleBloomFilter.exists(PASSED_PREFIX.concat(token))){
            claims = tokenParse.parseToken(token);
           setHeaders(claims, request.mutate());
            request.mutate().header(AUTHHEADER, token);
            return chain.filter(exchange);
        }

        String finalToken = token;
        return verifyTokenReactive( token)
                .flatMap(claims2 -> {
                    circleBloomFilter.put(PASSED_PREFIX.concat(finalToken));
                    setHeaders(claims2, request.mutate());
                    request.mutate().header(AUTHHEADER, finalToken);
                    return chain.filter(exchange);
                }).onErrorResume(e -> {
                    if (e instanceof ExpiredJwtException){
                        circleBloomFilter.put(EXPIRED_PREFIX.concat(finalToken));
                        if (!isWhite) {return getVoidMono(response, request, HttpStatus.FORBIDDEN, BODY_403);}
                    }else{
                        circleBloomFilter.put(STOPPED_PREFIX.concat(finalToken));
                        if (!isWhite) {
                            return getVoidMono(response, request, HttpStatus.UNAUTHORIZED, BODY_401);
                        }
                    }
                    return chain.filter(exchange); // fallback to white path
                });

    }

    private Mono<Void> getVoidMono(ServerHttpResponse serverHttpResponse, ServerHttpRequest httpRequest, HttpStatus status, String body) {
        HttpHeaders headers = serverHttpResponse.getHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        serverHttpResponse.setStatusCode(status);
        
        DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap(body.getBytes());
        return serverHttpResponse.writeWith(Flux.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private boolean checkWhitePath(String reqPath){
        for (String white : myFilterConfiguration.getWhiteList()) {
            if (pathMatcher.match(white, reqPath)) {
                return true;
            }
        }

        return false;
    }

    private void setHeaders(Claims claims, ServerHttpRequest.Builder builder){
        builder.header(USER_ID_KEY, claims.get("uid").toString());
        String username = Optional.ofNullable(claims.getSubject()).orElse("");
        try {
            builder.header(USER_NAME_KEY, URLEncoder.encode(username, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            builder.header(USER_NAME_KEY, username);
        }
        Set<String> claimsAudience = claims.getAudience();
        if (claimsAudience != null && claimsAudience.size() > 0) {
            builder.header(Claims.AUDIENCE, (String) claimsAudience.toArray()[0]);
        }
    }

    public static void main(String[] args) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        System.out.println(pathMatcher.match("/*", "/testing"));
        System.out.println(pathMatcher.match("/*/**", "/testing/testing"));
        System.out.println(pathMatcher.match("/ms-user/shop/*", "/ms-user/shop/employee/switchToken?shopId=5"));
    }

    private Mono<Claims> verifyTokenReactive(String token) {
        return Mono.fromCallable(() -> checkTokenUtil.check(token)) // 同步逻辑放到 Callable
                .subscribeOn(Schedulers.boundedElastic());       // 放阻塞线程池执行
    }

    // 使用时间戳 + ThreadLocalRandom 生成唯一ID
    private String generateTraceId() {
        return System.currentTimeMillis() + "-" +
                ThreadLocalRandom.current().nextLong(100000, 999999);
    }

}
