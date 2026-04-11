package com.fedeval.cartservicechallenge.utils;

import java.util.UUID;

public class CodeGenerator {
    private CodeGenerator() {
    }

    public static String generateProductCode() {
        return "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateCartCode() {
        return "CART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
