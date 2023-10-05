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

final class Parallel extends StageWrapper { // will change visibility once the builder is complete
    private final int THREADPOOL_SIZE = 10;  // size of threadpool = number of concurrent pipelines that can run at any given time
    private final String PARALLEL_START_TAG  = " - <Parallel>";
    private final String PARALLEL_END_TAG = " - </Parallel>";

    private ExecutorService executorService;
    private List<Future<ExecutionContext>> futures;
    private static final Logger logger = LoggerFactory.getLogger(Parallel.class);
    private final List<Pipeline> parallelPipelines = new Vector<>();
    private Boolean fastFail = true; // true by default

    //ctors
    Parallel(String id, Boolean fastFail) {
        super(id);
        this.fastFail = fastFail;
    }

    Parallel addPipeline(Pipeline parallelPipeline) {
        parallelPipelines.add(parallelPipeline);
        return this;
    }

    // or we can add lists stages
    Parallel addPipelines(List<Pipeline> parallelPipelines) {
        this.parallelPipelines.addAll(parallelPipelines);
        return this;
    }

    @Override
    final ExecutionContext init(ExecutionContext context) throws Exception {
        setStage(this);
        futures = null;
        executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE); // this is what manages the concurrency
        return context;
    }

    @Override
    final ExecutionContext close(ExecutionContext context) throws Exception {
        executorService.shutdown();  // clean up all the threads (after execution has completed)
        if (fastFail) {
            for (Future<ExecutionContext> future : futures) {
                future.get();  // may throw if the pipeline threw
            }
        }
        return context;
    }

    // because this class is derived from PipelineStage, it implements the doWork interface (abstract method)
    // this method is what kicks off and manages the concurrent execution of all of the sub-stages
    // it does not return until either all sub-stages have completed OR an exception is thrown
    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {

        parallelPipelines.forEach(parallelPipeline -> parallelPipeline.setContext(context));

        List<String> parallelPipelineIds = new Vector<>();  // for the event log
        parallelPipelines.forEach( parallelPipeline -> parallelPipelineIds.add(parallelPipeline.getId()));
        try {
            context.createEvent(this, ExecutionContext.EventType.CALLING_STAGE, "Concurrently invoking : " + parallelPipelineIds);
            List<Future<ExecutionContext>> future = executorService.invokeAll(parallelPipelines);  // execute all the pipelines being ran in parallel (order of execution is non-deterministic)
            context.createEvent(this, ExecutionContext.EventType.CALLED_STAGE, "Concurrently invoked pipelines: " + parallelPipelineIds);
        } catch (Exception e) {
            logger.error("Parallel execution failed: " + e.getLocalizedMessage());
            context.createEvent(this, ExecutionContext.EventType.EXCEPTION, "Exception in one of the pipelines belonging to parallel " + getId());
            throw new PipelineExecutionException(e);
        }
        return context;
    }

    @Override
    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        String startId = this.getId() + PARALLEL_START_TAG;
        String endId = this.getId() + PARALLEL_END_TAG;
        pipelineGraph.addVertex(startId);
        pipelineGraph.addVertex(endId);

        pipelineGraph.addEdge(root, startId);
        parallelPipelines.forEach(parallelPipeline -> pipelineGraph.addEdge(parallelPipeline.buildGraph(startId, pipelineGraph),endId));
        return endId;
    }
}
