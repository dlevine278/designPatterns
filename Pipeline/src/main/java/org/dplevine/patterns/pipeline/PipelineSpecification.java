package org.dplevine.patterns.pipeline;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The PipelineSpecification class represets a specification for defining a pipeline's structure, including
 * stages, options, and parallel sections.
 *
 * It models the structure of a pipeline using various inner classes:
 * OptionDefinition to define options with a name, type, and value.
 * StageDefinition to define stages with an identifier and class name.
 * ParallelDefinition to define parallel sections containing a list of parallel pipelines.
 * PipelineDefinition to define a pipeline with an identifier and a list of steps.
 */
public final class PipelineSpecification {
    @JsonProperty(required = true)
    private String id;
    @JsonProperty
    private List<StageDefinition> stages = new Vector<>();
    @JsonProperty
    private List<ParallelDefinition> parallels = new Vector<>();
    @JsonProperty(required = true)
    private List<String> steps = new Vector<>();
    @JsonIgnore
    private List<PipelineDefinition> pipelines = new Vector<>();


    public PipelineSpecification() {
    }

    PipelineSpecification(String id, List<StageDefinition> stages, List<PipelineDefinition> pipelines, List<ParallelDefinition> parallels, List<String> steps) {
        this.id = id;
        this.stages.addAll(stages);
        this.pipelines.addAll(pipelines);
        this.parallels.addAll(parallels);
        this.steps.addAll(steps);
    }

    public static class StageDefinition {
        @JsonProperty(required = true)
        private String id;
        @JsonProperty(required = true)
        private String className;

        public StageDefinition() {
        }

        public StageDefinition(String id, String className){
            this.id = id;
            this.className = className;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getId() {
            return id;
        }

        public String getClassName() {
            return className;
        }
    }

    public static class ParallelDefinition {
        @JsonProperty(required = true)
        private String id;
        @JsonProperty(required = true)
        private final List<PipelineDefinition> parallelPipelines = new Vector<>();

        public ParallelDefinition() {
        }

        public ParallelDefinition(String id, List<PipelineDefinition> parallelPipelines) {
            this.id = id;
            setParallelPipelines(parallelPipelines);
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setParallelPipelines(List<PipelineDefinition> parallelPipelines) {
            for (PipelineDefinition parallelPipeline : parallelPipelines) {
                parallelPipeline.setId(id + "[" + this.parallelPipelines.size() + "]");
                this.parallelPipelines.add(parallelPipeline);
            }
        }

        public String getId() {
            return id;
        }

        public List<PipelineDefinition> getParallelPipelines() {
            return parallelPipelines;
        }
    }

    public static class PipelineDefinition {
        private String id;
        @JsonProperty(required = true)
        private List<String> steps;

        public PipelineDefinition() {
        }

        public PipelineDefinition(String id, List<String> steps) {
            this.id = id;
            this.steps.addAll(steps);
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setSteps(List<String> steps) {
            this.steps = steps;
        }

        public String getId() {
            return id;
        }

        public List<String> getSteps() {
            return steps;
        }
    }

    public String getId() {
        return id;
    }

    public List<StageDefinition> getStages() {
        return stages;
    }

    public List<ParallelDefinition> getParallels() {
        return parallels;
    }

    public List<PipelineDefinition> getPipelines() {
        pipelines.clear();
        
        getParallels().forEach(parallel -> pipelines.addAll(parallel.getParallelPipelines()));
        return pipelines;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStages(List<StageDefinition> stages) {
        this.stages = stages;
    }

    public void setParallels(List<ParallelDefinition> parallels) {
        this.parallels = parallels;
    }

    public void setPipelines(List<PipelineDefinition> pipelines) {
        this.pipelines = pipelines;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
}
