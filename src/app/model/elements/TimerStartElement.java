package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class TimerStartElement extends DiagramElement {
    public TimerStartElement(String id, double x, double y, String label) { super(id, x, y, label); }
    @Override public ElementType getType() { return ElementType.TIMER_START; }
}
