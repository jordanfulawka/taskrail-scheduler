package ca.jordanfulawka.taskrail.core;

import ca.jordanfulawka.taskrail.handlers.JobHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerRegistry {

    private final Map<String, JobHandler> handlers = new HashMap<>();

    public HandlerRegistry(List<JobHandler> handlerList) {
        for(JobHandler handler : handlerList) {
            JobHandler existing = handlers.putIfAbsent(handler.type(), handler);
            if(existing != null) {
                throw new IllegalStateException(
                        "Duplicate job type '" + handler.type() + ": "
                        + existing.getClass().getSimpleName() + " and " + handler.getClass().getSimpleName()
                );
            }
        }
    }

    public JobHandler get(String type) {
        JobHandler handler = handlers.get(type);
        if(handler == null) {
            throw new IllegalArgumentException("No handler for type: " + type);
        }
        return handler;
    }

    public boolean supports(String type) {
        return handlers.containsKey(type);
    }

}
