package app.factory;

import app.model.DiagramElement;
import app.model.ElementType;

// Design Pattern: Factory Method
// Purpose: Abstract creator for canvas-placed BPMN node elements.
public interface ElementFactory {
    DiagramElement createElement(ElementType type, double x, double y);
}
