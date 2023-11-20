package org.dplevine.patterns.saga;

final class TransportSidecar {
    private final TransportFactory transportFactory;
    private final TransportSession transportSession;
    private SagaParticipant sagaParticipant;

    TransportSidecar(SagaParticipant sagaParticipant) throws Exception {
        this.sagaParticipant = sagaParticipant;
        transportFactory = new TransportFactory();
        transportSession = transportFactory.createSession();
    }

    // method to register saga transactions

    // method to forward saga transaction dispatch calls to the saga participant
}
