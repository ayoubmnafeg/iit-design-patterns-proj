package app.command;

import app.model.Diagram;
import app.model.DiagramElement;

public class RemoveElementCommand implements Command {
    private final Diagram diagram;
    private final DiagramElement element;

    public RemoveElementCommand(Diagram diagram, DiagramElement element) {
        this.diagram = diagram;
        this.element = element;
    }

    @Override public void execute() { diagram.removeElement(element.getId()); }
    @Override public void undo()    { diagram.addElement(element); }
}
