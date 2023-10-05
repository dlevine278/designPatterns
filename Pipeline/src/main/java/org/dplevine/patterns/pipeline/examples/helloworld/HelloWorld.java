package org.dplevine.patterns.pipeline.examples.helloworld;

import org.dplevine.patterns.pipeline.*;

import java.util.ArrayList;
import java.util.Arrays;

public class HelloWorld {


    static class HelloWorldContext extends ExecutionContext {
        public static final String MESSAGE = "HELLO_WORLD_MESSAGE";

        public HelloWorldContext() {
            super();
        }

        public String getMessage() {
            return (String) getObject(MESSAGE);
        }

        public void setMessage(String message) {
            addObject(MESSAGE, message);
        }
    }


    public static class Hello implements Stage, StageBuilder {
        public Hello () {}

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            HelloWorldContext  hwContext = (HelloWorldContext) context;

            hwContext.setMessage("Hello ");

            return context;
        }

        @Override
        public Stage buildStage() {
            return new Hello();
        }
    }


    public static class World implements Stage, StageBuilder {

        public World () {}

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            HelloWorldContext  hwContext = (HelloWorldContext) context;

            String message = hwContext.getMessage();

            hwContext.setMessage(message + "World!");

            return context;
        }

        @Override
        public Stage buildStage() {
            return new World();
        }
    }


    public static void main(String args[]) throws Exception {
        PipelineBuilder builder = PipelineBuilder.createBuilder();
        PipelineSpecification spec = new PipelineSpecification();
        spec.setId("HelloWorld Example");

        PipelineSpecification.StageDefinition stage1 = new PipelineSpecification.StageDefinition("stage 1", "", Hello.class.getName());
        PipelineSpecification.StageDefinition stage2 = new PipelineSpecification.StageDefinition("stage 2", "", World.class.getName());
        spec.setStages(new ArrayList<>(Arrays.asList(stage1, stage2)));
        spec.setSteps(new ArrayList<>(Arrays.asList("stage 1", "stage 2")));

        Pipeline pipeline = builder.buildFromPipelineSpecification(spec);

        HelloWorldContext context = new HelloWorldContext();
        pipeline.run(context);
        System.out.println(context.getMessage());
    }
}
