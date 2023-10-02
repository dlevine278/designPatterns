package org.dplevine.patterns.pipeline;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

class StageWrapper implements Stage {

    private String id;
    private Stage stage;

    //ctor
    StageWrapper(String id) {
        this.id = id;
    }

    StageWrapper(String id, Stage stage) {
        this.id = id;
        this.stage = stage;
    }

    //setters and getters
    void setId(String id) {
        this.id = id;
    }

    String getId() {
        return id;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    Stage getStage() {
        return stage;
    }

    // hook to allow the pipeline to do some initialization
    ExecutionContext init(ExecutionContext context) { return context;}

    // hook to allow the pipeline to do some cleanup - by default does nothing
    ExecutionContext close(ExecutionContext context) {return context;}

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        return stage.doWork(context);
    }

    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        pipelineGraph.addVertex(id);
        pipelineGraph.addEdge(root, id);
        return id;
    }
}