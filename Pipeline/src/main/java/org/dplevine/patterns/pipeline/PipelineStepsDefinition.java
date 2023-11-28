package org.dplevine.patterns.pipeline;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(PipelineStepsDefinitions.class)
public @interface PipelineStepsDefinition {
    String pipelineRootId();
    String parallelId() default "";
    String[] steps();
}
