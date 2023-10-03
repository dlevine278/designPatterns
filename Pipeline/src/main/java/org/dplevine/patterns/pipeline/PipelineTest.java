package org.dplevine.patterns.pipeline;

public class PipelineTest implements Stage, StageBuilder {
    static Pipeline pipeline;

    public static void main(String args[]) {
        try {
            pipeline = PipelineBuilder.createBuilder().buildFromPathName("/Users/dplevine/Desktop/example 3.json");
            ExecutionContext context = new ExecutionContext();
            context.addObject("pipeline", pipeline);
            pipeline.run(context);
            pipeline.render("/Users/dplevine/Desktop/example", Pipeline.ImageType.GIF);
            System.out.println(context.getEventLog());
            System.out.println(context.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        Thread.sleep(1000);
        ((Pipeline)context.getObject("pipeline")).render("/Users/dplevine/Desktop/temp", Pipeline.ImageType.GIF);
        return context;
    }

    @Override
    public Stage buildStage() {
        return new PipelineTest();
    }
}
