package org.dplevine.patterns.pipeline;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.Vector;

/**
 * The StageWrapper class is an essential part of the pipeline framework, serving as a wrapper for individual stages.
 * It provides a way to execute externally defined stages and manage initialization and cleanup routines, acting as a bridge
 * between the framework and user-defined stages.
 */
class StageWrapper implements Stage {

    private String id;
    private Stage stage;

    private List<StageWrapperCallback> initCallbacks = new Vector<>();
    private List<StageWrapperCallback> closeCallbacks = new Vector<>();

    private List<StageCallback> preStageCallbacks = new Vector<>();
    private List<StageCallback> postStageCallbacks = new Vector<>();

    //ctor
    private StageWrapper() {}

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
    ExecutionContext init(ExecutionContext context) throws Exception {
        for(StageWrapperCallback callback : initCallbacks) {
            callback.onEvent(this, context);
        }

        for (StageCallback callback : preStageCallbacks) {
            callback.onEvent(id, stage, context);
        }

        return context;}

    // hook to allow the pipeline to do some cleanup - by default does nothing
    ExecutionContext close(ExecutionContext context) throws Exception {
        for(StageWrapperCallback callback : closeCallbacks) {
            callback.onEvent(this, context);
        }

        for (StageCallback callback : postStageCallbacks) {
            callback.onEvent(id, stage, context);
        }

        return context;}

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        if (stage != null) {  // treat a null stage like a no-op
            return stage.doWork(context);
        }
        return context;
    }

    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        pipelineGraph.addVertex(id);
        pipelineGraph.addEdge(root, id);
        return id;
    }

    void clearInitCallbacks() {
        initCallbacks.clear();
    }

    void clearCloseCallbacks() {
        closeCallbacks.clear();
    }

    void registerInitCallback(StageWrapperCallback callback) {
        initCallbacks.add(callback);
    }

    void registerCloseCallback(StageWrapperCallback callback) {
        closeCallbacks.add(callback);
    }

    void registerPreStageCallback(String stageId, StageCallback callback) {
        if (id.equals(stageId)) {
            preStageCallbacks.add(callback);
        }
    }

    void registerPostStageCallback(String stageId, StageCallback callback) {
        if (id.equals(stageId)) {
            postStageCallbacks.add(callback);
        }
    }
}