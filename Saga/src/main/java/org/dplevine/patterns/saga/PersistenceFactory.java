package org.dplevine.patterns.saga;

final class PersistenceFactory {

    PersistenceFactory() {
        // load the class associated with the configured persistence method - the fully qualified class name should be specified in an application property
    }

    PersistenceSession createSession() {
        // using the loaded persistence factory, ask it to create and return a new persistence session

        return null;
    }
}
