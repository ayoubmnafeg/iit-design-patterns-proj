package app.factory;

import app.model.DiagramElement;
import app.model.elements.SequenceFlowElement;
import app.util.IdGenerator;

// Design Pattern: Factory Method
// Purpose: Instantiates sequence flow elements.
public class BPMNFlowElementFactory implements FlowElementFactory {

    // Factory Method: creates a new sequence flow with a generated id.
    @Override
    public DiagramElement createFlow(String sourceId, String targetId) {
        return createFlowWithStoredId(IdGenerator.generateId(), sourceId, targetId);
    }

    // Factory Method: restores a flow when loading from the database.
    @Override
    public DiagramElement createFlowWithStoredId(String id, String sourceId, String targetId) {
        return new SequenceFlowElement(id, sourceId, targetId);
    }
}
