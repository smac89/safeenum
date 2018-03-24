package com.github.smac89.safeenum;

import org.junit.Test;

public class SafeEnumNotAnnotatedTest {

    @Test(expected = IllegalArgumentException.class)
    public void enumWithoutAnnotation_isIgnored() {
        Test1Enum test1Enum = Test1Enum.valueOf("Blah");
    }

    public enum Test1Enum {
        A, B, C
    }
}
