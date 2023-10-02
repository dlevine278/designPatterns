package org.dplevine.patterns.pipeline;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public final class Pipeline extends StageWrapper implements Callable<ExecutionContext> {

    private static Logger logger = LoggerFactory.getLogger(Pipeline.class);
    private List<StageWrapper> stages = new Vector<>();
    private ExecutionContext context;
    private Boolean fastFail = true; // true by default

    public static enum ImageType {
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

    Pipeline(String id, List<StageWrapper> stages) {
        super(id);
        this.stages.addAll(stages);
    }

    Pipeline(String id, boolean fastFail) {
        super(id);
        this.fastFail = fastFail;
    }

    Pipeline(ExecutionContext context, String id, boolean fastFail) {
        super(id);
        this.fastFail = fastFail;
        this.context = context;
    }

    Pipeline(String id, boolean fastFail, List<StageWrapper> stages) {
        super(id);
        this.fastFail = fastFail;
        this.stages.addAll(stages);
    }

    Pipeline(ExecutionContext context, String id, boolean fastFail, List<StageWrapper> stages) {
        super(id);
        this.fastFail = fastFail;
        this.stages.addAll(stages);
        this.context = context;
    }

    //setters and getters
    void setContext(ExecutionContext context) {
        this.context = context;
    }

    ExecutionContext getContext() {
        return context;
    }

    // we can incrementally add stages as well
    Pipeline addStage(StageWrapper stage) {
        stages.add(stage);
        return this;
    }

    Pipeline addStages(List<StageWrapper> stages) {
        this.stages.addAll(stages);
        return this;
    }

    List<StageWrapper> getStages() {
        return stages;
    }

    @Override
    // StageWrapper
    void init(ExecutionContext context) {
        setStage(this);
    }

    @Override
    // StageWrapper
    void close(ExecutionContext context) {

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
        for(Integer i = 1; i < stages.size(); i++) {
            pipeline.addEdge(stages.get(i-1), stages.get(i));
        }

        // 2. make sure this pipeline's graph is acyclic (probably redundant)
        if (new CycleDetector<StageWrapper, DefaultEdge>(pipeline).detectCycles()) {
            throw new Exception("The pipeline must be acyclic, cycles detected in pipeline: " + getId());
        }

        // 3. traverse the graph and invoke the stages along the way
        StageRunner runner = new StageRunner(context);
        Iterator<StageWrapper> iterator = new TopologicalOrderIterator<>(pipeline);
        while (iterator.hasNext()) {
            StageWrapper stage= iterator.next();

            try {
                stage.init(runner.getContext());
                runner.getContext().createEvent(stage, ExecutionContext.EventType.CALLING_STAGE, stage.getStage().getClass().getCanonicalName() + ".doWork()");
                runner.run(stage);
                stage.close(runner.getContext());
                runner.getContext().createEvent(stage, ExecutionContext.EventType.CALLED_STAGE, stage.getStage().getClass().getCanonicalName() + ".doWork()");
            } catch (Exception e) {
                runner.getContext().createEvent(stage, ExecutionContext.EventType.EXCEPTION, stage.getStage().getClass().getCanonicalName() + e.getLocalizedMessage());
                e.printStackTrace();
                stage.close(runner.getContext());
                if (fastFail) {
                    logger.error("stack trace: " + e.getLocalizedMessage());
                    throw new ExecutionException(e);
                }
            }
        }
        return runner.getContext();
    }

    public final ExecutionContext run() {
        ExecutionContext context = new ExecutionContext();
        run(context);
        return context;
    }

    public final ExecutionContext run(ExecutionContext context) {
        context.setInProgress();
        context.createEvent(this, ExecutionContext.EventType.PIPELINE_IN_PROGRESS, this.getClass().getName() + ".run()");
        try {
            context = doWork(context);
            context.setSuccess();
            context.createEvent(this, ExecutionContext.EventType.SUCCESS, this.getClass().getCanonicalName() + ".run()");
        } catch (Throwable t) {
            context.createEvent(this, ExecutionContext.EventType.EXCEPTION, t.toString());
            context.setFailure();
            context.createEvent(this, ExecutionContext.EventType.FAILURE, this.getClass().getCanonicalName() + ".run()");
            logger.error("Pipeline.run(contex) failed with error: " + context.getEventLog().toString());
        }
        return context;
    }

    // Callable abstract method
    @Override
    //Callable
    public ExecutionContext call() throws Exception {
        return doWork(context);
    }

    @Override
    //StageWrapper
    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        String startId = this.getId() + " - [Pipeline Start]";
        String endId = this.getId() + " - [Pipeline End]";
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
        Graph<String, DefaultEdge> pipelineGraph = buildPiplineGraph();

        JGraphXAdapter<String, DefaultEdge> graphAdapter =
                new JGraphXAdapter<String, DefaultEdge>(pipelineGraph);
        graphAdapter.getEdgeToCellMap().forEach((edge, cell) -> cell.setValue(null));
        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        return image;
    }
}

