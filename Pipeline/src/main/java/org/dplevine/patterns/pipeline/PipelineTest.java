package org.dplevine.patterns.pipeline;

public class PipelineTest implements Stage, StageBuilder {

    public static void main(String args[]) {
        try {
            Pipeline pipeline = PipelineBuilder.createBuilder().buildFromPathName("/Users/dplevine/Desktop/example 3.json");
            ExecutionContext context = pipeline.run();
            pipeline.render("/Users/dplevine/Desktop/example", Pipeline.ImageType.GIF);
            System.out.println(context.getEventLog());
            System.out.println(context.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        //Thread.sleep(1000);
        return context;
    }

    @Override
    public Stage buildStage() {
        return new PipelineTest();
    }
}
