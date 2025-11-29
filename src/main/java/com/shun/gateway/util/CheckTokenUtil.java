package com.shun.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PublicKey;

@Component
public class CheckTokenUtil {

    private static PublicKey PUBLICKEY;


    @Value("${publicKeyPem}")
    private String publicKeyPem;


    @PostConstruct
    public void init() throws IOException {
        Reader rdr = new StringReader(publicKeyPem);
        Object parsed = new org.bouncycastle.openssl.PEMParser(rdr).readObject();
        PUBLICKEY = new JcaPEMKeyConverter().getPublicKey(((SubjectPublicKeyInfo) parsed));
    }

    public Claims check(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(PUBLICKEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims;
    }

    public static void main(String[] args) throws IOException {
        CheckTokenUtil checkTokenUtil = new CheckTokenUtil();
        checkTokenUtil.publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAElpzANDFRDkLNJ6Ee4iB9hogVXD56\n" +
                "gNchXHXAnuYxLxuNPPBZDvtvMBUToT+L2UiUzusQJYo9oI86GH9NUqJCjQ==\n" +
                "-----END PUBLIC KEY-----";
        checkTokenUtil.publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEbsc9wnZ/dKLbEDZYIXFJ6eO8jfik\n" +
                "XQSGS9fGecCDxpYQA/LyalWZw/5sQ5TLU60rgK7sPC1MGYIATZKUWS9saw==\n" +
                "-----END PUBLIC KEY-----\n";

        checkTokenUtil.publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                "  MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+2tpCIV+PxzwCRUb2/6VzjOkJ/oz\n" +
                "  D+hOyPFKo02R9Oq5EM3QEC3MguBbelPeG72I488c8R9dlK416OVyBBn2gA==\n" +
                "  -----END PUBLIC KEY-----";
        checkTokenUtil.init();

        checkTokenUtil.check("eyJraWQiOiJDV1MiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJjb20ud29vZHkiLCJhdWQiOiJZVU5fTUVOR19TSE9QIiwic3ViIjoi5pil56WlIiwiaWF0IjoxNzI3MjM0NTg4LCJleHAiOjE3Mjk4MjY1ODgsImp0aSI6ImUwOWE1ZDI3LTcwOWItNDBmOS1hZTBmLWU3YWM3OGI2MzQ4NyIsInVpZCI6IjE4MTUyMDMxNDM4MzE2MDUyNDgifQ.LiNQTXe3j3IAitYefmmH9rlZnxSB7FQJQRzFrKXLTAXHLvPK5z2PiWuKeTC3j2GLe4n5c92gRPDNjzg0BtM7mQ");

        Claims check1 = null;
        long start = System.currentTimeMillis();
        CircleBloomFilter circleBloomFilter = new CircleBloomFilter();
        String token = "eyJraWQiOiJFRjRGMjJDMC01Q0IwLTQzNDgtOTY3Qi0wMjY0OTVFN0VGQzgiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJjb20uemhpaHVpc2h1IiwiYXVkIjoicGMiLCJzdWIiOiJCZWxsYSIsImlhdCI6MTY0MTI3NDg2NCwiZXhwIjoxNjQxOTk0ODY0LCJqdGkiOiJjNGE2ODA4ZS0zMjUzLTQxODYtOTFjMC0wZmQ5MWY5MmFiYjMiLCJ1aWQiOjMsInJvbGUiOiJhZG1pbiJ9.4BTqbPYau8EPOLB7Z_5Gj41kZX_UnTeeb0JwYojVSr1NoMK-49W04NWRMA9HebifjwPDotGwKPjZqvk-VWZ1xQ";
        for (int i = 0; i < 1; i++) {
//            if (circleBloomFilter.exists("passed"+token)){
//                System.out.println("passed");
//                continue;
//            }
//
//            if(circleBloomFilter.exists("failed"+token)){
//                System.out.println("failed");
//                continue;
//            }
            try {
                check1 = checkTokenUtil.check(token);
                System.out.println(check1);
                circleBloomFilter.put("passed"+token);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                circleBloomFilter.put("failed"+token);
//                e.printStackTrace();
            }
        }
        System.out.println(System.currentTimeMillis() - start);
//        System.out.println(check1.getSubject());
//        System.out.println(check1.get("uid").toString());

    }

}
