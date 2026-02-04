package com.example.userservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@Testcontainers
public class PasswordEncoderConfigTest extends AbstractIntegrationTest {

    @Autowired
    ApplicationContext ctx;

    private int extractStrength(String encoded) {
        Pattern p = Pattern.compile("\\$2[aby]\\$(\\d\\d)\\$");
        Matcher m = p.matcher(encoded);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    @Test
    void default_strength_is_at_least_10() {
        PasswordEncoder enc = ctx.getBean(PasswordEncoder.class);
        String hash = enc.encode("password");
        int s = extractStrength(hash);
        Assertions.assertTrue(s >= 10, "BCrypt strength should be >= 10, got: " + s);
    }
}
