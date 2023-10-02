package org.dplevine.patterns.pipeline;

class BuilderContext extends ExecutionContext {
    public static final String SPEC_PATHNAME = "SPEC_PATHNAME";
    public static final String PIPELINE_SPEC = "PIPELINE_SPEC";
    public static final String PIPELINE = "PIPELINE";

    interface BuilderEvents extends ExecutionContext.EventType {
        public static String NULL_IDS_FOUND = "NULL_IDS_FOUND";
    }

    BuilderContext() {
        super();
    }
}
