package org.dplevine.patterns.saga;

public final class SagaCoordinator extends SagaParticipant {
    private static SagaCoordinator sagaCoordinator;
    static {
        try {
            sagaCoordinator = new SagaCoordinator(); // singleton
        } catch (Exception e) {

        }
    }
    private TransportSidecar transportSidecar;
    private PersistenceSidecar persistenceSidecar = new PersistenceSidecar();
    private final SagaDefinitionRepo sagaDefinitionRepo = new SagaDefinitionRepo(persistenceSidecar);

    public static SagaCoordinator getSagaCoordinator() {
        return sagaCoordinator;
    }

    private SagaCoordinator() throws Exception {
        super();
        this.transportSidecar = new TransportSidecar(this);
    }
}
