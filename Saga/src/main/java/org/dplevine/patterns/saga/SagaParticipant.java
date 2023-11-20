package org.dplevine.patterns.saga;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class SagaParticipant {
    private static SagaParticipant sagaPartacipant;
    private TransportSidecar transportSidecar;
    private Map<String, SagaTransaction> sagaTransactionMap = new ConcurrentHashMap<>();

    protected SagaParticipant() throws Exception {
        sagaPartacipant = new SagaParticipant();  // singleton
        transportSidecar = new TransportSidecar(this);

        // scan targeted packages for methods annotated with @SagaTransaction and create a SagaTransaction instance for each

        // register the final list of sagaTransactions with the sagaCoordinator
        registerSagaTransactions();
    }

    public SagaParticipant getSagaParticipant() {
        return sagaPartacipant;
    }

    void registerSagaTransaction(String sagaTransactionName, SagaTransaction sagaTransaction) throws Exception {
        // check to see if the sagaTransactionName is already in the keyset, throw if it is --> must be unique
        if (this.sagaTransactionMap.containsKey(sagaTransactionName)) {
            throw new Exception("sagaTransactionName(" + sagaTransactionName + ") can only be associated with one method.");
        }
        this.sagaTransactionMap.put(sagaTransactionName, sagaTransaction);
    }

    void registerSagaTransactions() {
        // using the transportSidecar, register all sagaTransactions with the SagaCoordinator
    }

    // Invoked by the sagaTransportSidecar
    SagaContext dispatchRequest(String sagaTransactionName, SagaContext sagaContext) {
        return sagaTransactionMap.get(sagaTransactionName).dispatchSagaTransaction(sagaContext);
    }
}

