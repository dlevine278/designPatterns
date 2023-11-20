package org.dplevine.patterns.saga;

import java.lang.reflect.Method;

final class SagaTransaction {
    private SagaParticipant sagaParticipant;
    private String transactionName;
    private Class<?> transactionClass;
    private Method transactionMethod;


    SagaTransaction(String transactionName, SagaParticipant sagaParticipant) {
        this.transactionName = transactionName;
        this.sagaParticipant = sagaParticipant;
    }

    SagaContext dispatchSagaTransaction(SagaContext context) {
        return context;
    }
}
