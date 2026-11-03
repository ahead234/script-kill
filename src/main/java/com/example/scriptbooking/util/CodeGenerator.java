package com.example.scriptbooking.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CodeGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private CodeGenerator() {
    }

    public static String bookingCode() {
        return "BK" + LocalDateTime.now().format(FORMATTER) + String.format("%04d", RANDOM.nextInt(10000));
    }

    public static String sessionCode() {
        return "SS" + LocalDateTime.now().format(FORMATTER) + String.format("%03d", RANDOM.nextInt(1000));
    }
}
