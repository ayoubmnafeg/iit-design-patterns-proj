package app.factory;

import app.model.DiagramElement;
import app.model.ElementType;



public class BPMNElementFactory implements ElementFactory {

    private final BPMNElementFamilyFactory family = new DefaultBPMNElementFamilyFactory();


    @Override
    public DiagramElement createElement(ElementType type, double x, double y) {
        return family.nodes().createElement(type, x, y);
    }


    public DiagramElement createFlow(String sourceId, String targetId) {
        return family.flows().createFlow(sourceId, targetId);
    }
}
