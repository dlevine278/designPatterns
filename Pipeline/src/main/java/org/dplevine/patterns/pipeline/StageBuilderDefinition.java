package org.dplevine.patterns.pipeline;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(StageBuilderDefinitions.class)
public @interface StageBuilderDefinition {
    String id();
}