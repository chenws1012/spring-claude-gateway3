package com.woody.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PublicKey;

@Component
public class CheckTokenUtil {

    private static PublicKey PUBLICKEY;


    @Value("${publicKeyPem}")
    private String publicKeyPem;

    public static final String USER_ID_KEY = "uid";

    @PostConstruct
    public void init() throws IOException {
        Reader rdr = new StringReader(publicKeyPem);
        Object parsed = new org.bouncycastle.openssl.PEMParser(rdr).readObject();
        PUBLICKEY = new JcaPEMKeyConverter().getPublicKey(((SubjectPublicKeyInfo) parsed));
    }

    public Claims check(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(PUBLICKEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims;
    }

    public static void main(String[] args) throws IOException {
        CheckTokenUtil checkTokenUtil = new CheckTokenUtil();
        checkTokenUtil.publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAElpzANDFRDkLNJ6Ee4iB9hogVXD56\n" +
                "gNchXHXAnuYxLxuNPPBZDvtvMBUToT+L2UiUzusQJYo9oI86GH9NUqJCjQ==\n" +
                "-----END PUBLIC KEY-----";
        checkTokenUtil.init();

        Claims check1 = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000*100; i++) {
            try {
                check1 = checkTokenUtil.check("eyJraWQiOiJFRjRGMjJDMC01Q0IwLTQzNDgtOTY3Qi0wMjY0OTVFN0VGQzgiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJjb20uemhpaHVpc2h1IiwiYXVkIjoicGMiLCJzdWIiOiJCZWxsYSIsImlhdCI6MTY0MTI3NDg2NCwiZXhwIjoxNjQxOTk0ODY0LCJqdGkiOiJjNGE2ODA4ZS0zMjUzLTQxODYtOTFjMC0wZmQ5MWY5MmFiYjMiLCJ1aWQiOjMsInJvbGUiOiJhZG1pbiJ9.4BTqbPYau8EPOLB7Z_5Gj41kZX_UnTeeb0JwYojVSr1NoMK-49W04NWRMA9HebifjwPDotGwKPjZqvk-VWZ1xQ");
                System.out.println(check1);
            } catch (Exception e) {
                System.out.println(e.getMessage());
//                e.printStackTrace();
            }
        }
        System.out.println(System.currentTimeMillis() - start);
//        System.out.println(check1.getSubject());
//        System.out.println(check1.get("uid").toString());

    }

}
