package org.dplevine.patterns.pipeline;

/**
 * The StageBuilder interface is a factory component in a pipeline-based system for creating and configuring stages,
 * allowing for flexibility and customization in how stages are constructed.
 */
public interface StageBuilder {
    Stage buildStage();
}
