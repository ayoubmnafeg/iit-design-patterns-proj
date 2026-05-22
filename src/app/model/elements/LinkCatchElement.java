package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class LinkCatchElement extends DiagramElement {
    public LinkCatchElement(String id, double x, double y, String label) { super(id, x, y, label); }
    @Override public ElementType getType() { return ElementType.LINK_CATCH; }
}
