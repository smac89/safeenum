package com.github.smac89.safeenum;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SafeEnumPrivateEnumTest {

    @Test
    public void callToValueOf_forPrivateEnum_isReplaced() {
        Test1Enum test1Enum = Test1Enum.valueOf("foo");
        assertEquals("Expected to equal UNKNOWN", test1Enum, Test1Enum.UNKNOWN);
    }

    @SafeEnum
    private enum Test1Enum {
        A, B, C, UNKNOWN
    }
}
