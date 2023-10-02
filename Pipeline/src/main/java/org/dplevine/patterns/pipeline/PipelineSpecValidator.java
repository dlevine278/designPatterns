package org.dplevine.patterns.pipeline;

import java.util.HashSet;
import java.util.Set;

class PipelineSpecValidator implements Stage {

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

            spec.getStages().forEach( stage -> ids.add(stage.getId()));
            spec.getPipelines().forEach(pipeline -> ids.add(pipeline.getId()));
            spec.getForks().forEach(forks -> ids.add(forks.getId()));

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

            spec.getStages().forEach( stage -> ids.add(stage.getId()));
            spec.getPipelines().forEach(pipeline -> ids.add(pipeline.getId()));
            spec.getForks().forEach(forks -> ids.add(forks.getId()));

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

            spec.getStages().forEach( stage -> ids.add(stage.getId()));
            spec.getPipelines().forEach(pipeline -> ids.add(pipeline.getId()));
            spec.getForks().forEach(forks -> ids.add(forks.getId()));

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
        validateSpecPipeline.addStage(new StageWrapper("validate null IDs", new PipelineSpecValidator.ValidateNullIDs()));
        validateSpecPipeline.addStage(new StageWrapper("validate unique IDs", new PipelineSpecValidator.ValidateUniqueIDs()));
        validateSpecPipeline.addStage(new StageWrapper("validate stage definitions", new PipelineSpecValidator.ValidateStageDefs()));
        validateSpecPipeline.addStage(new StageWrapper("validate pipeline definitions", new PipelineSpecValidator.ValidatePipelineDefs()));
        validateSpecPipeline.addStage(new StageWrapper("validate fork definitions", new PipelineSpecValidator.ValidateForkDefs()));
        validateSpecPipeline.addStage(new StageWrapper("validate spec steps", new PipelineSpecValidator.ValidateSteps()));

        context = validateSpecPipeline.run(context);

        if (context.isFailure()) {
            throw new Exception ("The pipeline specification is malformed, please correct: " + context.getEventLog().toString());
        }

        return context;
    }
}
