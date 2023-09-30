package org.dplevine.patterns.pipeline;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

class StageWrapper implements Stage {

    private String id = "";
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
    }

    String getId() {
        return id;
    }

    String getStageClassName() {
        return stage.getClass().getCanonicalName();
    }

    // hook to allow the pipeline to do some initialization
    void init(ExecutionContext context) {
    }

    // hook to allow the pipeline to do some cleanup - by default does nothing
    void close(ExecutionContext context) {}

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