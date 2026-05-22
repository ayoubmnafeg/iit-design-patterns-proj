package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class ExclusiveGatewayElement extends DiagramElement {
    public ExclusiveGatewayElement(String id, double x, double y, String label) {
        super(id, x, y, label);
    }
    @Override public ElementType getType() { return ElementType.EXCLUSIVE_GATEWAY; }
}
