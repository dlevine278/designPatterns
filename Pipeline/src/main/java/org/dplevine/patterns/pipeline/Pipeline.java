package org.dplevine.patterns.pipeline;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public final class Pipeline extends StageWrapper implements Callable<ExecutionContext> {

    private final String PIPELINE_START_TAG  = " - <Pipeline>";
    private final String PIPELINE_END_TAG = " - </Pipeline>";

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    private List<StageWrapper> stages = new Vector<>();
    private Boolean fastFail = true; // true by default
    private ExecutionContext context = new ExecutionContext();
    private ExecutorService executorService = null;

    private static class RootPipelineCloseCallback extends StageWrapperCallback {

        RootPipelineCloseCallback(StageWrapper stageWrapper) {
            super(stageWrapper);
        }

        @Override
        void doWork(StageWrapper stageWrapper, ExecutionContext context) {
            Pipeline pipeline = (Pipeline) stageWrapper;

            // set the status on the context accordingly now that the pipeline ran
            ExecutionContext.Event lastEvent = context.getLastStageEvent(pipeline.getId());
            if (lastEvent == null || lastEvent.getEventType() != ExecutionContext.EventType.EXCEPTION) {
                context.setSuccess();
                context.createEvent(pipeline, ExecutionContext.EventType.SUCCESS, this.getClass().getCanonicalName() + ".run()");
            } else {
                context.setFailure();
                context.createEvent(pipeline, ExecutionContext.EventType.FAILURE, this.getClass().getCanonicalName() + ".run()");
            }

            // clean up if this pipeline was ran in a detached thread
            if (pipeline.executorService != null) {
                pipeline.executorService.shutdown();
                pipeline.executorService = null;
            }
        }
    }

    public enum ImageType {
        JPEG,
        PNG,
        BMP,
        WBMP,
        GIF,
    }

    // ctors
    Pipeline(String id) {
        super(id);
    }

    Pipeline(String id, Boolean fastFail) {
        super(id);
        this.fastFail = fastFail;
    }

    //setters and getters
    void setContext(ExecutionContext context) {
        this.context = context;
    }

    // we can incrementally add stages as well
    Pipeline addStage(StageWrapper stage) {
        stages.add(stage);
        return this;
    }

    @Override
    // StageWrapper
    final ExecutionContext init(ExecutionContext context) throws Exception {
        context = super.init(context);
        setStage(this);
        return context;
    }

    @Override
    // StageWrapper
    final ExecutionContext close(ExecutionContext context) throws Exception {
        context = super.close(context);
        return context;
    }

    @Override
    //Stage
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        // 1. construct the graph
        Graph<StageWrapper, DefaultEdge> pipeline = new DirectedMultigraph<>(DefaultEdge.class);

        // add stages
        for (StageWrapper stage : stages) {
            pipeline.addVertex(stage);
        }

        // we start at index = 1 because the first entry (i.e., index = 0) is the root
        for(int i = 1; i < stages.size(); i++) {
            pipeline.addEdge(stages.get(i-1), stages.get(i));
        }

        // 2. make sure this pipeline's graph is acyclic (probably redundant)
        if (new CycleDetector<>(pipeline).detectCycles()) {
            throw new PipelineExecutionException("The pipeline must be acyclic, cycles detected in pipeline: " + getId());
        }

        // 3. traverse the graph and invoke the stages along the way
        StageRunner runner = new StageRunner(context);
        Iterator<StageWrapper> iterator = new TopologicalOrderIterator<>(pipeline);
        while (iterator.hasNext()) {
            StageWrapper stage= iterator.next();

            try {
                runner.run(stage);
            } catch (Exception e) {
                if (fastFail) {
                    logger.error("stack trace: " + e.getLocalizedMessage());
                    throw new PipelineExecutionException(e);
                }
            }
        }
        return runner.getContext();
    }

    public final ExecutionContext run() throws Exception {
        ExecutionContext context = new ExecutionContext();
        run(context);
        return context;
    }

    public final ExecutionContext run(ExecutionContext context) throws Exception {
        // prime the pipeline
        setContext(context);
        registerCloseCallback(new RootPipelineCloseCallback(this));
        context.setInProgress();
        context.createEvent(this, ExecutionContext.EventType.PIPELINE_IN_PROGRESS, this.getClass().getName() + ".run()");

        // execute the pipeline (returns when the pipeline fully executes --> ran in this thread)
        try {
            StageRunner runner = new StageRunner(context);
            context = runner.run(this);
        } catch (Exception e) {
            logger.error("Pipeline.run(context) failed with error: " + context.getEventLog().toString());
            throw e;
        }
        return context;
    }

    public final Future<ExecutionContext> runDetached() throws Exception {
        ExecutionContext context = new ExecutionContext();
        return runDetached(context);
    }

    public final Future<ExecutionContext> runDetached(ExecutionContext context) throws Exception {
        if (executorService != null) {
            throw new PipelineExecutionException("This pipeline was started but never shutdown; must invoke shutdownDetached() before running again.");
        }

        // prime the pipeline
        setContext(context);
        registerCloseCallback(new RootPipelineCloseCallback(this));
        context.setInProgress();
        context.createEvent(this, ExecutionContext.EventType.PIPELINE_IN_PROGRESS, this.getClass().getName() + ".run()");

        // run it detached (i.e., returns immediately and executes in a different thread)
        executorService = Executors.newFixedThreadPool(1);
        return executorService.submit(this);
    }

    // Callable abstract method
    @Override
    //Callable
    public ExecutionContext call() throws Exception {
        StageRunner runner = new StageRunner(context);
        return runner.run(this);
    }

    @Override
    //StageWrapper
    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        String startId = this.getId() + PIPELINE_START_TAG;
        String endId = this.getId() + PIPELINE_END_TAG;
        pipelineGraph.addVertex(startId);
        pipelineGraph.addVertex(endId);

        String subRoot = startId;
        if (root == null) {
            subRoot = startId;
        } else {
            pipelineGraph.addEdge(root, startId);
        }
        for (StageWrapper stage : stages) {
            subRoot = stage.buildGraph(subRoot, pipelineGraph);
        }
        pipelineGraph.addEdge(subRoot, endId);

        return endId;
    }

    Graph<String, DefaultEdge> buildPiplineGraph() {
        Graph<String, DefaultEdge> pipelineGraph = new DirectedMultigraph<>(DefaultEdge.class);
        buildGraph(null, pipelineGraph);
        return pipelineGraph;
    }

    public void render(String filepath, ImageType imageType) throws Exception {
        BufferedImage image = render();
        File imgFile = new File(filepath + "." + imageType.toString().toLowerCase());
        ImageIO.write(image, imageType.toString().toUpperCase(), imgFile);
    }

    public BufferedImage render() throws Exception {
        final String WHITE = "#ffffff";
        final String YELLOW = "#ffff00";
        final String GREEN = "#65fe08";
        final String RED = "#ff0000";

        Graph<String, DefaultEdge> pipelineGraph = buildPiplineGraph();
        JGraphXAdapter<String, DefaultEdge> graphAdapter =
                new JGraphXAdapter<>(pipelineGraph);
        graphAdapter.getEdgeToCellMap().forEach((edge, cell) -> cell.setValue(null));
        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // color the nodes based on activity (white - nothing, yellow - in progress, green - completed/success, red - failure)
        graphAdapter.getModel().beginUpdate();

        Map<String, mxICell> nodeMap = graphAdapter.getVertexToCellMap();
        Collection<mxICell> whites = nodeMap.values();
        Collection<mxICell> reds = new Vector<>();
        Collection<mxICell> greens = new Vector<>();
        Collection<mxICell> yellows = new Vector<>();
        Collection<mxICell> roots = new Vector<>();
        Collection<mxICell> parallels = new Vector<>();
        Collection<mxICell> pipelines = new Vector();

        List<ExecutionContext.Event> eventLog = context.getEventLog();
        for(String vertexId : pipelineGraph.vertexSet()) {
            mxICell cell = nodeMap.get(vertexId);
            for(int i = eventLog.size() - 1; i >=0; i--) {
                ExecutionContext.Event event = eventLog.get(i);
                if (vertexId.contains(event.getId())) {
                    switch (event.getEventType()) {
                        case ExecutionContext.EventType.CALLED_STAGE:
                        case ExecutionContext.EventType.SUCCESS:
                            greens.add(cell);
                            break;

                        case ExecutionContext.EventType.PIPELINE_IN_PROGRESS:
                        case ExecutionContext.EventType.CALLING_STAGE:
                            yellows.add(cell);
                            break;

                        case ExecutionContext.EventType.FAILURE:
                        case ExecutionContext.EventType.EXCEPTION:
                            reds.add(cell);
                            break;
                    }
                }
            }
            if (vertexId.contains(this.getId())) {
                roots.add(cell);
            } else if (vertexId.contains("Pipeline>")) {
                pipelines.add(cell);
            } else if (vertexId.contains("Parallel>")) {
                parallels.add(cell);
            }
        }

        try {
            graphAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, WHITE, whites.toArray());
            graphAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, YELLOW, yellows.toArray());
            graphAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, GREEN, greens.toArray());
            graphAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, RED, reds.toArray());

            graphAdapter.setCellStyles(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_RECTANGLE, roots.toArray());
            graphAdapter.setCellStyles(mxConstants.STYLE_FONTSTYLE, String.valueOf(mxConstants.FONT_BOLD), roots.toArray());

            graphAdapter.setCellStyles(mxConstants.STYLE_FONTSTYLE, String.valueOf(mxConstants.FONT_ITALIC), parallels.toArray());
            graphAdapter.setCellStyles(mxConstants.STYLE_FONTSTYLE, String.valueOf(mxConstants.FONT_ITALIC), pipelines.toArray());
        } finally {
            graphAdapter.getModel().endUpdate();
        }

        // return an image, the background color of the image is set to white
        return mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
    }

    @Override
    public void registerPreStageCallback(String stageId, StageCallback callback) {
        super.registerPreStageCallback(stageId, callback);
        for(StageWrapper stage : stages) {
                stage.registerPreStageCallback(stageId, callback);
        }
    }

    @Override
    public void registerPostStageCallback(String stageId, StageCallback callback) {
        super.registerPostStageCallback(stageId, callback);
        for(StageWrapper stage : stages) {
                stage.registerPostStageCallback(stageId, callback);
        }
    }
}

