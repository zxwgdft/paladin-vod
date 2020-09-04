package com.paladin.framework.spring;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DevCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return isDevelop(context.getEnvironment());
    }

    public static boolean isDevelop(Environment env) {
        String active = env.getProperty("spring.profiles.active");
        String[] aa = active.split(",");
        for (String a : aa) {
            if ("dev".equals(a)) {
                return true;
            }
        }
        return false;
    }

}
