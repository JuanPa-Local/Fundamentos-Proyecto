package com.openlib.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {
    @Test
    public void testHash() {
        System.out.println("HASH_OUTPUT_START");
        System.out.println(new BCryptPasswordEncoder().encode("1234"));
        System.out.println("HASH_OUTPUT_END");
    }
}
