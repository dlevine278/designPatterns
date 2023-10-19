package org.dplevine.patterns.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.*;

/** Primary data structure (a DTO) that is passed from one stage to another when invoking a pipeline.  This data structure contains
 * event log which is used exclusively by the framework itself for capturing key events during the invocation of a pipeline.
 *
 * Additionally, this data structure also contains a thread safe map (i.e., Map<String, Object>) for use by pipeline stages for passing data between stages.
 * The key is of type string and the values are of type object.
 * @author David Levine
 * @version 1.0
 * @since 1.0
 */
public class ExecutionContext {
    @JsonProperty(required = true)
    private Status status = ExecutionContext.Status.UNDEFINED;
    @JsonIgnore
    private final Map<String, Object> objects = Collections.synchronizedMap(new HashMap<>());
    @JsonProperty(required = true)
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
        @JsonProperty(required = true)
        private final String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        @JsonProperty(required = true)
        private final String id;
        @JsonProperty(required = true)
        private final String eventType;
        @JsonProperty(required = true)
        private final String details;

        Event(String id, String eventType, String details) {
            this.id = id;
            this.eventType = eventType;
            this.details = details;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getId() { return id;}

        String getId(String superId) {
            if (superId.contains(id)) {
                return superId;
            } else {
                return "";
            }
        }

        public String getEventType() {
            return eventType;
        }

        public String getDetails() {
            return details;
        }

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return "{}";
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

    public List<Event> getExceptionEvents() {
        List<Event> filteredEvents = new Vector<>();
        eventLog.stream().filter(event -> event.getEventType() == EventType.EXCEPTION).forEach(event -> filteredEvents.add(event));
        return filteredEvents;
    }

    public List<Event> getStageEvents(String id) {
        List<Event> filteredEvents = new Vector<>();
        eventLog.stream().filter(event -> event.getId().equals(id)).forEach(event -> filteredEvents.add(event));
        return filteredEvents;
    }

    public Event getLastStageEvent(String id) {
        Event lastEvent = null;

        for (int i = eventLog.size(); i > 0; i--) {
            if (eventLog.get(i-1).getId().equals(id)) {
                return eventLog.get(i-1);
            }
        }
        return lastEvent;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }
}
