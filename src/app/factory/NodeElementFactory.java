package app.factory;

import app.model.DiagramElement;
import app.model.ElementType;



public interface NodeElementFactory {


    DiagramElement createElement(ElementType type, double x, double y);


    DiagramElement createElementWithStoredId(
            ElementType type, String id, double x, double y, String label);
}
