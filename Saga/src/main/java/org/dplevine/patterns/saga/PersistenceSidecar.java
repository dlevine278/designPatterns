package org.dplevine.patterns.saga;

final class PersistenceSidecar {
    private final PersistenceFactory persistenceFactory = new PersistenceFactory();
    private final PersistenceSession persistenceSession = persistenceFactory.createSession();

    PersistenceSidecar() {

    }
}
