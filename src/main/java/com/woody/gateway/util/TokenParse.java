package com.woody.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.io.Deserializer;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.lang.Strings;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by chenwenshun on 2021/12/30
 */
@Component
public class TokenParse {
    public static final char SEPARATOR_CHAR = '.';
    private Decoder<String, byte[]> base64UrlDecoder = Decoders.BASE64URL;

    public Claims parseToken(String jwt){
        String base64UrlEncodedHeader = null;
        String base64UrlEncodedPayload = null;
        String base64UrlEncodedDigest = null;

        int delimiterCount = 0;

        StringBuilder sb = new StringBuilder(128);

        for (char c : jwt.toCharArray()) {

            if (c == SEPARATOR_CHAR) {

                CharSequence tokenSeq = Strings.clean(sb);
                String token = tokenSeq != null ? tokenSeq.toString() : null;

                if (delimiterCount == 0) {
                    base64UrlEncodedHeader = token;
                } else if (delimiterCount == 1) {
                    base64UrlEncodedPayload = token;
                    break;
                }

                delimiterCount++;
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        // =============== Body =================
        String payload = "";
        if (base64UrlEncodedPayload != null) {
            byte[] bytes = base64UrlDecoder.decode(base64UrlEncodedPayload);

            payload = new String(bytes, Strings.UTF_8);
        }

        Claims claims = null;

        if (!payload.isEmpty() && payload.charAt(0) == '{' && payload.charAt(payload.length() - 1) == '}') { //likely to be json, parse it:
            Map<String, Object> claimsMap = (Map<String, Object>) readValue(payload);
            claims = new DefaultClaims(claimsMap);
            return claims;
        }
        return null;
    }
    private Deserializer<Map<String, ?>> deserializer = new JacksonDeserializer<>();
    protected Map<String, ?> readValue(String val) {
        try {
            byte[] bytes = val.getBytes(Strings.UTF_8);
            return deserializer.deserialize(bytes);
        } catch (DeserializationException e) {
            throw new MalformedJwtException("Unable to read JSON value: " + val, e);
        }
    }


    public static void main(String[] args) {
        TokenParse tokenParse = new TokenParse();
        long start = System.currentTimeMillis();
        IntStream.range(0, 100000).forEach(i -> {

            Claims claims = tokenParse.parseToken("eyJraWQiOiJFRjRGMjJDMC01Q0IwLTQzNDgtOTY3Qi0wMjY0OTVFN0VGQzgiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJjb20uemhpaHVpc2h1IiwiYXVkIjoicGMiLCJzdWIiOiJCZWxsYSIsImlhdCI6MTY0MDc1ODMyMywiZXhwIjoxNjQwNzY1NTIzLCJqdGkiOiJmN2UwOWNkYi1hYjI2LTQwYWMtOGUzNC1kODYwZGI4ZmM2YTkiLCJ1aWQiOjE4NzUwMTY2NDgsInJvbGUiOiJhZG1pbiJ9.SW1ufWDK7esUOs0kfEx1M9G4N7L8yszAqDtgun1u3ELZ3nlKCvI_sl0wA_wWBUYldMvm7QuwnZGQzz5INbPm9Q");
            System.out.println(claims.get("uid"));
        });
        System.out.println(System.currentTimeMillis() - start);

    }
}
