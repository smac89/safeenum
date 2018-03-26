package com.github.smac89;

import com.github.smac89.safeenum.SafeEnum;

@SafeEnum
public enum MyEnum {
    HELLO(5), WORLD(5), UNKNOWN(-1);

    public final int size;

    MyEnum(int size) {
        this.size = size;
    }
}
