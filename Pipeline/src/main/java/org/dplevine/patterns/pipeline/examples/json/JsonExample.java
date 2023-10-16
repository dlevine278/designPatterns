package org.dplevine.patterns.pipeline.examples.json;

import org.dplevine.patterns.pipeline.*;
import java.net.URL;

/**
 * The JsonExample class demonstrates the use of your pipeline framework by creating a simple
 * "Hello, World" pipeline from a JSON configuration file.
 */
public class JsonExample {

    static final String PIPELINE_JSON = "/helloWorld.json";

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
            JsonExample.HelloWorldContext hwContext = (JsonExample.HelloWorldContext) context;
            hwContext.setMessage("Hello ");
            return context;
        }

        @Override
        public Stage buildStage() {
            return new JsonExample.Hello();
        }
    }


    public static class World implements Stage, StageBuilder {

        public World () {}

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            JsonExample.HelloWorldContext hwContext = (JsonExample.HelloWorldContext) context;
            String message = hwContext.getMessage();
            hwContext.setMessage(message + "World!");
            return context;
        }

        @Override
        public Stage buildStage() {
            return new JsonExample.World();
        }
    }

    public static void main(String args[]) throws Exception {
        PipelineBuilder builder = PipelineBuilder.createBuilder();

        URL url = JsonExample.class.getResource(PIPELINE_JSON);
        Pipeline pipeline = builder.buildFromPathName(url.getPath());
        JsonExample.HelloWorldContext context = new JsonExample.HelloWorldContext();
        pipeline.run(context);
        System.out.println(context.getMessage());
    }
}
