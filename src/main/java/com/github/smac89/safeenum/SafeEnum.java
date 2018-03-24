package com.github.smac89.safeenum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Logger;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface SafeEnum {
    String safeName() default "UNKNOWN";

    /**
     * Log the use of the safer alternative
     * @return true if we should also log the usage
     */
    boolean log() default false;
}
