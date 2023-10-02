package org.dplevine.patterns.pipeline;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

class PipelineGenerator implements Stage {

    private enum StageType {
        Simple,
        Pipeline,
        Fork,
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
        spec.getStages().forEach(stage -> pipelineGraph.addVertex(stage.getId()));
        spec.getPipelines().forEach(pipeline -> pipelineGraph.addVertex(pipeline.getId()));
        spec.getForks().forEach(fork -> pipelineGraph.addVertex(fork.getId()));

        // start at the pipeline root and iterate through  all the steps definitions
        String startId = spec.getId();
        for(PipelineSpecification.StepDefinition step : spec.getSteps()) {
            pipelineGraph.addEdge(startId, step.getId());
            startId = step.getId();
        }

        // iterate through all the sub-pipeline (forkPipelines) definitions
        for(PipelineSpecification.PipelineDefinition pipeline : spec.getPipelines()) {
            String subPipelineStartId = pipeline.getId();
            for(PipelineSpecification.StepDefinition step : pipeline.getSteps()) {
                pipelineGraph.addEdge(subPipelineStartId, step.getId());
                startId = step.getId();
            }
        }

        // iterate through all the fork definitions
        for(PipelineSpecification.ForkDefinition fork : spec.getForks()) {
            String forkStartId = fork.getId();
            for(PipelineSpecification.PipelineDefinition forkPipeline : fork.getPipelines()) {
                pipelineGraph.addEdge(forkStartId, forkPipeline.getId());
                startId = forkPipeline.getId();
            }
        }

        return new CycleDetector<String, DefaultEdge>(pipelineGraph).detectCycles();
    }

    class SimpleStageBuilder {
        private StageBuilder stageBuilder;

        SimpleStageBuilder(String classpath, String className) throws Exception {
            try {
                ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
                Class<?> clazz = appClassLoader.loadClass(className);
                this.stageBuilder = (StageBuilder) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new Exception("Could not construct a StageBuilder for class: " + className);
            }
        }

        SimpleStageBuilder(StageBuilder stageBuilder) {
            this.stageBuilder = stageBuilder;
        }

        StageWrapper newStage(String id) throws Exception {
            Stage stage;
            if (stageBuilder == null) {
                throw new Exception("No StageBuilder found for: " + id);
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

        for (PipelineSpecification.StageDefinition stage : spec.getStages()) {
            vertices.put(stage.getId(), new PipelineVertex(stage.getId(), new SimpleStageBuilder(stage.getClassPath(), stage.getClassName()).newStage(stage.getId()), StageType.Simple));
        }
        spec.getPipelines().forEach(pipeline -> vertices.put(pipeline.getId(),  new PipelineVertex(pipeline.getId(), new Pipeline(pipeline.getId()), StageType.Pipeline)));
        spec.getForks().forEach(fork -> vertices.put(fork.getId(), new PipelineVertex(fork.getId(), new Fork(fork.getId()), StageType.Fork)));

        // add the stages to the sub-piplines
        for(PipelineSpecification.PipelineDefinition pipelineDef : spec.getPipelines()) {
            Pipeline pipeline = (Pipeline) vertices.get(pipelineDef.getId()).getStage();
            for(PipelineSpecification.StepDefinition stepDef : pipelineDef.getSteps()) {
                pipeline.addStage(vertices.get(stepDef.getId()).getStage());
            }
        }

        // add the sub-piplines to the forks
        for(PipelineSpecification.ForkDefinition forkDef : spec.getForks()) {
            Fork fork = (Fork) vertices.get(forkDef.getId()).getStage();
            for(PipelineSpecification.PipelineDefinition forkPipelineDef : forkDef.getPipelines()) {
                fork.addPipeline((Pipeline) vertices.get(forkPipelineDef.getId()).getStage());
            }
        }

        return vertices;
    }

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        PipelineSpecification spec = (PipelineSpecification)  context.getObject(BuilderContext.PIPELINE_SPEC);


        // 0. create an empty pipeline
        Pipeline pipeline = new Pipeline(spec.getId());


        // 1. verify that it is acyclic
        if (isPipelineAcyclic(spec)) {
            throw new Exception("Pipeline specification is not acyclic, please correct and try again");
        }

        // 2. construct all the vertices (i.e., stages, sub-pipelines, forks) in the spec and build
        Map<String, PipelineVertex> pipelineVertices = generateVertices(spec);

        // 3. create and return the pipeline
        spec.getSteps().forEach(stepDef -> pipeline.addStage(pipelineVertices.get(stepDef.getId()).getStage()));

        // 4. add the constructed pipeline to the context
        context.addObject(BuilderContext.PIPELINE, pipeline);
        return context;
    }
}