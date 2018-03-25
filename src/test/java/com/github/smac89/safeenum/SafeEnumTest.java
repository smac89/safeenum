package com.github.smac89.safeenum;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SafeEnumTest {
    @Test
    public void callToValueOf_isReplacedWithSafeCall() {
        Test1Enum test1Enum = Test1Enum.valueOf("bar");
        assertEquals("Expected to equal UNKNOWN", test1Enum, Test1Enum.UNKNOWN);
    }

    @Test
    public void callToValueOf_isNotReplacedWithSafeCall() {
        Test2Enum test2Enum = Test2Enum.valueOf("UNKNOWN");
        assertEquals("Expected to equal UNKNOWN", test2Enum, Test2Enum.UNKNOWN);
    }

    @SafeEnum
    enum Test1Enum {
        A, B, C, UNKNOWN
    }

    @SafeEnum
    private enum Test2Enum {
        A, B, C, UNKNOWN
    }
}
