package org.dplevine.patterns.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

class SpecFromYamlGenerator implements Stage {
    private static Logger logger = LoggerFactory.getLogger(SpecFromYamlGenerator.class);

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        String pathname = (String) context.getObject(BuilderContext.SPEC_PATHNAME);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            //mapper.findAndRegisterModules();
            PipelineSpecification spec = mapper.readValue(new File(pathname), PipelineSpecification.class);
            context.addObject(BuilderContext.PIPELINE_SPEC, spec);

        } catch (Exception e) {
            logger.error("stack trace:" + e.getLocalizedMessage());
            logger.error("pipeline event log:" + context.getEventLog().toString());
        }
        return context;
    }
}