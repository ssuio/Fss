package com.ift.sw.fss;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FSSTag {
    String key();
}
