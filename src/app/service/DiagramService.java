package app.service;

import app.command.AddElementCommand;
import app.command.CommandManager;
import app.command.RemoveElementCommand;
import app.dao.DiagramDAO;
import app.dao.DiagramDAOImpl;
import app.factory.BPMNElementFactory;
import app.model.Diagram;
import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.SequenceFlowElement;

import java.util.List;

public class DiagramService {
    private final Diagram diagram;
    private final BPMNElementFactory factory = new BPMNElementFactory();
    private final CommandManager commandManager = new CommandManager();
    private final LoggingService loggingService;

    public DiagramService(Diagram diagram, LoggingService loggingService) {
        this.diagram = diagram;
        this.loggingService = loggingService;
    }

    public DiagramElement addElement(ElementType type, double x, double y) {
        DiagramElement element = factory.createElement(type, x, y);
        commandManager.executeCommand(new AddElementCommand(diagram, element));
        loggingService.log("Added " + type + " at (" + (int)x + "," + (int)y + ")");
        return element;
    }

    public void addFlow(String sourceId, String targetId,
                        double srcAx, double srcAy, double tgtAx, double tgtAy) {
        boolean exists = diagram.getElements().stream()
            .filter(e -> e.getType() == app.model.ElementType.SEQUENCE_FLOW)
            .map(e -> (app.model.elements.SequenceFlowElement) e)
            .anyMatch(sf -> sf.getSourceId().equals(sourceId) && sf.getTargetId().equals(targetId));
        if (exists) return;

        app.model.elements.SequenceFlowElement flow =
            (app.model.elements.SequenceFlowElement) factory.createFlow(sourceId, targetId);
        flow.setSourceAnchor(srcAx, srcAy);
        flow.setTargetAnchor(tgtAx, tgtAy);
        commandManager.executeCommand(new AddElementCommand(diagram, flow));
        loggingService.log("Added SEQUENCE_FLOW from " + sourceId + " to " + targetId);
    }

    public void removeElement(String id) {
        diagram.getElementById(id).ifPresent(el -> {
            List<DiagramElement> elementsToRemove = new java.util.ArrayList<>();
            elementsToRemove.add(el);
            if (el.getType() != ElementType.SEQUENCE_FLOW) {
                diagram.getElements().stream()
                    .filter(candidate -> candidate.getType() == ElementType.SEQUENCE_FLOW)
                    .map(candidate -> (SequenceFlowElement) candidate)
                    .filter(flow -> flow.getSourceId().equals(id) || flow.getTargetId().equals(id))
                    .forEach(elementsToRemove::add);
            }
            commandManager.executeCommand(new RemoveElementCommand(diagram, elementsToRemove));
            loggingService.log("Removed " + el.getType() + " id=" + id);
        });
    }

    public void moveElement(String id, double x, double y) {
        diagram.getElementById(id).ifPresent(el -> {
            double dx = x - el.getX();
            double dy = y - el.getY();
            el.setX(x);
            el.setY(y);
            moveConnectedFlowAnchors(id, dx, dy);
        });
    }

    private void moveConnectedFlowAnchors(String elementId, double dx, double dy) {
        if (dx == 0 && dy == 0) return;

        diagram.getElements().stream()
            .filter(e -> e.getType() == ElementType.SEQUENCE_FLOW)
            .map(e -> (SequenceFlowElement) e)
            .forEach(flow -> {
                if (flow.getSourceId().equals(elementId)) {
                    flow.setSourceAnchor(
                        flow.getSourceAnchorX() + dx,
                        flow.getSourceAnchorY() + dy
                    );
                }
                if (flow.getTargetId().equals(elementId)) {
                    flow.setTargetAnchor(
                        flow.getTargetAnchorX() + dx,
                        flow.getTargetAnchorY() + dy
                    );
                }
            });
    }

    public void updateElementLabel(String id, String label) {
        diagram.getElementById(id).ifPresent(el -> {
            el.setLabel(label);
            loggingService.log("Updated " + el.getType() + " label id=" + id);
        });
    }

    public void undo() {
        commandManager.undo();
        loggingService.log("Undo");
    }

    public void redo() {
        commandManager.redo();
        loggingService.log("Redo");
    }

    public void saveDiagram() {
        createDiagramDAO().save(diagram);
        loggingService.log("Saved diagram: " + diagram.getName());
    }

    public void loadDiagram(int diagramId) {
        Diagram loaded = createDiagramDAO().load(diagramId);
        diagram.getElements().setAll(loaded.getElements());
        diagram.setName(loaded.getName());
        diagram.setDbId(loaded.getDbId());
        loggingService.log("Loaded diagram id=" + diagramId);
    }

    public List<String[]> listSavedDiagrams() {
        return createDiagramDAO().listAll();
    }

    public Diagram getDiagram() { return diagram; }
    public boolean canUndo() { return commandManager.canUndo(); }
    public boolean canRedo() { return commandManager.canRedo(); }

    private DiagramDAO createDiagramDAO() {
        return new DiagramDAOImpl();
    }
}
