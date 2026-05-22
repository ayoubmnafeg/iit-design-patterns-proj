package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class ErrorEndElement extends DiagramElement {
    public ErrorEndElement(String id, double x, double y, String label) { super(id, x, y, label); }
    @Override public ElementType getType() { return ElementType.ERROR_END; }
}
