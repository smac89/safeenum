package com.github.smac89.internal.aspect;

import com.github.smac89.internal.Constants;
import com.github.smac89.internal.EnumMembers;
import com.github.smac89.safeenum.SafeName;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Aspect
public final class SafeEnumInjector {
    private final Map<String, EnumMembers> enumFields = new HashMap<String, EnumMembers>();

    @Pointcut("execution(* (@(com.github.smac89.safeenum.SafeEnum) *).valueOf(String)) && args(name)")
    public void callToValueOf(String name) {
    }

    @Pointcut("staticinitialization (@(com.github.smac89.safeenum.SafeEnum) *)")
    public void enumDeclared() {
    }

    @Around("callToValueOf(value)")
    public Object atMatchedElement(String value, ProceedingJoinPoint jp) throws Throwable {
        String enumName = jp.getSignature().getDeclaringType().getCanonicalName();
        EnumMembers declaredConstants = enumFields.get(enumName);

        if (declaredConstants == null || declaredConstants.fieldSet.contains(value)) {
            return jp.proceed();
        }

        if (!declaredConstants.fieldSet.isEmpty()) {
            return jp.proceed(new Object[]{declaredConstants.safeName});
        }

        return jp.proceed();
    }

    @After("enumDeclared()")
    public void cacheEnumFields(JoinPoint jp) {
        String name = jp.getSignature().getDeclaringType().getCanonicalName();
        String safeName = Constants.SAFE_NAME_DEFAULT;
        Field[] fields = jp.getSignature().getDeclaringType().getFields();

        int enumsIndexLast = 0;
        for (int enumsIndex = 0; enumsIndex < fields.length; enumsIndex++) {
            Field enumField = fields[enumsIndex];
            if (enumField.isEnumConstant()) {
                fields[enumsIndexLast++] = enumField;
                SafeName defaultName = enumField.getAnnotation(SafeName.class);
                if (defaultName != null) {
                    safeName = enumField.getName();
                }
            }
        }

        if (enumsIndexLast > 1) {
            List<Field> fieldList = Arrays.asList(Arrays.copyOf(fields, enumsIndexLast));
            Set<String> constantNames = new HashSet<String>();
            for (Field field : fieldList) {
                constantNames.add(field.getName());
            }
            enumFields.put(name, new EnumMembers(safeName, Collections.unmodifiableSet(constantNames)));
        } else if (enumsIndexLast == 1) {
            enumFields.put(name, new EnumMembers(safeName, Collections.singleton(fields[0].getName())));
        } else {
            enumFields.put(name, new EnumMembers(safeName, Collections.<String>emptySet()));
        }
    }

}
