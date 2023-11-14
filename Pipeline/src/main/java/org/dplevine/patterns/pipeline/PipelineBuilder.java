package org.dplevine.patterns.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The PipelineBuilder class is responsible for constructing pipelines based on different input sources,
 * such as file paths or pipeline specifications.
 * The PipelineBuilder class takes a meta approach by creating a pipeline to build new pipelines (e.g., read definition, validate definition, create pipeline)
 */
public final class PipelineBuilder {
    private static final String SPEC_GENERATOR_JSON = "Spec_Generator_JSON";
    private static final String JSON_SUFFIX = ".json";
    private static final String SPEC_GENERATOR_YAML = "Spec_Generator_YAML";
    private static final String YAML_SUFFIX = ".yaml";
    private static final String SPEC_VALIDATOR = "Spec_Validator";
    private static final String PIPELINE_GENERATOR = "Pipeline_Generator";
    private static final String PIPELINE_BOOTSTRAPPER = "Bootstrap Pipeline Builder";

    private static final Logger logger = LoggerFactory.getLogger(PipelineBuilder.class);

    public static PipelineBuilder createBuilder() {
        return new PipelineBuilder();
    }

    private PipelineBuilder() {
    }

    private final StageWrapper specFromJsonGenerator = new StageWrapper(SPEC_GENERATOR_JSON, (context) -> {
        String pathname = (String) context.getObject(BuilderContext.SPEC_PATHNAME);

        try {
            ObjectMapper mapper = new ObjectMapper();
            PipelineSpecification spec = mapper.readValue(new File(pathname), PipelineSpecification.class);
            context.addObject(BuilderContext.PIPELINE_SPEC, spec);

        } catch (Exception e) {
            logger.error("stack trace:" + e.getLocalizedMessage());
            logger.error("pipeline event log:" + context.getEventLog().toString());
            throw new PipelineBuilderException(e);
        }
        return context;
    });

    private final StageWrapper specFromYamlGenerator = new StageWrapper(SPEC_GENERATOR_YAML, (context) -> {
        String pathname = (String) context.getObject(BuilderContext.SPEC_PATHNAME);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            //mapper.findAndRegisterModules();
            PipelineSpecification spec = mapper.readValue(new File(pathname), PipelineSpecification.class);
            context.addObject(BuilderContext.PIPELINE_SPEC, spec);

        } catch (Exception e) {
            logger.error("stack trace:" + e.getLocalizedMessage());
            logger.error("pipeline event log:" + context.getEventLog().toString());
            throw new PipelineBuilderException(e);
        }
        return context;
    });

    public Pipeline buildFromPathName(String pathname) throws Exception {
        StageWrapper fileReaderStage;
        if (pathname.contains(JSON_SUFFIX)) {
            fileReaderStage = specFromJsonGenerator;
        } else if (pathname.contains(YAML_SUFFIX)) {
            fileReaderStage = specFromYamlGenerator;
        } else {
            throw new PipelineBuilderException("Unrecognized file suffix.  Supported types are .json and .yaml.");
        }

        ExecutionContext context = new ExecutionContext();
        context.addObject(BuilderContext.SPEC_PATHNAME, pathname);

        Pipeline builderPipeline = new Pipeline(PIPELINE_BOOTSTRAPPER);
        builderPipeline.addStage(fileReaderStage).addStage(new StageWrapper(SPEC_VALIDATOR, new PipelineSpecValidator())).addStage(new StageWrapper(PIPELINE_GENERATOR, new PipelineGenerator()));
        Pipeline pipeline = (Pipeline) builderPipeline.run(context).getObject(BuilderContext.PIPELINE);
        if (pipeline == null) {
            throw new PipelineBuilderException("Pipeline could not be generated");
        }
        return pipeline;
    }

    public Pipeline buildFromPipelineSpecification(PipelineSpecification spec) throws Exception {
        ExecutionContext context = new ExecutionContext();
        context.addObject(BuilderContext.PIPELINE_SPEC, spec);

        Pipeline builderPipeline = new Pipeline(PIPELINE_BOOTSTRAPPER);
        builderPipeline.addStage(new StageWrapper(SPEC_VALIDATOR, new PipelineSpecValidator())).addStage(new StageWrapper(PIPELINE_GENERATOR, new PipelineGenerator()));
        Pipeline pipeline = (Pipeline) builderPipeline.run(context).getObject(BuilderContext.PIPELINE);
        if (pipeline == null) {
            throw new PipelineBuilderException("Pipeline could not be generated");
        }
        return pipeline;
    }
}
