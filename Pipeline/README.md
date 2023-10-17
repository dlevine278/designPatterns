**Pipeline**


**_Background and Motivation_**

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
>>_Stage:_ A stage is 
> 
>> _StageBuilder:_
> 
>>_Parallel:_
> 
>>_Step:_
> 


**_How to Build_**

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

_**How to Implement Pipelines**_

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
_**How to Monitor Pipeline Execution and Status**_
>_**Rendering Pipelines**_
> 
> _**Callbacks**_
> 
> _**Event Log**_





