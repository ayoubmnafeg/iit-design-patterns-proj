package app.factory;

import app.model.DiagramElement;



public interface FlowElementFactory {


    DiagramElement createFlow(String sourceId, String targetId);


    DiagramElement createFlowWithStoredId(String id, String sourceId, String targetId);
}
