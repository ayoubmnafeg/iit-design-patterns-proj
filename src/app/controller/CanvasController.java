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
    private final Runnable onDiagramChanged;

    private DiagramElement pendingFlowSource = null;
    private double pendingSourceAnchorX, pendingSourceAnchorY;
    private boolean pendingSourceHorizontal = true;
    private String selectedNodeId = null;

    public CanvasController(DiagramService diagramService,
                            PaletteController paletteController,
                            CanvasView canvasView,
                            PaletteView paletteView,
                            Runnable onDiagramChanged) {
        this.diagramService    = diagramService;
        this.paletteController = paletteController;
        this.canvasView        = canvasView;
        this.paletteView       = paletteView;
        this.onDiagramChanged  = onDiagramChanged;

        canvasView.setOnMouseClicked(e -> onCanvasClick(e.getX(), e.getY()));
        canvasView.setOnNodeClicked(this::onNodeClick);
        canvasView.setOnFlowClicked(this::onFlowClick);
        canvasView.setOnNodeDragged(this::onNodeDragged);
        canvasView.setOnNodeDragFinished(this::onNodeDragFinished);
        canvasView.setOnConnectorDragged(new CanvasView.ConnectorDragHandler() {
            @Override
            public void start(DiagramElement element, double anchorX, double anchorY) {
                onConnectorDragStart(element, anchorX, anchorY);
            }

            @Override
            public void drag(double sceneX, double sceneY) {
                onConnectorDrag(sceneX, sceneY);
            }

            @Override
            public void finish(double sceneX, double sceneY) {
                onConnectorDragFinish(sceneX, sceneY);
            }

            @Override
            public void cancel() {
                onConnectorDragCancel();
            }
        });
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
        notifyDiagramChanged();
        paletteController.clearAll();
        paletteView.clearActive();
    }

    private void onNodeClick(DiagramElement element, int clickCount) {
        if (paletteController.isDeleteMode()) {
            diagramService.removeElement(element.getId());
            notifyDiagramChanged();
            selectedNodeId = null;
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

    private void onFlowClick(app.model.elements.SequenceFlowElement flow, int clickCount) {
        if (paletteController.isDeleteMode()) {
            diagramService.removeElement(flow.getId());
            notifyDiagramChanged();
            return;
        }

        if (clickCount > 1) {
            editFlowLabel(flow);
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
                  notifyDiagramChanged();
                  canvasView.highlightSourceElement(element.getId());
              });
    }

    private void editFlowLabel(DiagramElement element) {
        TextInputDialog dialog = new TextInputDialog(element.getLabel());
        dialog.setTitle("Edit Link");
        dialog.setHeaderText("Link text");
        dialog.setContentText("Label:");

        Optional<String> result = dialog.showAndWait();
        result.map(String::trim)
              .ifPresent(text -> {
                  diagramService.updateElementLabel(element.getId(), text);
                  notifyDiagramChanged();
                  canvasView.refresh();
              });
    }

    private boolean onNodeDragged(DiagramElement element, double x, double y) {
        if (!paletteController.isSelectMode()) return false;

        diagramService.moveElement(element.getId(), x, y);
        notifyDiagramChanged();
        return true;
    }

    private void onNodeDragFinished(DiagramElement element) {
        diagramService.autoSaveIfSaved();
        notifyDiagramChanged();
    }

    private void onConnectorDragStart(DiagramElement element, double anchorX, double anchorY) {
        if (paletteController.isDeleteMode()) return;

        pendingFlowSource = element;
        selectedNodeId = element.getId();
        pendingSourceAnchorX = anchorX;
        pendingSourceAnchorY = anchorY;
        pendingSourceHorizontal =
            Math.abs(anchorX - element.getX()) >= Math.abs(anchorY - element.getY());
        canvasView.showConnectorPreview(anchorX, anchorY, anchorX, anchorY, pendingSourceHorizontal);
    }

    private void onConnectorDrag(double sceneX, double sceneY) {
        if (pendingFlowSource == null) return;

        canvasView.showConnectorPreview(
            pendingSourceAnchorX,
            pendingSourceAnchorY,
            canvasView.sceneToDiagramX(sceneX),
            canvasView.sceneToDiagramY(sceneY),
            pendingSourceHorizontal
        );
    }

    private void onConnectorDragFinish(double sceneX, double sceneY) {
        if (pendingFlowSource == null) return;

        CanvasView.ConnectorTarget target =
            canvasView.findConnectorTarget(sceneX, sceneY, pendingFlowSource.getId());
        if (target != null) {
            diagramService.addFlow(
                pendingFlowSource.getId(), target.getElement().getId(),
                pendingSourceAnchorX, pendingSourceAnchorY,
                target.getAnchorX(), target.getAnchorY()
            );
            notifyDiagramChanged();
        }
        clearPendingConnector();
    }

    private void onConnectorDragCancel() {
        if (pendingFlowSource != null) {
            clearPendingConnector();
        }
    }

    private void clearPendingConnector() {
        pendingFlowSource = null;
        selectedNodeId = null;
        canvasView.clearConnectorPreview();
        canvasView.clearHighlight();
        paletteController.clearAll();
        paletteView.clearActive();
    }

    private void notifyDiagramChanged() {
        if (onDiagramChanged != null) {
            onDiagramChanged.run();
        }
    }
}
