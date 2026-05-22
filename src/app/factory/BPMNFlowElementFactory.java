package app.factory;

import app.model.DiagramElement;
import app.model.elements.SequenceFlowElement;
import app.util.IdGenerator;



public class BPMNFlowElementFactory implements FlowElementFactory {


    @Override
    public DiagramElement createFlow(String sourceId, String targetId) {
        return createFlowWithStoredId(IdGenerator.generateId(), sourceId, targetId);
    }


    @Override
    public DiagramElement createFlowWithStoredId(String id, String sourceId, String targetId) {
        return new SequenceFlowElement(id, sourceId, targetId);
    }
}
