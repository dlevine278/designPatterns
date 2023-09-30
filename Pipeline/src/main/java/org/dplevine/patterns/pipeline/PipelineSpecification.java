package org.dplevine.patterns.pipeline;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class PipelineSpecification {
    @JsonProperty
    private String id;
    @JsonProperty
    private List<OptionDefinition> options = new Vector<>();
    @JsonProperty
    private List<StageDefinition> stages = new Vector<>();
    @JsonProperty
    private List<ForkDefinition> forks = new Vector<>();
    @JsonProperty
    private List<PipelineDefinition> pipelines = new Vector<>();
    @JsonProperty
    private List<StepDefinition> steps = new Vector<>();
    @JsonProperty
    private Boolean fastFail = true;  //true by default



    public PipelineSpecification() {
    }

    PipelineSpecification(String id, List<OptionDefinition> options, List<StageDefinition> stages, List<PipelineDefinition> pipelines, List<ForkDefinition> forks, List<StepDefinition> steps) {
        this.id = id;
        this.options.addAll(options);
        this.stages.addAll(stages);
        this.pipelines.addAll(pipelines);
        this.forks.addAll(forks);
        this.steps.addAll(steps);
    }

    public static class OptionDefinition {
        @JsonProperty
        private String name;
        @JsonProperty
        private OptionType type;
        @JsonProperty
        private String value;

        public OptionDefinition() {
        }

        public OptionDefinition(String name, OptionType type, String value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(OptionType type) {
            this.type = type;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public OptionType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public static enum OptionType {
            STRING,
            INTEGER,
            DOUBLE,
            DATETIME,
            CUSTOM
        }
    }

    public static class StageDefinition {
        @JsonProperty
        private String id;
        @JsonProperty
        private String classPath;
        @JsonProperty
        private String className;

        public StageDefinition() {
        }

        public StageDefinition(String id, String classPath, String className){
            this.id = id;
            this.classPath = classPath;
            this.className = className;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setClassPath(String classPath) {
            this.classPath = classPath;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getId() {
            return id;
        }

        public String getClassPath() {
            return classPath;
        }

        public String getClassName() {
            return className;
        }
    }

    public static class ForkDefinition {
        @JsonProperty
        private String id;
        @JsonProperty
        private List<PipelineDefinition> pipelines;

        public ForkDefinition() {
        }

        public ForkDefinition(String id, List<PipelineDefinition> pipelines) {
            this.id = id;
            this.pipelines.addAll(pipelines);
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setPipelines(List<PipelineDefinition> pipelines) {
            this.pipelines = pipelines;
        }

        public String getId() {
            return id;
        }

        public List<PipelineDefinition> getPipelines() {
            return pipelines;
        }
    }

    public static class PipelineDefinition {
        @JsonProperty
        private String id;
        @JsonProperty
        private List<StepDefinition> steps;

        public PipelineDefinition() {
        }

        public PipelineDefinition(String id, List<StepDefinition> steps) {
            this.id = id;
            this.steps.addAll(steps);
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setSteps(List<StepDefinition> steps) {
            this.steps = steps;
        }

        public String getId() {
            return id;
        }

        public List<StepDefinition> getSteps() {
            return steps;
        }
    }

    public static class StepDefinition {
        @JsonProperty
        private String id;

        public StepDefinition() {
        }

        public StepDefinition(String id) {
            this.id = id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

    }

    public String getId() {
        return id;
    }

    public List<OptionDefinition> getOptions() {
        return options;
    }

    public List<StageDefinition> getStages() {
        return stages;
    }

    public List<ForkDefinition> getForks() {
        return forks;
    }

    public List<PipelineDefinition> getPipelines() {
        return pipelines;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    public Boolean getFastFail() {
        return fastFail;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOptions(List<OptionDefinition> options) {
        this.options = options;
    }

    public void setStages(List<StageDefinition> stages) {
        this.stages = stages;
    }

    public void setForks(List<ForkDefinition> forks) {
        this.forks = forks;
    }

    public void setPipelines(List<PipelineDefinition> pipelines) {
        this.pipelines = pipelines;
    }

    public void setSteps(List<StepDefinition> steps) {
        this.steps = steps;
    }

    public void setFastFail(Boolean fastFail) {
        this.fastFail = fastFail;
    }
}
