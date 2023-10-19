package org.dplevine.patterns.pipeline;

/**
 * The PipelineBuilder class is responsible for constructing pipelines based on different input sources,
 * such as file paths or pipeline specifications.
 * The PipelineBuilder class takes a meta approach by creating a pipeline to build new pipelines (e.g., read definition, validate definition, create pipeline)
 */
public final class PipelineBuilder {

    public static PipelineBuilder createBuilder() {
        return new PipelineBuilder();
    }

    PipelineBuilder() {
        Pipeline builderPipeline = new Pipeline("Builder");
        builderPipeline.addStage(new StageWrapper("Spec_Generator_JSON", new SpecFromJsonGenerator())).addStage(new StageWrapper("Spec_Validator", new PipelineSpecValidator())).addStage(new StageWrapper("Pipeline_Generator", new PipelineGenerator()));
    }

    public Pipeline buildFromPathName(String pathname) throws Exception {
        StageWrapper fileReaderStage;
        if (pathname.contains(".json")) {
            fileReaderStage = new StageWrapper("Spec_Generator_JSON", new SpecFromJsonGenerator());
        } else if (pathname.contains(".yaml")) {
            fileReaderStage = new StageWrapper("Spec_Generator_YAML", new SpecFromYamlGenerator());
        } else {
            throw new PipelineBuilderException("Unrecognized file suffix.  Supported types are .json and .yaml.");
        }

        ExecutionContext context = new ExecutionContext();
        context.addObject(BuilderContext.SPEC_PATHNAME, pathname);
        Pipeline builderPipeline = new Pipeline("Bootstrap Pipeline Builder");
        builderPipeline.addStage(fileReaderStage).addStage(new StageWrapper("Spec_Validator", new PipelineSpecValidator())).addStage(new StageWrapper("Pipeline_Generator", new PipelineGenerator()));

        Pipeline pipeline = (Pipeline) builderPipeline.run(context).getObject(BuilderContext.PIPELINE);
        if (pipeline == null) {
            throw new PipelineBuilderException("Pipeline could not be generated");
        }
        return pipeline;
    }

    public Pipeline buildFromPipelineSpecification(PipelineSpecification spec) throws Exception {
        ExecutionContext context = new ExecutionContext();
        context.addObject(BuilderContext.PIPELINE_SPEC, spec);
        Pipeline builderPipeline = new Pipeline("Bootstrap Pipeline Builder");
        builderPipeline.addStage(new StageWrapper("Spec_Validator", new PipelineSpecValidator())).addStage(new StageWrapper("Pipeline_Generator", new PipelineGenerator()));

        Pipeline pipeline = (Pipeline) builderPipeline.run(context).getObject(BuilderContext.PIPELINE);
        if (pipeline == null) {
            throw new PipelineBuilderException("Pipeline could not be generated");
        }
        return pipeline;
    }
}
