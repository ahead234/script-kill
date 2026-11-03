package com.example.scriptbooking.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * JDK 8 兼容的不可变 Set 构造工具。
 * 用于替代 Java 9+ 才提供的 Set.of，保证工程可在 JDK 8 上编译运行。
 */
public final class Sets {
    private Sets() {
    }

    public static Set<String> of(String... values) {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
    }
}
