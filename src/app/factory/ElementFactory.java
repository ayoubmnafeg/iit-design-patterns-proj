package app.factory;

import app.model.DiagramElement;
import app.model.ElementType;



public interface ElementFactory {
    DiagramElement createElement(ElementType type, double x, double y);
}
