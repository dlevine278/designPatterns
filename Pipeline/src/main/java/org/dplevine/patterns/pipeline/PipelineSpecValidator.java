package org.dplevine.patterns.pipeline;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * The PipelineSpecValidator class is responsible for validating a PipelineSpecification to ensure that it adheres to certain rules
 * and is well-formed.  This class applies several validation rules to the PipelineSpecification.
 * These rules cover various aspects, including checking for null IDs, unique IDs, well-formed stages, pipelines, parallels, and steps.
 *
 */
final class PipelineSpecValidator implements Stage {
    private static final String VALIDATE_NULL_IDS = "validate null IDs";
    private static final String VALIDATE_UNIQUE_IDS = "validate unique IDs";
    private static final String VALIDATE_STAGE_DEFS = "validate stage definitions";
    private static final String VALIDATE_PIPELINE_DEFS = "validate pipeline definitions";
    private static final String VALIDATE_PARALLEL_DEFS = "validate parallel definitions";
    private static final String VALIDATE_STEPS = "validate spec steps";

    // make sure all ids are non-null
    private final StageWrapper validateNullIDs = new StageWrapper(VALIDATE_NULL_IDS, (context) -> {
        PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);


        if (spec.getId() == null || spec.getId().equals("")) {
            throw new PipelineBuilderException("The pipeline specification has a null identifier");
        }

        if (spec.getStages().stream().anyMatch(stageDef -> stageDef.getId() == null || stageDef.getId().equals(""))) {
            throw new PipelineBuilderException("At least one stage definition has a null identifier");
        }

        if (spec.getParallels().stream().anyMatch(parallelDef -> parallelDef.getId() == null || parallelDef.getId().equals(""))) {
            throw new PipelineBuilderException("At least one parallel definition has a null identifier");
        }

        List<PipelineSpecification.PipelineDefinition> allParallelPipelines = spec.getAllParallelPipelines();
        if (allParallelPipelines.stream().anyMatch(pipelineDef -> pipelineDef.getId() == null || pipelineDef.getId().equals(""))) {
            throw new PipelineBuilderException("At least one pipeline definition has a null identifier");
        }

        if (spec.getSteps().stream().anyMatch(stepDef -> stepDef == null || stepDef.equals(""))) {
            throw new PipelineBuilderException("At least one step definition has a null identifier");
        }

        return context;
    });

    // make sure all the ids are unique from one another
    private final StageWrapper validateUniqueIDs = new StageWrapper(VALIDATE_UNIQUE_IDS, (context) -> {
        PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
        Set<String> duplicateIds = new HashSet<>();
        Set<String> ids = new HashSet<>();

        spec.getStages().stream().filter(stageDef -> !ids.add(stageDef.getId())).forEach(stageDef -> duplicateIds.add(stageDef.getId()));
        spec.getParallels().stream().filter(parallelDef -> !ids.add(parallelDef.getId())).forEach(parallelDef -> duplicateIds.add(parallelDef.getId()));
        List<PipelineSpecification.PipelineDefinition> allParallelPipelines = spec.getAllParallelPipelines();
        allParallelPipelines.stream().filter(pipelineDef -> !ids.add(pipelineDef.getId())).forEach(pipelineDef -> duplicateIds.add(pipelineDef.getId()));

        if (!duplicateIds.isEmpty()) {
            throw new PipelineBuilderException("The following Ids are duplicated:" + duplicateIds);
        }
        return context;
    });

    // make sure stages are well formed (i.e., references are valid)
    private final StageWrapper validateStageDefs = new StageWrapper(VALIDATE_STAGE_DEFS, (context) -> {
        PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
        Set<String> malformedStages = new HashSet<>();

        spec.getStages().stream().filter(stageDef -> stageDef.getClassName() == null || stageDef.getClassName().equals("")).forEach(stageDef -> malformedStages.add(stageDef.getId()));

        if (!malformedStages.isEmpty()) {
            throw new PipelineBuilderException("The following stage definitions are malformed:" + malformedStages);
        }

        return context;
    });

    // make sure piplines are well formed (i.e., references are valid)
    private final StageWrapper validatePipelineDefs = new StageWrapper(VALIDATE_PIPELINE_DEFS, (context) -> {
        PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
        Set<String> malformedPipelines = new HashSet<>();
        Set<String> ids = new HashSet<>();
        List<PipelineSpecification.PipelineDefinition> allParallelPipelines = spec.getAllParallelPipelines();

        spec.getStages().forEach( stageDef -> ids.add(stageDef.getId()));
        allParallelPipelines.forEach(pipelineDef -> ids.add(pipelineDef.getId()));
        spec.getParallels().forEach(parallelDef -> ids.add(parallelDef.getId()));

        for (PipelineSpecification.PipelineDefinition pipelineDef : allParallelPipelines) {
            pipelineDef.getSteps().stream().filter(pipelineStepDef -> !ids.contains(pipelineStepDef)).forEach(pipelineStepDef -> malformedPipelines.add(pipelineDef.getId()));
        }

        if (!malformedPipelines.isEmpty()) {
            throw new PipelineBuilderException("The following pipelines contain unresolved references:" + malformedPipelines);
        }

        return context;
    });

    // make sure parallels are well formed (i.e., references are valid)
    private final StageWrapper validateParallelDefs = new StageWrapper(VALIDATE_PARALLEL_DEFS, (context) -> {
        PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
        Set<String> malformedParallels = new HashSet<>();
        Set<String> ids = new HashSet<>();
        List<PipelineSpecification.PipelineDefinition> allParallelPipelines = spec.getAllParallelPipelines();

        spec.getStages().forEach( stageDef -> ids.add(stageDef.getId()));
        allParallelPipelines.forEach(pipelineDef -> ids.add(pipelineDef.getId()));
        spec.getParallels().forEach(parallelDef -> ids.add(parallelDef.getId()));

        for (PipelineSpecification.ParallelDefinition parallelDef : spec.getParallels()) {
            parallelDef.getParallelPipelines().stream().filter(parallelPipeline -> !ids.contains(parallelPipeline.getId())).forEach(parallelPipeline -> malformedParallels.add(parallelDef.getId()));
        }

        if (!malformedParallels.isEmpty()) {
            throw new PipelineBuilderException("The following parallels contain unresolved references:" + malformedParallels);
        }

        return context;
    });

    // make sure steps are well formed (i.e., references are valid)
    private final StageWrapper validateSteps = new StageWrapper(VALIDATE_STEPS, (context) -> {
        PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
        Set<String> malformedSteps = new HashSet<>();
        Set<String> ids = new HashSet<>();
        List<PipelineSpecification.PipelineDefinition> allParallelPipelines = spec.getAllParallelPipelines();

        spec.getStages().forEach( stageDef -> ids.add(stageDef.getId()));
        allParallelPipelines.forEach(pipelineDef -> ids.add(pipelineDef.getId()));
        spec.getParallels().forEach(parallelDef -> ids.add(parallelDef.getId()));

        spec.getSteps().stream().filter(stepDef -> !ids.contains(stepDef)).forEach(stepDef -> malformedSteps.add(stepDef));

        if (!malformedSteps.isEmpty()) {
            throw new PipelineBuilderException("The following steps definitions are unresolved:" + malformedSteps);
        }

        return context;
    });

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        PipelineSpecification spec = (PipelineSpecification) context.getObject(BuilderContext.PIPELINE_SPEC);
        Pipeline validateSpecPipeline = new Pipeline("validate: " + spec.getId());

        validateSpecPipeline.addStage(validateNullIDs);
        validateSpecPipeline.addStage(validateUniqueIDs);
        validateSpecPipeline.addStage(validateStageDefs);
        validateSpecPipeline.addStage(validatePipelineDefs);
        validateSpecPipeline.addStage(validateParallelDefs);
        validateSpecPipeline.addStage(validateSteps);

        context = validateSpecPipeline.run(context);

        if (context.isFailure()) {
            throw new PipelineBuilderException ("The pipeline specification is malformed, please correct: " + context.getEventLog().toString());
        }

        return context;
    }
}
