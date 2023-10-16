package org.dplevine.patterns.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The SpecFromJsonGenerator class is responsible for generating a PipelineSpecification object from a JSON file. The class
 * performs the important task of reading a JSON file and creating a PipelineSpecification object from its content; handling
 * JSON deserialization, logging of error messages when exceptions occur, and makes the parsed object available in the
 * context for further processing.
 */
class SpecFromJsonGenerator implements Stage {

    private static final Logger logger = LoggerFactory.getLogger(SpecFromJsonGenerator.class);

    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {
        String pathname = (String) context.getObject(BuilderContext.SPEC_PATHNAME);

        try {
            ObjectMapper mapper = new ObjectMapper();
            PipelineSpecification spec = mapper.readValue(new File(pathname), PipelineSpecification.class);
            context.addObject(BuilderContext.PIPELINE_SPEC, spec);

        } catch (Exception e) {
            logger.error("stack trace:" + e.getLocalizedMessage());
            logger.error("pipeline event log:" + context.getEventLog().toString());
            throw new PipelineBuilderException(e);
        }
        return context;
    }
}
