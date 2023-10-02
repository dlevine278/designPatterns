package org.dplevine.patterns.pipeline;

import java.io.File;
import java.util.*;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PipelineBuilder {

    private static Logger logger = LoggerFactory.getLogger(PipelineBuilder.class);

    private Pipeline builderPipeline;

    public static PipelineBuilder createBuilder() {
        return new PipelineBuilder();
    }

    PipelineBuilder() {
        builderPipeline = new Pipeline("Builder");
        builderPipeline.addStage(new StageWrapper("Spec_Generator_JSON", new SpecFromJsonGenerator())).addStage(new StageWrapper("Spec_Validator", new PipelineSpecValidator())).addStage(new StageWrapper("Pipeline_Generator", new PipelineGenerator()));
    }

    public Pipeline buildFromPathName(String pathname) throws Exception {
        StageWrapper fileReaderStage;
        if (pathname.contains(".json")) {
            fileReaderStage = new StageWrapper("Spec_Generator_JSON", new SpecFromJsonGenerator());
        } else if (pathname.contains(".yaml")) {
            fileReaderStage = new StageWrapper("Spec_Generator_YAML", new SpecFromYamlGenerator());
        } else {
            throw new Exception("Unrecoginized file suffix.  Supported types are .json and .yaml.");
        }

        ExecutionContext context = new ExecutionContext();
        context.addObject(BuilderContext.SPEC_PATHNAME, pathname);
        Pipeline builderPipeline = new Pipeline("Bootstrap Pipeline Builder");
        builderPipeline.addStage(fileReaderStage).addStage(new StageWrapper("Spec_Validator", new PipelineSpecValidator())).addStage(new StageWrapper("Pipeline_Generator", new PipelineGenerator()));

        Pipeline pipeline = (Pipeline) builderPipeline.run(context).getObject(BuilderContext.PIPELINE);
        if (pipeline == null) {
            throw new Exception("Pipeline could not be generated");
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
            throw new Exception("Pipeline could not be generated");
        }
        return pipeline;
    }
}
