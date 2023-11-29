package org.dplevine.patterns.pipeline;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class SpecTemplateBuilder {

    List<String> packages;
    Map<String, StageBuilderDef> stageBuilderDefs = new HashMap<>();
    Map<String, PipelineStepsDef> pipelineStepsDefs = new HashMap<>();

    private SpecTemplateBuilder() {}

    SpecTemplateBuilder(List<String> packages) {
        this.packages = Objects.requireNonNullElseGet(packages, Vector::new);
    }

    private static class StageBuilderDef {
        private final String id;
        private final String className;

        StageBuilderDef(String id, String className) {
            this.id = id;
            this.className = className;
        }

        String getId() {
            return id;
        }

        String getClassName() {
            return className;
        }
    }


    private static class PipelineStepsDef {
        private final String pipelineRootId;
        private final String parallelId;
        private List<String> steps;

        PipelineStepsDef(String pipelineRootId, String parallelId, String[] steps) {
            this.pipelineRootId = pipelineRootId;
            this.parallelId = parallelId;
            if (steps != null) {
                this.steps = new Vector<>(Arrays.asList(steps));
            }
        }

        String getPipelineRootId() {
            return pipelineRootId;
        }

        String getParallelId() {
            return parallelId;
        }

        List<String> getSteps() {
            return steps;
        }
    }

    private interface ScannerFunc {
        void doWork(Class<?> clazz) throws Exception ;
    }

    private void packageScaner(String packageName, ScannerFunc func, Class<? extends Annotation> annotation) throws Exception {
        for (Class<?> clazz : AnnotationScanner.scanClassesWithAnnotation(packageName, annotation)) {
            func.doWork(clazz);
        }
    }

    private final ScannerFunc scanForStageBuilders = (clazz) -> {
        for (StageBuilderDefinition stageBuilderDefinition : clazz.getAnnotation(StageBuilderDefinitions.class).value()) {
            StageBuilderDef stageBuilderDef = new StageBuilderDef(stageBuilderDefinition.id(), clazz.getName());
            stageBuilderDefs.put(stageBuilderDef.getId(), stageBuilderDef);
        }
    };

    private final ScannerFunc scanForPipelineSteps = (clazz) -> {
        int parallelIndex = 0;
        for (PipelineStepsDefinition pipelineSteps : clazz.getAnnotation(PipelineStepsDefinitions.class).value()) {
            if (pipelineSteps.parallelId().equals("") && pipelineStepsDefs.containsKey(pipelineSteps.pipelineRootId())) {
                throw new PipelineBuilderException("Scanned packages contain more than one PipelineSteps root definition for pipelineRootId = " + pipelineSteps.pipelineRootId());
            }
            String key = (pipelineSteps.parallelId().equals("")) ? pipelineSteps.pipelineRootId() : pipelineSteps.pipelineRootId() + parallelIndex++;
            PipelineStepsDef pipelineStepsDef = new PipelineStepsDef(pipelineSteps.pipelineRootId(), pipelineSteps.parallelId(), pipelineSteps.steps());
            pipelineStepsDefs.put(key, pipelineStepsDef);
        }
    };

    Map<String, PipelineSpecification> specTemplatesFromAnnotations() throws Exception {
        Map<String, PipelineSpecification> specTemplates = new ConcurrentHashMap<>();

        // scan each package for pipeline related annotations
        for (String packageName : packages) {
            // scan for StageBuilderDefinitions annotations and construct StageBuilderDefs
            packageScaner(packageName, scanForStageBuilders, StageBuilderDefinitions.class);

            packageScaner(packageName, scanForPipelineSteps, PipelineStepsDefinitions.class);
        }


        // Assemble what we found into pipeline specification templates

        //first, create empty pipeline specification templates
        List<PipelineStepsDef> pipelines = pipelineStepsDefs.values().stream().filter(pipelineStepDef -> pipelineStepDef.getParallelId().equals("")).collect(Collectors.toList());  // parallelId == "" --> root pipeline
        pipelines.forEach(pipeline -> specTemplates.put(pipeline.getPipelineRootId(), new PipelineSpecification(pipeline.getPipelineRootId())));  // create the pipeline specification template
        pipelines.forEach(pipeline -> specTemplates.get(pipeline.getPipelineRootId()).setSteps(pipeline.getSteps())); // set the steps on the pipeline specification template

        //next, create and add the parallels (if any) to the appropriate pipeline specification template
        List<PipelineStepsDef> allParallelDefs= pipelineStepsDefs.values().stream().filter(pipelineStepDef -> !pipelineStepDef.getParallelId().equals("")).collect(Collectors.toList()); // parallelId != "" --> parallel
        for (PipelineSpecification pipelineSpecification : specTemplates.values()) {  // for each pipeline specification template
            List<PipelineStepsDef> pipelineParallelDefs = allParallelDefs.stream().filter(parallelDef -> parallelDef.getPipelineRootId().equals(pipelineSpecification.getId())).collect(Collectors.toList());
            Map<String, PipelineSpecification.ParallelDefinition> parallelDefs = new ConcurrentHashMap<>();
            for (PipelineStepsDef parallelDef : pipelineParallelDefs) {
                if (!parallelDefs.containsKey(parallelDef.getParallelId())) {
                    parallelDefs.put(parallelDef.getParallelId(), new PipelineSpecification.ParallelDefinition());
                    parallelDefs.get(parallelDef.getParallelId()).setId(parallelDef.getParallelId());
                }
                parallelDefs.get(parallelDef.getParallelId()).addParallelPipeline(parallelDef.getSteps());
            }
            pipelineSpecification.setParallels(new Vector<>(parallelDefs.values()));
        }

        // finally, create and add the stage definitions to the appropriate pipeline specification template
        List<PipelineSpecification.StageDefinition> pipelineStageDefinitions = new Vector<>();
        stageBuilderDefs.values().forEach(stageBuilderDef -> pipelineStageDefinitions.add(new PipelineSpecification.StageDefinition(stageBuilderDef.getId(), stageBuilderDef.getClassName())));
        for (PipelineSpecification pipelineSpecification : specTemplates.values()) {
            pipelineSpecification.setStages(pipelineStageDefinitions);
        }

        return specTemplates;
    }
}