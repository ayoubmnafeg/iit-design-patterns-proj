package app.factory;

import app.model.DiagramElement;

// Design Pattern: Factory Method
// Purpose: Abstraction for creating BPMN flow elements (sequence flows).
public interface FlowElementFactory {

    // Factory Method: creates a sequence flow between two element ids.
    DiagramElement createFlow(String sourceId, String targetId);

    // Factory Method: restores a sequence flow with persisted id.
    DiagramElement createFlowWithStoredId(String id, String sourceId, String targetId);
}
