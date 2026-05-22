package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class TaskElement extends DiagramElement {
    public TaskElement(String id, double x, double y, String label) {
        super(id, x, y, label);
    }
    @Override public ElementType getType() { return ElementType.TASK; }
}
