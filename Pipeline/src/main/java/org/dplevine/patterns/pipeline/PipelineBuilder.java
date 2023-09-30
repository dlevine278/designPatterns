package org.dplevine.patterns.pipeline;

import java.io.File;
import java.util.*;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PipelineBuilder {

    private static Logger logger = LoggerFactory.getLogger(PipelineBuilder.class);

    private static class BuilderContext extends ExecutionContext {
        public static final String SPEC_PATHNAME = "SPEC_PATHNAME";
        public static final String PIPELINE_SPEC = "PIPELINE_SPEC";
        public static final String PIPELINE = "PIPELINE";
        public static final String STAGE_BUILDERS = "STAGE_BUILDERS";

        static interface BuilderEvents extends ExecutionContext.EventType {
            public static String NULL_IDS_FOUND = "NULL_IDS_FOUND";
        }

        BuilderContext() {
            super();
        }
    }

    private Pipeline builderPipeline;

    public static PipelineBuilder createBuilder() {
        return new PipelineBuilder();
    }

    PipelineBuilder() {
        builderPipeline = new Pipeline("Builder");
        builderPipeline.addStage(new StageWrapper("Spec_Generator_JSON", new SpecFromJsonGenerator())).addStage(new StageWrapper("Spec_Validator", new PipelineSpecValidator())).addStage(new StageWrapper("Pipeline_Generator", new PipelineGenerator()));
    }

    private static class SpecFromJsonGenerator implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            String pathname = (String) context.getObject(BuilderContext.SPEC_PATHNAME);

            try {
                ObjectMapper mapper = new ObjectMapper();
                PipelineSpecification spec = mapper.readValue(new File(pathname), PipelineSpecification.class);
                context.addObject(BuilderContext.PIPELINE_SPEC, spec);

            } catch (Exception e) {
                logger.error("stack trace:" + e.getLocalizedMessage());
                logger.error("pipeline event log:" + context.getEventLog().toString());
            }
            return context;
        }
    }

    private static class SpecFromYamlGenerator implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            String pathname = (String) context.getObject(BuilderContext.SPEC_PATHNAME);

            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                //mapper.findAndRegisterModules();
                PipelineSpecification spec = mapper.readValue(new File(pathname), PipelineSpecification.class);
                context.addObject(BuilderContext.PIPELINE_SPEC, spec);

            } catch (Exception e) {
                logger.error("stack trace:" + e.getLocalizedMessage());
                logger.error("pipeline event log:" + context.getEventLog().toString());
            }
            return context;
        }
    }

    private static class PipelineSpecValidator implements Stage {

        // make sure all ids are non-null
        private class ValidateNullIDs implements Stage {

            @Override
            public ExecutionContext doWork(ExecutionContext context) throws Exception {
                PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);

                if (spec.getId() == null || spec.getId().equals("")) {
                    throw new Exception("The pipeline specification has a null identifier");
                }
                for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
                    if (stage.getId() == null || stage.getId().equals("")) {
                        throw new Exception("At least one stage definition has a null identifier");
                    }
                }
                for (PipelineSpecification.ForkDefinition fork : spec.getForks()) {
                    if (fork.getId() == null || fork.getId().equals("")) {
                        throw new Exception("At least one fork definition has a null identifier");
                    }
                }
                for (PipelineSpecification.PipelineDefinition pipeline : spec.getPipelines()) {
                    if (pipeline.getId() == null || pipeline.getId().equals("")) {
                        throw new Exception("At least one pipeline definition has a null identifier");
                    }
                }
                for (PipelineSpecification.StepDefinition step : spec.getSteps()) {
                    if (step.getId() == null || step.getId().equals("")) {
                        throw new Exception("At least one step definition has a null identifier");
                    }
                }
                return context;
            }
        }

        // make sure all the ids are unique from one another
        private class ValidateUniqueIDs implements Stage {

            @Override
            public ExecutionContext doWork(ExecutionContext context) throws Exception {
                PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
                Set<String> duplicateIds = new HashSet<String>();
                Set<String> ids = new HashSet<String>();

                for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
                    if (!ids.add(stage.getId())) {
                        duplicateIds.add(stage.getId());
                    }
                }
                for (PipelineSpecification.ForkDefinition fork : spec.getForks()) {
                    if (!ids.add(fork.getId())) {
                        duplicateIds.add(fork.getId());
                    }
                }
                for (PipelineSpecification.PipelineDefinition pipeline : spec.getPipelines()) {
                    if (!ids.add(pipeline.getId())) {
                        duplicateIds.add(pipeline.getId());
                    }
                }

                if (!duplicateIds.isEmpty()) {
                    throw new Exception("The following Ids are duplicated:" + duplicateIds.toString());
                }
                return context;
            }
        }

        // make sure stages are well formed (i.e., references are valid)
        private class ValidateStageDefs implements Stage {

            @Override
            public ExecutionContext doWork(ExecutionContext context) throws Exception {
                PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
                Set<String> malformedStages = new HashSet<>();

                for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
                    if (stage.getClassName() == null || stage.getClassName().equals("")) {
                        malformedStages.add(stage.getId());
                    }
                }

                if (!malformedStages.isEmpty()) {
                    throw new Exception("The following stage definitions are malformed:" + malformedStages.toString());
                }

                return context;
            }
        }

        // make sure piplines are well formed (i.e., references are valid)
        private class ValidatePipelineDefs implements Stage {

            @Override
            public ExecutionContext doWork(ExecutionContext context) throws Exception {

                PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
                Set<String> malformedPipelines = new HashSet<>();
                Set<String> ids = new HashSet<>();

                for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
                    ids.add(stage.getId());
                }
                for (PipelineSpecification.PipelineDefinition pipeline : spec.getPipelines()) {
                    ids.add(pipeline.getId());
                }
                for (PipelineSpecification.ForkDefinition fork : spec.getForks()) {
                    ids.add(fork.getId());
                }

                for (PipelineSpecification.PipelineDefinition pipeline : spec.getPipelines()) {
                    for (PipelineSpecification.StepDefinition pipelineStep : pipeline.getSteps()) {
                        if (!ids.contains(pipelineStep.getId())) {
                            malformedPipelines.add(pipeline.getId());
                        }
                    }
                }

                if (!malformedPipelines.isEmpty()) {
                    throw new Exception("The following pipelines contain unresolved references:" + malformedPipelines.toString());
                }

                return context;
            }
        }

        // make sure forks are well formed (i.e., references are valid)
        private class ValidateForkDefs implements Stage {

            @Override
            public ExecutionContext doWork(ExecutionContext context) throws Exception {

                PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
                Set<String> malformedForks = new HashSet<>();
                Set<String> ids = new HashSet<>();

                for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
                    ids.add(stage.getId());
                }
                for (PipelineSpecification.PipelineDefinition pipeline : spec.getPipelines()) {
                    ids.add(pipeline.getId());
                }
                for (PipelineSpecification.ForkDefinition fork : spec.getForks()) {
                    ids.add(fork.getId());
                }

                for (PipelineSpecification.ForkDefinition fork : spec.getForks()) {
                    for (PipelineSpecification.PipelineDefinition forkPipeline : fork.getPipelines()) {
                        if (!ids.contains(forkPipeline.getId())) {
                            malformedForks.add(fork.getId());
                        }
                    }
                }

                if (!malformedForks.isEmpty()) {
                    throw new Exception("The following forks contain unresolved references:" + malformedForks.toString());
                }

                return context;
            }
        }

        // make sure steps are well formed (i.e., references are valid)
        private class ValidateSteps implements Stage {

            @Override
            public ExecutionContext doWork(ExecutionContext context) throws Exception {

                PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
                Set<String> malformedSteps = new HashSet<>();
                Set<String> ids = new HashSet<>();

                for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
                    ids.add(stage.getId());
                }
                for (PipelineSpecification.PipelineDefinition pipeline : spec.getPipelines()) {
                    ids.add(pipeline.getId());
                }
                for (PipelineSpecification.ForkDefinition fork : spec.getForks()) {
                    ids.add(fork.getId());
                }

                for (PipelineSpecification.StepDefinition step : spec.getSteps()) {
                    if (!ids.contains(step.getId())) {
                        malformedSteps.add(step.getId());
                    }
                }

                if (!malformedSteps.isEmpty()) {
                    throw new Exception("The following steps definitions are unresolved:" + malformedSteps.toString());
                }

                return context;
            }

            // make sure there are no circular references
        }

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
            Pipeline validateSpecPipeline = new Pipeline("validate: " + spec.getId());
            validateSpecPipeline.addStage(new StageWrapper("validate null IDs", new ValidateNullIDs()));
            validateSpecPipeline.addStage(new StageWrapper("validate unique IDs", new ValidateUniqueIDs()));
            validateSpecPipeline.addStage(new StageWrapper("validate stage definitions", new ValidateStageDefs()));
            validateSpecPipeline.addStage(new StageWrapper("validate pipeline definitions", new ValidatePipelineDefs()));
            validateSpecPipeline.addStage(new StageWrapper("validate fork definitions", new ValidateForkDefs()));
            validateSpecPipeline.addStage(new StageWrapper("validate spec steps", new ValidateSteps()));

            context = validateSpecPipeline.run(context);

            if (context.isFailure()) {
                throw new Exception ("The pipeline specification is malformed, please correct: " + context.getEventLog().toString());
            }

            return context;
        }
    }

    private static class PipelineGenerator implements Stage {

        Map<String, Builder> builderMap = new HashMap<>();

        class Builder {
            private StageBuilder stageBuilder;

            Builder(String classpath, String className) throws Exception {
                try {
                    ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
                    Class<?> clazz = appClassLoader.loadClass(className);
                    this.stageBuilder = (StageBuilder) clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new Exception("Could not construct a StageBuilder for class: " + className);
                }
            }

            Builder(StageBuilder stageBuilder) {
                this.stageBuilder = stageBuilder;
            }

            Stage newStage(String id) throws Exception {
                Stage stage;
                if (stageBuilder == null) {
                    throw new Exception("No StageBuilder found for: " + id);
                }
                return stageBuilder.buildStage();
            }

            // getters
            StageBuilder getStageBuilder() {
                return stageBuilder;
            }
        }
/*
        class PipelineStageBuilder implements StageBuilder {
            List<PipelineStage> stages = new Vector<>();

            PipelineStageBuilder(List<String> stageIDs, Map<String, Builder> builderMap) throws Exception {
                for(String stageId : stageIDs) {
                    stages.add(new PipelineStage(stageId, builderMap.get(stageId).newStage(stageId)));
                }
            }

            @Override
            public Stage buildStage() {
                Pipeline pipeline = new Pipeline();
                pipeline.addStages(stages);
                return pipeline;
            }
        }

        class ForkStageBuilder implements StageBuilder {
            List<Pipeline> stages = new Vector<>();

            ForkStageBuilder(List<String> stageIDs, Map<String, Builder> builderMap) throws Exception {
                for(String stageId : stageIDs) {
                    stages.add(new Pipeline(stageId, builderMap.get(stageId).newStage(stageId)));
                }
            }

            @Override
            public Stage buildStage(String id) {
                Fork fork = new Fork(id);
                fork.addPipelines(stages);
                return null;
            }
        }  */

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            PipelineSpecification spec = (PipelineSpecification)  context.getObject(BuilderContext.PIPELINE_SPEC);
            List<Stage> stages = new Vector<>();

            // Get builder (aka factory) for each defined stage (if any)
            for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
                Builder builder = new Builder(stage.getClassPath(), stage.getClassName());
                builderMap.put(stage.getId(), builder);
            }

            // Create sub-pipelines from pipeline node (if any)

            // Create forks (if any)

            // Assemble the pipeline and insert it into the context
            Pipeline pipeline = new Pipeline(spec.getId());
            for(PipelineSpecification.StepDefinition step : spec.getSteps()) {
                pipeline.addStage(new StageWrapper(step.getId(), builderMap.get(step.getId()).newStage(step.getId())));
            }
            context.addObject(BuilderContext.PIPELINE, pipeline);

            return context;
        }
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
        Pipeline builderPipeline = new Pipeline("Builder");
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
        Pipeline builderPipeline = new Pipeline("Builder");
        builderPipeline.addStage(new StageWrapper("Spec_Validator", new PipelineSpecValidator())).addStage(new StageWrapper("Pipeline_Generator", new PipelineGenerator()));

        Pipeline pipeline = (Pipeline) builderPipeline.run(context).getObject(BuilderContext.PIPELINE);
        if (pipeline == null) {
            throw new Exception("Pipeline could not be generated");
        }
        return pipeline;
    }
}
