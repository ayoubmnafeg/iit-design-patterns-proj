package app.factory;

import app.model.DiagramElement;
import app.model.ElementType;

// Design Pattern: Factory Method
// Purpose: Creates BPMN diagram elements by delegating to node/flow factories from BPMNElementFamilyFactory (Abstract Factory).
public class BPMNElementFactory implements ElementFactory {

    private final BPMNElementFamilyFactory family = new DefaultBPMNElementFamilyFactory();

    // Factory Method: delegates to the node factory from the current element family.
    @Override
    public DiagramElement createElement(ElementType type, double x, double y) {
        return family.nodes().createElement(type, x, y);
    }

    // Factory Method: delegates to the flow factory from the current element family.
    public DiagramElement createFlow(String sourceId, String targetId) {
        return family.flows().createFlow(sourceId, targetId);
    }
}
