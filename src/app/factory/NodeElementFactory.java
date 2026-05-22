package app.factory;

import app.model.DiagramElement;
import app.model.ElementType;

// Design Pattern: Factory Method
// Purpose: Abstraction for creating BPMN node-shaped elements (events, tasks, gateways).
public interface NodeElementFactory {

    // Factory Method: creates a new node with a generated id.
    DiagramElement createElement(ElementType type, double x, double y);

    // Factory Method: reconstructs a node from persisted identity and geometry.
    DiagramElement createElementWithStoredId(
            ElementType type, String id, double x, double y, String label);
}
