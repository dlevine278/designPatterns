package org.dplevine.patterns.pipeline;

import java.util.HashSet;
import java.util.Set;

/**
 * The PipelineSpecValidator class is responsible for validating a PipelineSpecification to ensure that it adheres to certain rules
 * and is well-formed.  This class applies several validation rules to the PipelineSpecification.
 * These rules cover various aspects, including checking for null IDs, unique IDs, well-formed stages, pipelines, parallels, and steps.
 *
 */
class PipelineSpecValidator implements Stage {

    // make sure all ids are non-null
    private static class ValidateNullIDs implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);

            if (spec.getId() == null || spec.getId().equals("")) {
                throw new PipelineBuilderException("The pipeline specification has a null identifier");
            }

            for (PipelineSpecification.StageDefinition stageDef : spec.getStages()) {
                if (stageDef.getId() == null || stageDef.getId().equals("")) {
                    throw new PipelineBuilderException("At least one stage definition has a null identifier");
                }
            }
            for (PipelineSpecification.ParallelDefinition parallelDef : spec.getParallels()) {
                if (parallelDef.getId() == null || parallelDef.getId().equals("")) {
                    throw new PipelineBuilderException("At least one parallel definition has a null identifier");
                }
            }
            for (PipelineSpecification.PipelineDefinition pipelineDef : spec.getPipelines()) {
                if (pipelineDef.getId() == null || pipelineDef.getId().equals("")) {
                    throw new PipelineBuilderException("At least one pipeline definition has a null identifier");
                }
            }
            for (String stepDef : spec.getSteps()) {
                if (stepDef == null || stepDef.equals("")) {
                    throw new PipelineBuilderException("At least one step definition has a null identifier");
                }
            }
            return context;
        }
    }

    // make sure all the ids are unique from one another
    private static class ValidateUniqueIDs implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
            Set<String> duplicateIds = new HashSet<>();
            Set<String> ids = new HashSet<>();

            for (PipelineSpecification.StageDefinition stageDef : spec.getStages()) {
                if (!ids.add(stageDef.getId())) {
                    duplicateIds.add(stageDef.getId());
                }
            }
            for (PipelineSpecification.ParallelDefinition parallelDef : spec.getParallels()) {
                if (!ids.add(parallelDef.getId())) {
                    duplicateIds.add(parallelDef.getId());
                }
            }
            for (PipelineSpecification.PipelineDefinition pipelineDef : spec.getPipelines()) {
                if (!ids.add(pipelineDef.getId())) {
                    duplicateIds.add(pipelineDef.getId());
                }
            }

            if (!duplicateIds.isEmpty()) {
                throw new PipelineBuilderException("The following Ids are duplicated:" + duplicateIds);
            }
            return context;
        }
    }

    // make sure stages are well formed (i.e., references are valid)
    private static class ValidateStageDefs implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
            Set<String> malformedStages = new HashSet<>();

            for (PipelineSpecification.StageDefinition stageDef : spec.getStages()) {
                if (stageDef.getClassName() == null || stageDef.getClassName().equals("")) {
                    malformedStages.add(stageDef.getId());
                }
            }

            if (!malformedStages.isEmpty()) {
                throw new PipelineBuilderException("The following stage definitions are malformed:" + malformedStages);
            }

            return context;
        }
    }

    // make sure piplines are well formed (i.e., references are valid)
    private static class ValidatePipelineDefs implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {

            PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
            Set<String> malformedPipelines = new HashSet<>();
            Set<String> ids = new HashSet<>();

            spec.getStages().forEach( stageDef -> ids.add(stageDef.getId()));
            spec.getPipelines().forEach(pipelineDef -> ids.add(pipelineDef.getId()));
            spec.getParallels().forEach(parallelDef -> ids.add(parallelDef.getId()));

            for (PipelineSpecification.PipelineDefinition pipelineDef : spec.getPipelines()) {
                for (String pipelineStepDef : pipelineDef.getSteps()) {
                    if (!ids.contains(pipelineStepDef)) {
                        malformedPipelines.add(pipelineDef.getId());
                    }
                }
            }

            if (!malformedPipelines.isEmpty()) {
                throw new PipelineBuilderException("The following pipelines contain unresolved references:" + malformedPipelines);
            }

            return context;
        }
    }

    // make sure parallels are well formed (i.e., references are valid)
    private static class ValidateParallelDefs implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {

            PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
            Set<String> malformedParallels = new HashSet<>();
            Set<String> ids = new HashSet<>();

            spec.getStages().forEach( stageDef -> ids.add(stageDef.getId()));
            spec.getPipelines().forEach(pipelineDef -> ids.add(pipelineDef.getId()));
            spec.getParallels().forEach(parallelDef -> ids.add(parallelDef.getId()));

            for (PipelineSpecification.ParallelDefinition parallelDef : spec.getParallels()) {
                for (PipelineSpecification.PipelineDefinition parallelPipeline : parallelDef.getParallelPipelines()) {
                    if (!ids.contains(parallelPipeline.getId())) {
                        malformedParallels.add(parallelDef.getId());
                    }
                }
            }

            if (!malformedParallels.isEmpty()) {
                throw new PipelineBuilderException("The following parallels contain unresolved references:" + malformedParallels);
            }

            return context;
        }
    }

    // make sure steps are well formed (i.e., references are valid)
    private static class ValidateSteps implements Stage {

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {

            PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
            Set<String> malformedSteps = new HashSet<>();
            Set<String> ids = new HashSet<>();

            spec.getStages().forEach( stageDef -> ids.add(stageDef.getId()));
            spec.getPipelines().forEach(pipelineDef -> ids.add(pipelineDef.getId()));
            spec.getParallels().forEach(parallelDef -> ids.add(parallelDef.getId()));

            for (String stepDef : spec.getSteps()) {
                if (!ids.contains(stepDef)) {
                    malformedSteps.add(stepDef);
                }
            }

            if (!malformedSteps.isEmpty()) {
                throw new PipelineBuilderException("The following steps definitions are unresolved:" + malformedSteps);
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
        validateSpecPipeline.addStage(new StageWrapper("validate parallel definitions", new PipelineSpecValidator.ValidateParallelDefs()));
        validateSpecPipeline.addStage(new StageWrapper("validate spec steps", new PipelineSpecValidator.ValidateSteps()));

        context = validateSpecPipeline.run(context);

        if (context.isFailure()) {
            throw new PipelineBuilderException ("The pipeline specification is malformed, please correct: " + context.getEventLog().toString());
        }

        return context;
    }
}
