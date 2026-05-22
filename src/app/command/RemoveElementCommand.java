package app.command;

import app.model.Diagram;
import app.model.DiagramElement;
import java.util.ArrayList;
import java.util.List;

public class RemoveElementCommand implements Command {
    private final Diagram diagram;
    private final List<DiagramElement> elements;

    public RemoveElementCommand(Diagram diagram, DiagramElement element) {
        this.diagram = diagram;
        this.elements = List.of(element);
    }

    public RemoveElementCommand(Diagram diagram, List<DiagramElement> elements) {
        this.diagram = diagram;
        this.elements = new ArrayList<>(elements);
    }

    @Override
    public void execute() {
        elements.forEach(element -> diagram.removeElement(element.getId()));
    }

    @Override
    public void undo() {
        elements.forEach(diagram::addElement);
    }
}
