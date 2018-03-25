package com.github.smac89.safeenum;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SafeEnumAnnotatedWithCustomSafeNameTest {
    @Test
    public void callToValueOf_whenSafeEnumWithCustomName_isReplacedWithSafeCall() {
        Test1Enum test1Enum = Test1Enum.valueOf("Foo");
        assertEquals("Expected the enum to choose the last one", test1Enum, Test1Enum.LAST);
    }

    @SafeEnum
    public enum Test1Enum {
        A, B, C, @SafeName LAST
    }
}
