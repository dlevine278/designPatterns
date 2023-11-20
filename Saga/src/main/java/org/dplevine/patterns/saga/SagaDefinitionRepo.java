package org.dplevine.patterns.saga;

final class SagaDefinitionRepo {
    private PersistenceSidecar persistenceSidecar;

    private SagaDefinitionRepo() {}

    SagaDefinitionRepo(PersistenceSidecar persistenceSidecar) {
        this.persistenceSidecar = persistenceSidecar;
    }
}
