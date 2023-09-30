package org.dplevine.patterns.pipeline;

import java.text.SimpleDateFormat;
import java.util.*;

public class ExecutionContext {

    private Status status = ExecutionContext.Status.UNDEFINED;
    private final Map<String, Object> objects = Collections.synchronizedMap(new HashMap<>());
    private final List<Event> eventLog = new Vector<>();

    public enum Status {
        SUCCESS,
        FAILURE,
        IN_PROGRESS,
        UNDEFINED,
    }

    public interface EventType {
        String PIPELINE_IN_PROGRESS = "PIPELINE_IN_PROGRESS";
        String CALLING_STAGE = "CALLING_STAGE";
        String CALLED_STAGE = "CALLED_STAGE";
        String SUCCESS = "SUCCESS";
        String FAILURE = "FAILURE";
        String EXCEPTION = "EXCEPTION";
    }

    public static class Event {
        private final String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        private final String id;
        private final String eventType;
        private final String details;

        public Event(String id, String eventType, String details) {
            this.id = id;
            this.eventType = eventType;
            this.details = details;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getId() { return id;}

        public String getEventType() {
            return eventType;
        }

        public String getDetails() {
            return details;
        }

        @Override
        public String toString() {
            return timestamp + " " + eventType + " " + details + "\n";
        }
    }

    ExecutionContext createEvent(StageWrapper stage, String eventType, String details) {
        eventLog.add(new Event(stage.getId(), eventType, details));
        return this;
    }

    public List<Event> getEventLog() {
        return eventLog;
    }

    // default constructor
    public ExecutionContext() {
    }

    public void addObject(String key, Object object) {
        objects.put(key, object);
    }

    public void clearAllObjects() {
        objects.clear();
    }

    public Object getObject(String key) {
        return objects.get(key);
    }

    public boolean isUndefined() {
        return status == Status.UNDEFINED;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isFailure() {
        return status == Status.FAILURE;
    }

    public boolean isInProgress() { return status == Status.IN_PROGRESS; }

    void setFailure() {
        status = Status.FAILURE;
    }

    void setSuccess() {
        status = Status.SUCCESS;
    }

    void setInProgress() { status = Status.IN_PROGRESS; }

    public Status getStatus() {
        return status;
    }
}
