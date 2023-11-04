package org.dplevine.patterns.pipeline;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class ParallelTest {
    private Parallel parallel;

    @Before
    public void setUp() {
        parallel = new Parallel("testParallel");
    }

    @Test
    public void testAddPipeline() {
        Pipeline pipeline = Mockito.mock(Pipeline.class);
        parallel.addParallelPipeline(pipeline);
        assertTrue(parallel.getParallelPipelines().contains(pipeline));
    }

    @Test
    public void testAddPipelines() {
        List<Pipeline> pipelines = new ArrayList<>();
        Pipeline pipeline1 = Mockito.mock(Pipeline.class);
        Pipeline pipeline2 = Mockito.mock(Pipeline.class);
        pipelines.add(pipeline1);
        pipelines.add(pipeline2);

        parallel.addParallelPipelines(pipelines);
        assertTrue(parallel.getParallelPipelines().contains(pipeline1));
        assertTrue(parallel.getParallelPipelines().contains(pipeline2));
    }

    @Test
    public void testDoWork() throws Exception {
        // Create a test ExecutionContext and set it up as needed for the test
        ExecutionContext context = new ExecutionContext();

        // Mock the Pipeline instances within Parallel
        Pipeline pipeline1 = Mockito.mock(Pipeline.class);
        Pipeline pipeline2 = Mockito.mock(Pipeline.class);

        // Set up the parallelPipelines list
        parallel.addParallelPipeline(pipeline1);
        parallel.addParallelPipeline(pipeline2);

        // Define expected behavior of the mocked Pipelines
        Mockito.when(pipeline1.call()).thenReturn(context);
        Mockito.when(pipeline2.call()).thenReturn(context);

        try {
            parallel.init(context);
            ExecutionContext resultContext = parallel.doWork(context);

            // Perform assertions based on expected results
            assertNotNull(resultContext);
            // Add more assertions as needed
        } catch (Exception e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        } finally {
            parallel.close(context);
        }
    }
}
