package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class MessageThrowElement extends DiagramElement {
    public MessageThrowElement(String id, double x, double y, String label) { super(id, x, y, label); }
    @Override public ElementType getType() { return ElementType.MESSAGE_THROW; }
}
