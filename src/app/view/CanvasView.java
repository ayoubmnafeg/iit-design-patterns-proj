package app.view;

import app.model.Diagram;
import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.SequenceFlowElement;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class CanvasView extends Pane {
    private static final double BASE_DOT_SPACING = 20;
    private static final double MIN_ZOOM = 0.6;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;
    private static final double DEFAULT_ZOOM = 1.3;

    public interface ConnectorDragHandler {
        void start(DiagramElement element, double anchorX, double anchorY);
        void drag(double sceneX, double sceneY);
        void finish(double sceneX, double sceneY);
        void cancel();
    }

    @FunctionalInterface
    public interface NodeDragHandler {
        boolean accept(DiagramElement element, double x, double y);
    }

    @FunctionalInterface
    public interface NodeDragFinishedHandler {
        void accept(DiagramElement element);
    }

    @FunctionalInterface
    public interface NodeClickHandler {
        void accept(DiagramElement element, int clickCount);
    }

    @FunctionalInterface
    public interface FlowClickHandler {
        void accept(SequenceFlowElement flow, int clickCount);
    }

    private final Diagram diagram;
    private NodeClickHandler onNodeClicked;
    private FlowClickHandler onFlowClicked;
    private NodeDragHandler onNodeDragged;
    private NodeDragFinishedHandler onNodeDragFinished;
    private ConnectorDragHandler onConnectorDragged;
    private String highlightedElementId = null;
    private final Canvas dotCanvas;
    private final Pane contentLayer;
    private final Pane flowLayer;
    private final Pane nodeLayer;
    private Node connectorPreview;

    // Pan state
    private double panOffsetX = 0, panOffsetY = 0;
    private double dragStartX, dragStartY;
    private double nodeDragStartSceneX, nodeDragStartSceneY;
    private boolean nodeWasDragged = false;
    private boolean panEnabled = false;
    private boolean eraseMode = false;
    private double zoomFactor = DEFAULT_ZOOM;
    private final Cursor eraserCursor = createEraserCursor();

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
        flowLayer = new Pane();
        nodeLayer = new Pane();
        contentLayer.setPickOnBounds(false);
        flowLayer.setPickOnBounds(false);
        nodeLayer.setPickOnBounds(false);
        contentLayer.getChildren().addAll(flowLayer, nodeLayer);
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
        flowLayer.getChildren().clear();
        nodeLayer.getChildren().clear();
        connectorPreview = null;
        applyViewportTransform();

        refreshFlows();

        for (DiagramElement el : diagram.getElements()) {
            if (el.getType() != ElementType.SEQUENCE_FLOW) {
                boolean highlighted = el.getId().equals(highlightedElementId);
                Node node = ElementRenderer.renderNode(el, onConnectorDragged, highlighted);
                node.setOnMousePressed(e -> {
                    if (isConnectorDotEvent(e) || startConnectorDragIfNearAnchor(el, e.getSceneX(), e.getSceneY())) {
                        return;
                    }
                    nodeDragStartSceneX = e.getSceneX();
                    nodeDragStartSceneY = e.getSceneY();
                    nodeWasDragged = false;
                    e.consume();
                });
                node.setOnMouseDragged(e -> {
                    if (isConnectorDotEvent(e) || connectorPreview != null) {
                        if (onConnectorDragged != null && connectorPreview != null) {
                            onConnectorDragged.drag(e.getSceneX(), e.getSceneY());
                        }
                        return;
                    }
                    if (onNodeDragged != null) {
                        double dx = (e.getSceneX() - nodeDragStartSceneX) / zoomFactor;
                        double dy = (e.getSceneY() - nodeDragStartSceneY) / zoomFactor;
                        double nextX = el.getX() + dx;
                        double nextY = el.getY() + dy;
                        if (onNodeDragged.accept(el, nextX, nextY)) {
                            node.setLayoutX(nextX);
                            node.setLayoutY(nextY);
                            nodeWasDragged = true;
                            snapConnectedFlowAnchors(el);
                            refreshFlows();
                        }
                    }
                    nodeDragStartSceneX = e.getSceneX();
                    nodeDragStartSceneY = e.getSceneY();
                    e.consume();
                });
                node.setOnMouseReleased(e -> {
                    if (isConnectorDotEvent(e) || connectorPreview != null) {
                        if (onConnectorDragged != null && connectorPreview != null) {
                            onConnectorDragged.finish(e.getSceneX(), e.getSceneY());
                        }
                        return;
                    }
                    if (nodeWasDragged) {
                        refresh();
                        if (onNodeDragFinished != null) {
                            onNodeDragFinished.accept(el);
                        }
                    } else if (onNodeClicked != null) {
                        onNodeClicked.accept(el, e.getClickCount());
                    }
                    e.consume();
                });
                nodeLayer.getChildren().add(node);
            }
        }
    }

    public void refreshFlows() {
        flowLayer.getChildren().clear();
        for (DiagramElement el : diagram.getElements()) {
            if (el.getType() == ElementType.SEQUENCE_FLOW) {
                SequenceFlowElement sf = (SequenceFlowElement) el;
                diagram.getElementById(sf.getSourceId()).ifPresent(src ->
                    diagram.getElementById(sf.getTargetId()).ifPresent(tgt -> {
                        Node arrow = ElementRenderer.renderFlow(src, tgt, sf);
                        arrow.setOnMouseClicked(e -> {
                            if (onFlowClicked != null) {
                                onFlowClicked.accept(sf, e.getClickCount());
                                e.consume();
                            }
                        });
                        flowLayer.getChildren().add(arrow);
                    })
                );
            }
        }
    }

    private void snapConnectedFlowAnchors(DiagramElement movedElement) {
        for (DiagramElement el : diagram.getElements()) {
            if (el.getType() != ElementType.SEQUENCE_FLOW) continue;

            SequenceFlowElement flow = (SequenceFlowElement) el;
            if (!flow.getSourceId().equals(movedElement.getId())
                    && !flow.getTargetId().equals(movedElement.getId())) {
                continue;
            }

            diagram.getElementById(flow.getSourceId()).ifPresent(source ->
                diagram.getElementById(flow.getTargetId()).ifPresent(target -> {
                    double[][] pair = nearestConnectorPair(source, target);
                    flow.setSourceAnchor(pair[0][0], pair[0][1]);
                    flow.setTargetAnchor(pair[1][0], pair[1][1]);
                })
            );
        }
    }

    private double[][] nearestConnectorPair(DiagramElement source, DiagramElement target) {
        double[][] sourceOffsets = ElementRenderer.connectorOffsets(source);
        double[][] targetOffsets = ElementRenderer.connectorOffsets(target);
        double[] bestSource = null;
        double[] bestTarget = null;
        double bestDistance = Double.MAX_VALUE;

        for (double[] sourceOffset : sourceOffsets) {
            double sourceX = source.getX() + sourceOffset[0];
            double sourceY = source.getY() + sourceOffset[1];
            for (double[] targetOffset : targetOffsets) {
                double targetX = target.getX() + targetOffset[0];
                double targetY = target.getY() + targetOffset[1];
                double dx = targetX - sourceX;
                double dy = targetY - sourceY;
                double distance = dx * dx + dy * dy;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestSource = new double[] {sourceX, sourceY};
                    bestTarget = new double[] {targetX, targetY};
                }
            }
        }

        return new double[][] {bestSource, bestTarget};
    }

    public void setOnNodeClicked(NodeClickHandler handler) {
        this.onNodeClicked = handler;
    }

    public void setOnFlowClicked(FlowClickHandler handler) {
        this.onFlowClicked = handler;
    }

    public void setOnNodeDragged(NodeDragHandler handler) {
        this.onNodeDragged = handler;
    }

    public void setOnNodeDragFinished(NodeDragFinishedHandler handler) {
        this.onNodeDragFinished = handler;
    }

    private boolean isConnectorDotEvent(javafx.event.Event event) {
        Object target = event.getTarget();
        return target instanceof Node
            && Boolean.TRUE.equals(((Node) target).getProperties().get("connectorDot"));
    }

    private boolean startConnectorDragIfNearAnchor(DiagramElement element, double sceneX, double sceneY) {
        if (onConnectorDragged == null) return false;

        double diagramX = sceneToDiagramX(sceneX);
        double diagramY = sceneToDiagramY(sceneY);
        double hitRadius = 14 / zoomFactor;
        for (double[] offset : ElementRenderer.connectorOffsets(element)) {
            double anchorX = element.getX() + offset[0];
            double anchorY = element.getY() + offset[1];
            double dx = anchorX - diagramX;
            double dy = anchorY - diagramY;
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                onConnectorDragged.start(element, anchorX, anchorY);
                return true;
            }
        }
        return false;
    }

    public void setOnConnectorDragged(ConnectorDragHandler handler) {
        this.onConnectorDragged = handler;
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
        eraseMode = false;
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
        eraseMode = false;
        setCursor(Cursor.DEFAULT);
    }

    public void setEraseMode(boolean enabled) {
        eraseMode = enabled;
        if (enabled) {
            setOnMousePressed(null);
            setOnMouseDragged(null);
            setOnMouseReleased(null);
            setCursor(eraserCursor);
        } else if (!panEnabled) {
            setCursor(Cursor.DEFAULT);
        }
    }

    public double toDiagramX(double viewX) {
        return (viewX - panOffsetX) / zoomFactor;
    }

    public double toDiagramY(double viewY) {
        return (viewY - panOffsetY) / zoomFactor;
    }

    public double sceneToDiagramX(double sceneX) {
        return toDiagramX(sceneToLocal(sceneX, 0).getX());
    }

    public double sceneToDiagramY(double sceneY) {
        return toDiagramY(sceneToLocal(0, sceneY).getY());
    }

    public void showConnectorPreview(double startX, double startY, double endX, double endY) {
        showConnectorPreview(startX, startY, endX, endY, Math.abs(endX - startX) >= Math.abs(endY - startY));
    }

    public void showConnectorPreview(double startX, double startY, double endX, double endY,
                                     boolean sourceHorizontal) {
        clearConnectorPreview();
        javafx.scene.shape.Path preview = ElementRenderer.buildOrthogonalFlowPath(
            startX, startY, endX, endY, sourceHorizontal, sourceHorizontal
        );
        preview.setFill(Color.TRANSPARENT);
        preview.setStroke(Color.web("#4a90d9"));
        preview.setStrokeWidth(1.8);
        preview.getStrokeDashArray().addAll(6.0, 5.0);
        preview.setMouseTransparent(true);
        connectorPreview = preview;
        flowLayer.getChildren().add(connectorPreview);
    }

    public void clearConnectorPreview() {
        if (connectorPreview != null) {
            flowLayer.getChildren().remove(connectorPreview);
            connectorPreview = null;
        }
    }

    public ConnectorTarget findConnectorTarget(double sceneX, double sceneY, String excludedElementId) {
        double diagramX = sceneToDiagramX(sceneX);
        double diagramY = sceneToDiagramY(sceneY);
        double hitRadius = 80 / zoomFactor;
        ConnectorTarget bestTarget = null;
        double bestDistance = hitRadius * hitRadius;

        for (DiagramElement element : diagram.getElements()) {
            if (element.getType() == ElementType.SEQUENCE_FLOW) continue;
            if (element.getId().equals(excludedElementId)) continue;

            for (double[] offset : ElementRenderer.connectorOffsets(element)) {
                double anchorX = element.getX() + offset[0];
                double anchorY = element.getY() + offset[1];
                double dx = anchorX - diagramX;
                double dy = anchorY - diagramY;
                double distance = dx * dx + dy * dy;
                if (distance <= bestDistance) {
                    bestDistance = distance;
                    bestTarget = new ConnectorTarget(element, anchorX, anchorY);
                }
            }
        }

        return bestTarget;
    }

    public static class ConnectorTarget {
        private final DiagramElement element;
        private final double anchorX;
        private final double anchorY;

        public ConnectorTarget(DiagramElement element, double anchorX, double anchorY) {
            this.element = element;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
        }

        public DiagramElement getElement() { return element; }
        public double getAnchorX() { return anchorX; }
        public double getAnchorY() { return anchorY; }
    }

    public void zoomIn() {
        setZoom(zoomFactor + ZOOM_STEP);
    }

    public void zoomOut() {
        setZoom(zoomFactor - ZOOM_STEP);
    }

    public int getZoomPercent() {
        return (int) Math.round((zoomFactor / DEFAULT_ZOOM) * 100);
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

    private Cursor createEraserCursor() {
        int size = 24;
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();
        Color outline = Color.web("#374151");
        Color fill = Color.web("#f8fafc");
        Color band = Color.web("#cbd5e1");

        fillRotatedRect(writer, 5, 9, 14, 8, -0.75, fill);
        drawRotatedRect(writer, 5, 9, 14, 8, -0.75, outline);
        fillRotatedRect(writer, 5, 9, 5, 8, -0.75, band);
        drawLine(writer, 9, 7, 15, 13, outline);
        drawLine(writer, 8, 18, 19, 18, outline);

        return new ImageCursor(image, 7, 15);
    }

    private void fillRotatedRect(PixelWriter writer, double x, double y, double w, double h,
                                 double angle, Color color) {
        double cx = x + w / 2.0;
        double cy = y + h / 2.0;
        double cos = Math.cos(-angle);
        double sin = Math.sin(-angle);
        for (int px = 0; px < 24; px++) {
            for (int py = 0; py < 24; py++) {
                double dx = px - cx;
                double dy = py - cy;
                double rx = dx * cos - dy * sin;
                double ry = dx * sin + dy * cos;
                if (Math.abs(rx) <= w / 2.0 && Math.abs(ry) <= h / 2.0) {
                    writer.setColor(px, py, color);
                }
            }
        }
    }

    private void drawRotatedRect(PixelWriter writer, double x, double y, double w, double h,
                                 double angle, Color color) {
        double[][] points = {
            rotatePoint(x, y, x + w / 2.0, y + h / 2.0, angle),
            rotatePoint(x + w, y, x + w / 2.0, y + h / 2.0, angle),
            rotatePoint(x + w, y + h, x + w / 2.0, y + h / 2.0, angle),
            rotatePoint(x, y + h, x + w / 2.0, y + h / 2.0, angle)
        };
        for (int i = 0; i < points.length; i++) {
            double[] start = points[i];
            double[] end = points[(i + 1) % points.length];
            drawLine(writer, (int) Math.round(start[0]), (int) Math.round(start[1]),
                (int) Math.round(end[0]), (int) Math.round(end[1]), color);
        }
    }

    private double[] rotatePoint(double x, double y, double cx, double cy, double angle) {
        double dx = x - cx;
        double dy = y - cy;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new double[] {cx + dx * cos - dy * sin, cy + dx * sin + dy * cos};
    }

    private void drawLine(PixelWriter writer, int x1, int y1, int x2, int y2, Color color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x1 >= 0 && x1 < 24 && y1 >= 0 && y1 < 24) {
                writer.setColor(x1, y1, color);
            }
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

}
