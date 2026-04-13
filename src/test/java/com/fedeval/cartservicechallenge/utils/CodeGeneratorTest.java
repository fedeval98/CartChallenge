package com.fedeval.cartservicechallenge.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodeGeneratorTest {
    @Test
    void shouldGenerateValidProductCode() {
        String code = CodeGenerator.generateProductCode();

        assertNotNull(code);
        assertTrue(code.startsWith("PROD-"));
        assertEquals(13, code.length());

        String suffix = code.substring(5);
        assertTrue(suffix.matches("[A-Z0-9]{8}"));
    }

    @Test
    void shouldGenerateValidCartCode() {
        String code = CodeGenerator.generateCartCode();

        assertNotNull(code);
        assertTrue(code.startsWith("CART-"));
        assertEquals(13, code.length());

        String suffix = code.substring(5);
        assertTrue(suffix.matches("[A-Z0-9]{8}"));
    }
}

