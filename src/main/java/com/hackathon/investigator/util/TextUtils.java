package com.hackathon.investigator.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(\\d+(?:[,\\.]\\d+)?)\\s*(?:taka|tk|bdt|৳)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(\\+?880\\d{10}|01\\d{9})"
    );

    private TextUtils() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).trim();
    }

    public static BigDecimal extractAmount(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = convertBengaliDigits(text);
        Matcher matcher = AMOUNT_PATTERN.matcher(normalized);
        if (matcher.find()) {
            String raw = matcher.group(1).replace(",", "");
            return new BigDecimal(raw);
        }
        return null;
    }

    private static String convertBengaliDigits(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace('০', '0').replace('১', '1').replace('২', '2').replace('৩', '3')
                .replace('৪', '4').replace('৫', '5').replace('৬', '6').replace('৭', '7')
                .replace('৮', '8').replace('৯', '9');
    }

    public static String extractPhoneNumber(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static boolean containsAny(String text, String... keywords) {
        String normalized = normalize(text);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
