**Pipeline**


<br>**_Background and Motivation_**

> **_What is a Pipeline_**
> The pipeline design pattern promotes separation of concerns and improves maintainability by encapsulating each step's logic in a separate component or class. 
> It also enables the easiness in extensibility and flexibility as new steps can be added and existing steps can be modified without affecting the overall pipeline.
> 
>The main idea behind the pipeline pattern is to create a set of operations (stages) assembled and arranged in a linear and acyclic manner, and pass data through it as each stage 
>executes. Although the 'Chain of Responsibility' and the 'Decorator' design patterns
> can handle this task partially, the main power of the pipeline is that it’s flexible about the type of its result.  
> 
> My implementation of the pipeline pattern consists of the following sub-constructs:
> 
>>_**Execution Context:**_ The ExecutionContext class is the primary data structure used throughout the pipeline for passing data (i.e., DTO) and maintaining a log of events during the execution of a pipeline. The class contains methods that stages can use to add and access data to the Execution Context.
>
>>_**Stage:**_ A Stage is a simple interface that defines a generic method that can be implemented by various stages or steps in a pipeline. 
> Each stage will take an input ExecutionContext, perform some work, and return an updated ExecutionContext. 
> The use of exceptions indicates that errors can occur during the execution of a stage.  Any class that is to be defined as part of a pipeline implements this interface.
> 
>> _**StageBuilder:**_ The StageBuiler interface is used for building and configuring stages in a pipeline (i.e., factory pattern). By implementing this interface, classes provide custom logic for creating instances of the Stage interface, 
> potentially with specific configuration parameters or settings.
> 
>>_**Parallel:**_ The Parallel is a framework class that can be defined as a pipeline stage that allows the execution of multiple Pipeline instances concurrently. 
> It allows you to define stages that are comprised of a group of pipelines to run in parallel and manages their execution.
> 
>>_**Step:**_
> 
>>_**Pipeline:**_ The Pipeline class is responsible for executing a series of stage instances in a specific order (i.e., Steps). It ensures that the stages are executed in a topological order, making it suitable for scenarios where stages have dependencies on one another.
>
>>_**PipelineBuilder:**_ The PipelineBuilder class is a factory responsible for constructing pipelines based on different input sources, such as JSON or YAML files or a PiplineSpecification object.  
> It encapsulates the work required to assemble and wire together pipelines.
>
>>_**PipelineSpecification:**_ The PipelineSpecification class is used to define a set of elements that collectively describe a pipeline's configuration.  
> Instances of this can be directly be created within your java code to dynamically construct piplines and/or are created under the covers by the PipelineBuilder class as part of parsing JSON and/or YAML pipeline specifications.



<br>**_How to Build_**

This project is a Maven project. 

>_**Dependencies:**_ 
> Please ensure the following dependencies are setup in your POM file
> 
> _NOTE:_ with the exception of JUnit 5, you may modify the versions of these dependencies in order to avoid conflicts with older/newer versions of the same 
> dependencies used in your larger project.
> 
```
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.7</version>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>2.14.2</version>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.14.2</version>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
            <version>2.14.2</version>
        </dependency>
        
        <dependency>
            <groupId>org.jgrapht</groupId>
            <artifactId>jgrapht-core</artifactId>
            <version>1.5.2</version>
        </dependency>
        
        <dependency>
            <groupId>org.jgrapht</groupId>
            <artifactId>jgrapht-ext</artifactId>
            <version>1.5.2</version>
        </dependency>
        
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-surefire-provider</artifactId>
            <version>1.3.2</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-suite</artifactId>
            <version>1.10.0</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.3.1</version>
            <scope>test</scope>
        </dependency>
```


<br>_**How to Implement Pipelines**_

> _**Declaring Pipelines using JSON or YAML**_
> 
> Sample JSON pipeline definition
> ```
> {
>  "id" : "Sample Pipeline JSON",
>  "fastFail" : false,
>  "stages" : [
>       {
>        "id" : "stage 0",
>        "className" : "org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage"
>       },
>       {
>        "id" : "stage 1",
>        "className" : "org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage"
>       },
>       {
>        "id" : "stage 2",
>        "className" : "org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage"
>       },
>       {
>        "id" : "stage 3",
>        "className" : "org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage"
>       },
>       {
>        "id" : "stage 4",
>        "className" : "org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage"
>       },
>       {
>        "id" : "stage 5",
>        "className" : "org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage"
>        },
>        {
>        "id" : "stage 6",
>        "className" : "org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage"
>        }
>           ],
>  "parallels" : [
>       {
>        "id" :"parallel 1",
>        "parallelPipelines" : [
>             {
>              "steps" : [
>                 "stage 4",
>                 "stage 6"
>              ]
>             },
>             {
>              "steps" : [
>                 "stage 3"
>              ]
>             }
>        ]
>       },
>       {
>        "id" :"parallel 2",
>        "parallelPipelines" : [
>             {
>              "steps" : [
>                 "stage 1"
>              ]
>             },
>             {
>              "steps" : [
>                 "stage 2"
>              ]
>             }
>        ]
>       }
>  ],
>  "steps" : [
>     "stage 0",
>     "parallel 1",
>     "parallel 2",
>     "stage 5"
>  ]
> }
>```
> 
> > Sample YAML pipeline definition
> ```
> ---
> id: pipeline graph pipeline
> fastFail: false
> stages:
> - id: stage 0
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
> - id: stage 1
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
> - id: stage 2
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
> - id: stage 3
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
> - id: stage 4
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
> - id: stage 5
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
> - id: stage 6
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
> - id: stage 7
>   className: org.dplevine.patterns.pipeline.examples.graph.PipelineGraph$TimerStage
>   parallels:
> - id: parallel 1
>   parallelPipelines:
>     - steps:
>         - stage 4
>         - stage 6
>     - steps:
>         - stage 3
> - id: parallel 2
>   parallelPipelines:
>     - steps:
>         - stage 1
>     - steps:
>         - stage 2
>         - stage 7
>           steps:
> - stage 0
> - parallel 1
> - parallel 2
> - stage 5
>```
> 
> _**Declaring Pipelines at runtime in your Java code**_
>```
>        PipelineBuilder builder = PipelineBuilder.createBuilder();
>        PipelineSpecification spec = new PipelineSpecification();
>        spec.setId("HelloWorld Example");
>
>        PipelineSpecification.StageDefinition stage1 = new PipelineSpecification.StageDefinition("stage 1", Hello.class.getName());
>        PipelineSpecification.StageDefinition stage2 = new PipelineSpecification.StageDefinition("stage 2", World.class.getName());
>        spec.setStages(new ArrayList<>(Arrays.asList(stage1, stage2)));
>        spec.setSteps(new ArrayList<>(Arrays.asList("stage 1", "stage 2")));
>
>        Pipeline pipeline = builder.buildFromPipelineSpecification(spec);
>```
> 
> _**Running Pipelines**_
> 
>> _**Modes of Execution**_
>>> fastFail
>>> 
>>> Detached vs. Same-Thread 
>
<br>_**How to Monitor Pipeline Execution and Status**_
>_**Rendering Pipelines**_
> 
> _**Callbacks**_
> 
> _**Event Log**_
>



<br>**_Putting It All Together: Hello World!_**

Here's the source code for a simple two stage pipeline that prints out Hello World! to the console. 
>```
>package org.dplevine.patterns.pipeline.examples.json;
>
>import org.dplevine.patterns.pipeline.*;
>import java.net.URL;
>
>/**
>* The JsonExample class demonstrates the use of your pipeline framework by creating a simple
>* "Hello, World" pipeline from a JSON configuration file.
>  */
>  public class JsonExample {
>
>  static final String PIPELINE_JSON = "/helloWorld.json";
>
>  static class HelloWorldContext extends ExecutionContext {
>  public static final String MESSAGE = "HELLO_WORLD_MESSAGE";
>
>       public HelloWorldContext() {
>           super();
>       }
>
>       public String getMessage() {
>           return (String) getObject(MESSAGE);
>       }
>
>       public void setMessage(String message) {
>           addObject(MESSAGE, message);
>       }
>  }
>
>
>    public static class Hello implements Stage, StageBuilder {
>        public Hello () {}
>
>        @Override
>        public ExecutionContext doWork(ExecutionContext context) throws Exception {
>            JsonExample.HelloWorldContext hwContext = (JsonExample.HelloWorldContext) context;
>            hwContext.setMessage("Hello ");
>            return context;
>        }
>
>        @Override
>        public Stage buildStage() {
>            return new JsonExample.Hello();
>        }
>    }
>
>
>    public static class World implements Stage, StageBuilder {
>
>        public World () {}
>
>        @Override
>        public ExecutionContext doWork(ExecutionContext context) throws Exception {
>            JsonExample.HelloWorldContext hwContext = (JsonExample.HelloWorldContext) context;
>            String message = hwContext.getMessage();
>            hwContext.setMessage(message + "World!");
>            return context;
>        }
>
>        @Override
>        public Stage buildStage() {
>            return new JsonExample.World();
>        }
>    }
>
>    public static void main(String args[]) throws Exception {
>        PipelineBuilder builder = PipelineBuilder.createBuilder();
>
>        URL url = JsonExample.class.getResource(PIPELINE_JSON);
>        Pipeline pipeline = builder.buildFromPathName(url.getPath());
>        JsonExample.HelloWorldContext context = new JsonExample.HelloWorldContext();
>        pipeline.run(context);
>        System.out.println(context.getMessage());
>    }
>}
>```



Here's the JSON doc referenced by the sample above, specifying how the pipeline is constructed:
>```
>{
> "id" : "hello world pipeline",
> "fastFail" : true,
> "stages" : [
>    {
>     "id" : "hello",
>     "className" : "org.dplevine.patterns.pipeline.examples.json.JsonExample$Hello"
>    },
>    {
>     "id" : "world",
>     "className" : "org.dplevine.patterns.pipeline.examples.json.JsonExample$World"
>    }
> ],
> "steps" : [
>    "hello",
>    "world"
> ]
>}
>```





