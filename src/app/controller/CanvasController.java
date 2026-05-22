package app.controller;

import app.model.DiagramElement;
import app.model.ElementType;
import app.service.DiagramService;
import app.view.CanvasView;
import app.view.PaletteView;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class CanvasController {
    private final DiagramService diagramService;
    private final PaletteController paletteController;
    private final CanvasView canvasView;
    private final PaletteView paletteView;

    private DiagramElement pendingFlowSource = null;
    private double pendingSourceAnchorX, pendingSourceAnchorY;
    private String selectedNodeId = null;

    public CanvasController(DiagramService diagramService,
                            PaletteController paletteController,
                            CanvasView canvasView,
                            PaletteView paletteView) {
        this.diagramService    = diagramService;
        this.paletteController = paletteController;
        this.canvasView        = canvasView;
        this.paletteView       = paletteView;

        canvasView.setOnMouseClicked(e -> onCanvasClick(e.getX(), e.getY()));
        canvasView.setOnNodeClicked(this::onNodeClick);
        canvasView.setOnNodeDragged(this::onNodeDragged);
        canvasView.setOnConnectorClicked(this::onConnectorClick);
    }

    private void onCanvasClick(double x, double y) {
        if (pendingFlowSource != null) {
            pendingFlowSource = null;
            selectedNodeId = null;
            canvasView.clearHighlight();
            return;
        }
        if (paletteController.isFlowMode() || paletteController.isDeleteMode()) return;
        if (paletteController.getSelectedType() == null) {
            if (paletteController.isSelectMode()) {
                selectedNodeId = null;
                canvasView.clearHighlight();
            }
            return;
        }

        diagramService.addElement(
            paletteController.getSelectedType(),
            canvasView.toDiagramX(x),
            canvasView.toDiagramY(y)
        );
        paletteController.clearAll();
        paletteView.clearActive();
    }

    private void onNodeClick(DiagramElement element, int clickCount) {
        if (paletteController.isDeleteMode()) {
            diagramService.removeElement(element.getId());
            selectedNodeId = null;
            paletteController.clearAll();
            paletteView.clearActive();
            return;
        }

        if (paletteController.isSelectMode()) {
            boolean alreadySelected = element.getId().equals(selectedNodeId);
            selectedNodeId = element.getId();
            canvasView.highlightSourceElement(element.getId());
            if ((clickCount > 1 || alreadySelected) && element.getType() == ElementType.TASK) {
                editTaskLabel(element);
            }
        }
    }

    private void editTaskLabel(DiagramElement element) {
        TextInputDialog dialog = new TextInputDialog(element.getLabel());
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Task text");
        dialog.setContentText("Label:");

        Optional<String> result = dialog.showAndWait();
        result.map(String::trim)
              .filter(text -> !text.isEmpty())
              .ifPresent(text -> {
                  diagramService.updateElementLabel(element.getId(), text);
                  canvasView.highlightSourceElement(element.getId());
              });
    }

    private boolean onNodeDragged(DiagramElement element, double x, double y) {
        if (!paletteController.isSelectMode()) return false;

        diagramService.moveElement(element.getId(), x, y);
        return true;
    }

    private void onConnectorClick(DiagramElement element, double anchorX, double anchorY) {
        if (paletteController.isDeleteMode()) return;

        if (pendingFlowSource == null) {
            pendingFlowSource = element;
            selectedNodeId = element.getId();
            pendingSourceAnchorX = anchorX;
            pendingSourceAnchorY = anchorY;
            canvasView.highlightSourceElement(element.getId());
        } else {
            if (!pendingFlowSource.getId().equals(element.getId())) {
                diagramService.addFlow(
                    pendingFlowSource.getId(), element.getId(),
                    pendingSourceAnchorX, pendingSourceAnchorY,
                    anchorX, anchorY
                );
            }
            pendingFlowSource = null;
            selectedNodeId = null;
            canvasView.clearHighlight();
            paletteController.clearAll();
            paletteView.clearActive();
        }
    }
}
