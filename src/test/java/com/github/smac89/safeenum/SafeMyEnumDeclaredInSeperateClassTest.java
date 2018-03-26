package com.github.smac89.safeenum;

import com.github.smac89.MyEnum;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SafeMyEnumDeclaredInSeperateClassTest {

    @Test
    public void callValueOf_whenEnumIsInSeperateClass_willUseSafeCall() {
        MyEnum myEnum = MyEnum.valueOf("foobar");
        assertEquals("Expected myEnum to equal UNKNOWN", myEnum, MyEnum.UNKNOWN);
    }
}
