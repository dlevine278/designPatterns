package org.dplevine.patterns.saga;

final class TransportFactory {

    TransportFactory() throws Exception {
        // load the class associated with the configured transport - the fully qualified class name should be specified in an application property
    }

    TransportSession createSession() {
        // using the loaded transport factory, ask it to create and return a new transport session
        return null;
    }
}
