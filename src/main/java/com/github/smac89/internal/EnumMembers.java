package com.github.smac89.internal;

import java.util.Set;

public final class EnumMembers {
    public final String safeName;
    public final Set<String> fieldSet;

    public EnumMembers(String safeName, Set<String> fieldSet) {
        this.safeName = safeName;
        this.fieldSet = fieldSet;
    }
}
