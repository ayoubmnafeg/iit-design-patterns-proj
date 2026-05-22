package app.command;

import app.model.Diagram;
import app.model.DiagramElement;

public class AddElementCommand implements Command {
    private final Diagram diagram;
    private final DiagramElement element;

    public AddElementCommand(Diagram diagram, DiagramElement element) {
        this.diagram = diagram;
        this.element = element;
    }

    @Override public void execute() { diagram.addElement(element); }
    @Override public void undo()    { diagram.removeElement(element.getId()); }
}
