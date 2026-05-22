package app.view;

import app.model.Diagram;
import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.SequenceFlowElement;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class CanvasView extends Pane {
    private static final double BASE_DOT_SPACING = 20;
    private static final double MIN_ZOOM = 0.6;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;

    @FunctionalInterface
    public interface ConnectorClickHandler {
        void accept(DiagramElement element, double anchorX, double anchorY);
    }

    @FunctionalInterface
    public interface NodeDragHandler {
        boolean accept(DiagramElement element, double x, double y);
    }

    @FunctionalInterface
    public interface NodeClickHandler {
        void accept(DiagramElement element, int clickCount);
    }

    private final Diagram diagram;
    private NodeClickHandler onNodeClicked;
    private NodeDragHandler onNodeDragged;
    private ConnectorClickHandler onConnectorClicked;
    private String highlightedElementId = null;
    private final Canvas dotCanvas;
    private final Pane contentLayer;

    // Pan state
    private double panOffsetX = 0, panOffsetY = 0;
    private double dragStartX, dragStartY;
    private double nodeDragStartSceneX, nodeDragStartSceneY;
    private boolean nodeWasDragged = false;
    private boolean panEnabled = false;
    private double zoomFactor = 1.0;

    public CanvasView(Diagram diagram) {
        this.diagram = diagram;
        setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
            Color.web("#f8f8f8"), null, null)));
        setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        dotCanvas = new Canvas();
        dotCanvas.setMouseTransparent(true);
        dotCanvas.widthProperty().bind(widthProperty());
        dotCanvas.heightProperty().bind(heightProperty());
        dotCanvas.widthProperty().addListener(o -> drawDots());
        dotCanvas.heightProperty().addListener(o -> drawDots());

        contentLayer = new Pane();
        contentLayer.setPickOnBounds(false);
        getChildren().addAll(dotCanvas, contentLayer);

        // Observer: react to model changes
        diagram.getElements().addListener((ListChangeListener<DiagramElement>) change -> {
            refresh();
        });
    }

    private void drawDots() {
        GraphicsContext gc = dotCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, dotCanvas.getWidth(), dotCanvas.getHeight());
        gc.setFill(Color.web("#cccccc"));
        double spacing = BASE_DOT_SPACING * zoomFactor;
        double startX = firstDotOffset(panOffsetX, spacing);
        double startY = firstDotOffset(panOffsetY, spacing);
        for (double x = startX; x < dotCanvas.getWidth(); x += spacing) {
            for (double y = startY; y < dotCanvas.getHeight(); y += spacing) {
                gc.fillOval(x - 1, y - 1, 2, 2);
            }
        }
    }

    public void refresh() {
        drawDots();
        contentLayer.getChildren().clear();
        applyViewportTransform();

        for (DiagramElement el : diagram.getElements()) {
            if (el.getType() == ElementType.SEQUENCE_FLOW) {
                SequenceFlowElement sf = (SequenceFlowElement) el;
                diagram.getElementById(sf.getSourceId()).ifPresent(src ->
                    diagram.getElementById(sf.getTargetId()).ifPresent(tgt -> {
                        Node arrow = ElementRenderer.renderFlow(src, tgt, sf);
                        contentLayer.getChildren().add(arrow);
                    })
                );
            }
        }

        for (DiagramElement el : diagram.getElements()) {
            if (el.getType() != ElementType.SEQUENCE_FLOW) {
                boolean highlighted = el.getId().equals(highlightedElementId);
                Node node = ElementRenderer.renderNode(el, onConnectorClicked, highlighted);
                node.setOnMousePressed(e -> {
                    nodeDragStartSceneX = e.getSceneX();
                    nodeDragStartSceneY = e.getSceneY();
                    nodeWasDragged = false;
                    e.consume();
                });
                node.setOnMouseDragged(e -> {
                    if (onNodeDragged != null) {
                        double dx = (e.getSceneX() - nodeDragStartSceneX) / zoomFactor;
                        double dy = (e.getSceneY() - nodeDragStartSceneY) / zoomFactor;
                        double nextX = el.getX() + dx;
                        double nextY = el.getY() + dy;
                        if (onNodeDragged.accept(el, nextX, nextY)) {
                            node.setLayoutX(nextX);
                            node.setLayoutY(nextY);
                            nodeWasDragged = true;
                        }
                    }
                    nodeDragStartSceneX = e.getSceneX();
                    nodeDragStartSceneY = e.getSceneY();
                    e.consume();
                });
                node.setOnMouseReleased(e -> {
                    if (nodeWasDragged) {
                        refresh();
                    } else if (onNodeClicked != null) {
                        onNodeClicked.accept(el, e.getClickCount());
                    }
                    e.consume();
                });
                contentLayer.getChildren().add(node);
            }
        }
    }

    public void setOnNodeClicked(NodeClickHandler handler) {
        this.onNodeClicked = handler;
    }

    public void setOnNodeDragged(NodeDragHandler handler) {
        this.onNodeDragged = handler;
    }

    public void setOnConnectorClicked(ConnectorClickHandler handler) {
        this.onConnectorClicked = handler;
    }

    public void highlightSourceElement(String elementId) {
        highlightedElementId = elementId;
        refresh();
    }

    public void clearHighlight() {
        highlightedElementId = null;
        refresh();
    }

    public void setPanEnabled(boolean enabled) {
        panEnabled = enabled;
        setCursor(enabled ? Cursor.OPEN_HAND : Cursor.DEFAULT);
        if (enabled) {
            setOnMousePressed(e -> {
                dragStartX = e.getSceneX() - panOffsetX;
                dragStartY = e.getSceneY() - panOffsetY;
                setCursor(Cursor.CLOSED_HAND);
            });
            setOnMouseDragged(e -> {
                panOffsetX = e.getSceneX() - dragStartX;
                panOffsetY = e.getSceneY() - dragStartY;
                applyViewportTransform();
                drawDots();
            });
            setOnMouseReleased(e -> setCursor(Cursor.OPEN_HAND));
        } else {
            setOnMousePressed(null);
            setOnMouseDragged(null);
            setOnMouseReleased(null);
            setCursor(Cursor.DEFAULT);
        }
    }

    public void setSelectMode(boolean enabled) {
        setCursor(enabled ? Cursor.DEFAULT : Cursor.DEFAULT);
    }

    public double toDiagramX(double viewX) {
        return (viewX - panOffsetX) / zoomFactor;
    }

    public double toDiagramY(double viewY) {
        return (viewY - panOffsetY) / zoomFactor;
    }

    public void zoomIn() {
        setZoom(zoomFactor + ZOOM_STEP);
    }

    public void zoomOut() {
        setZoom(zoomFactor - ZOOM_STEP);
    }

    private double firstDotOffset(double panOffset, double spacing) {
        double offset = panOffset % spacing;
        if (offset < 0) {
            offset += spacing;
        }
        return offset;
    }

    private void setZoom(double requestedZoom) {
        double clampedZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requestedZoom));
        if (Math.abs(clampedZoom - zoomFactor) < 0.0001) {
            return;
        }
        zoomFactor = clampedZoom;
        applyViewportTransform();
        drawDots();
    }

    private void applyViewportTransform() {
        contentLayer.getTransforms().setAll(
            new Scale(zoomFactor, zoomFactor, 0, 0),
            new Translate(panOffsetX, panOffsetY)
        );
    }

}
