package org.dplevine.patterns.pipeline;

/** Specialized execution context (derived from ExecutionContext) for the pipeline builder.
 * @author David Levine
 * @version 1.0
 * @since 1.0
 */
class BuilderContext extends ExecutionContext {
    public static final String SPEC_PATHNAME = "SPEC_PATHNAME";
    public static final String PIPELINE_SPEC = "PIPELINE_SPEC";
    public static final String PIPELINE = "PIPELINE";

    /** Builder specific events that will be reported when attempting to construct a new pipeline.
     **/
    interface BuilderEvents extends ExecutionContext.EventType {
        public static String NULL_IDS_FOUND = "NULL_IDS_FOUND";
    }

    /** Sole constructor. (For invocation by the PipelineBuilder class)
     */
    BuilderContext() {
        super();
    }
}
