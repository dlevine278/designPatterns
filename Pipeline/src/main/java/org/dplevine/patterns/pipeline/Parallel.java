package org.dplevine.patterns.pipeline;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * The Parallel class is a component that can be used to execute multiple Pipeline instances concurrently.
 * It allows the definition of a group of pipelines to run in parallel and manage their execution.
 */
final class Parallel extends StageWrapper { // will change visibility once the builder is complete

    private static final int THREADPOOL_SIZE = 10;  // size of threadpool = number of concurrent pipelines that can run at any given time
    static final String PARALLEL_START_TAG  = " - <Parallel>";
    static final String PARALLEL_END_TAG = " - </Parallel>";
    private static final Logger logger = LoggerFactory.getLogger(Parallel.class);

    private ExecutorService executorService;
    private List<Future<ExecutionContext>> futures;
    private final List<Pipeline> parallelPipelines = new Vector<>();

    //ctors
    Parallel(String id) {
        super(id);
    }

    Parallel addParallelPipeline(Pipeline parallelPipeline) {
        parallelPipelines.add(parallelPipeline);
        return this;
    }

    // or we can add lists stages
    Parallel addParallelPipelines(List<Pipeline> parallelPipelines) {
        this.parallelPipelines.addAll(parallelPipelines);
        return this;
    }

    List<Pipeline> getParallelPipelines() {
        return parallelPipelines;
    }

    @Override
    final ExecutionContext init(ExecutionContext context) throws Exception {
        context = super.init(context); // must always call super's init first (invokes callbacks)

        setStage(this);
        futures = null;
        executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE); // this is what manages the concurrency
        return context;
    }

    @Override
    final ExecutionContext close(ExecutionContext context) throws Exception {
        context = super.close(context); // must always call super's close first (invokes callbacks)

        executorService.shutdown();  // clean up all the threads (after execution has completed)
        return context;
    }

    // because this class is derived from StageWrapper, it implements the doWork interface (abstract method)
    // this method is what kicks off and manages the concurrent execution of all of the sub-stages
    // it does not return until either all sub-stages have completed OR an exception is thrown
    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {

        parallelPipelines.forEach(parallelPipeline -> parallelPipeline.setContext(context));

        List<String> parallelPipelineIds = new Vector<>();  // for the event log
        parallelPipelines.forEach( parallelPipeline -> parallelPipelineIds.add(parallelPipeline.getId()));
        try {
            futures = executorService.invokeAll(parallelPipelines);  // execute all the pipelines being ran in parallel (order of execution is non-deterministic) - this is a blocking call
            if (futures != null) {
                for (Future<ExecutionContext> future : futures) {
                    future.get();  // will throw if one of the parallel pipelines threw
                }
            }
        } catch (Exception e) {
            logger.error("Parallel execution failed: " + e.getLocalizedMessage());
            throw new PipelineExecutionException(e);
        }
        return context;
    }

    @Override
    void registerPreStageCallback(String stageId, StageCallback callback) {
        super.registerPreStageCallback(stageId, callback);
        parallelPipelines.forEach(pipeline -> pipeline.registerPreStageCallback(stageId, callback));
    }

    @Override
    void registerPostStageCallback(String stageId, StageCallback callback) {
        super.registerPostStageCallback(stageId, callback);
        parallelPipelines.forEach(pipeline -> pipeline.registerPostStageCallback(stageId, callback));
    }

    // method for constructing the Parallel subgraph vertices and inbound edge as part of the overall pipeline graph
    @Override
    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        String startId = this.getId() + PARALLEL_START_TAG;
        String endId = this.getId() + PARALLEL_END_TAG;
        pipelineGraph.addVertex(startId);
        pipelineGraph.addVertex(endId);

        pipelineGraph.addEdge(root, startId);
        parallelPipelines.forEach(parallelPipeline -> pipelineGraph.addEdge(parallelPipeline.buildGraph(startId, pipelineGraph),endId));
        return endId;  // new root
    }
}
