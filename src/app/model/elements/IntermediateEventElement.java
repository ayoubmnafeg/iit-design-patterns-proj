package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class IntermediateEventElement extends DiagramElement {
    public IntermediateEventElement(String id, double x, double y, String label) { super(id, x, y, label); }
    @Override public ElementType getType() { return ElementType.INTERMEDIATE_EVENT; }
}
