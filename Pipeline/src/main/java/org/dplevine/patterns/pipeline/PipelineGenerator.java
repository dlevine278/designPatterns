package org.dplevine.patterns.pipeline;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

/**
 * The PipelineGenerator class is responsible for generating a pipeline based on a provided PipelineSpecification.
 */
class PipelineGenerator implements Stage {

    private enum StageType {
        Simple,
        Pipeline,
        Parallel,
    }

    private static class PipelineVertex {
        private String id;
        private StageWrapper stage;
        StageType stageType;

        PipelineVertex(String id, StageWrapper stage, StageType stageType) {
            this.id = id;
            this. stage = stage;
            this.stageType = stageType;
        }

        String getId() {
            return id;
        }

        StageWrapper getStage() {
            return stage;
        }

        StageType getStageType() {
            return stageType;
        }
    }

    private Boolean isPipelineAcyclic(PipelineSpecification spec) {
        // no steps in the pipeline --> no cycles
        if (spec.getSteps().size() == 0) {
            return false;
        }

        Graph<String, DefaultEdge> pipelineGraph = new DirectedMultigraph<>(DefaultEdge.class);

        // define all the vertices
        pipelineGraph.addVertex(spec.getId());
        spec.getStages().forEach(stageDef -> pipelineGraph.addVertex(stageDef.getId()));
        spec.getPipelines().forEach(pipelineDef -> pipelineGraph.addVertex(pipelineDef.getId()));
        spec.getParallels().forEach(parallelDef -> pipelineGraph.addVertex(parallelDef.getId()));

        // start at the pipeline root and iterate through  all the steps definitions
        String startId = spec.getId();
        for(String stepDef : spec.getSteps()) {
            pipelineGraph.addEdge(startId, stepDef);
            startId = stepDef;
        }

        // iterate through all the sub-pipeline (parallelPipelines) definitions
        for(PipelineSpecification.PipelineDefinition pipelineDef : spec.getPipelines()) {
            String subPipelineStartId = pipelineDef.getId();
            for(String stepDef : pipelineDef.getSteps()) {
                pipelineGraph.addEdge(subPipelineStartId, stepDef);
                startId = stepDef;
            }
        }

        // iterate through all the parallel definitions
        for(PipelineSpecification.ParallelDefinition parallelDef : spec.getParallels()) {
            String parallelStartId = parallelDef.getId();
            for(PipelineSpecification.PipelineDefinition parallelPipelineDef : parallelDef.getParallelPipelines()) {
                pipelineGraph.addEdge(parallelStartId, parallelPipelineDef.getId());
                startId = parallelPipelineDef.getId();
            }
        }

        return new CycleDetector<>(pipelineGraph).detectCycles();
    }

    static class SimpleStageBuilder {
        private StageBuilder stageBuilder;

        SimpleStageBuilder(String className) throws Exception {
            try {
                ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
                Class<?> clazz = appClassLoader.loadClass(className);
                this.stageBuilder = (StageBuilder) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new PipelineBuilderException("Could not construct a StageBuilder for class: " + className);
            }
        }

        StageWrapper newStage(String id) throws Exception {
            if (stageBuilder == null) {
                throw new PipelineBuilderException("No StageBuilder found for: " + id);
            }
            return new StageWrapper(id, stageBuilder.buildStage());
        }

        // getters
        StageBuilder getStageBuilder() {
            return stageBuilder;
        }
    }

    private Map<String, PipelineVertex> generateVertices(PipelineSpecification spec) throws Exception {
        Map<String, PipelineVertex> vertices = new HashMap<>();

        for (PipelineSpecification.StageDefinition stageDef : spec.getStages()) {
            vertices.put(stageDef.getId(), new PipelineVertex(stageDef.getId(), new SimpleStageBuilder(stageDef.getClassName()).newStage(stageDef.getId()), StageType.Simple));
        }
        spec.getPipelines().forEach(pipelineDef -> vertices.put(pipelineDef.getId(),  new PipelineVertex(pipelineDef.getId(), new Pipeline(pipelineDef.getId(), spec.getFastFail()), StageType.Pipeline)));
        spec.getParallels().forEach(parallelDef -> vertices.put(parallelDef.getId(), new PipelineVertex(parallelDef.getId(), new Parallel(parallelDef.getId(), spec.getFastFail()), StageType.Parallel)));

        // add the stages to the sub-piplines
        for(PipelineSpecification.PipelineDefinition pipelineDef : spec.getPipelines()) {
            Pipeline pipeline = (Pipeline) vertices.get(pipelineDef.getId()).getStage();
            pipelineDef.getSteps().forEach(stepDef -> pipeline.addStage(vertices.get(stepDef).getStage()));
        }

        // add the sub-piplines to the parallels
        for(PipelineSpecification.ParallelDefinition parallelDef : spec.getParallels()) {
            Parallel parallel = (Parallel) vertices.get(parallelDef.getId()).getStage();
            parallelDef.getParallelPipelines().forEach(parallelPipelineDef -> parallel.addParallelPipeline((Pipeline) vertices.get(parallelPipelineDef.getId()).getStage()));
        }

        return vertices;
    }

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        PipelineSpecification spec = (PipelineSpecification)  context.getObject(BuilderContext.PIPELINE_SPEC);


        // 0. create an empty pipeline
        Pipeline pipeline = new Pipeline(spec.getId(), spec.getFastFail());


        // 1. verify that it is acyclic
        if (isPipelineAcyclic(spec)) {
            throw new PipelineBuilderException("Pipeline specification is not acyclic, please correct and try again");
        }

        // 2. construct all the vertices (i.e., stages, parallel-pipelines, parallels) in the spec and build
        Map<String, PipelineVertex> pipelineVertices = generateVertices(spec);

        // 3. create and return the pipeline
        spec.getSteps().forEach(stepDef -> pipeline.addStage(pipelineVertices.get(stepDef).getStage()));

        // 4. add the constructed pipeline to the context
        context.addObject(BuilderContext.PIPELINE, pipeline);
        return context;
    }
}