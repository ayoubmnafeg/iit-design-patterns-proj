package app.view;

import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.SequenceFlowElement;
import app.view.CanvasView.ConnectorDragHandler;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ElementRenderer {

    private static final double EVENT_R  = 25;
    private static final double TASK_W   = 100;
    private static final double TASK_H   = 50;
    private static final double TASK_PAD_X = 28;
    private static final double TASK_MAX_W = 280;
    private static final double TASK_FONT_SIZE = 11;
    private static final double GW_HALF  = 40;
    private static final int    EVENT_MARKER_SIZE = 24;
    private static final double LABEL_GAP = 10;

    private static final double DOT_R       = 5;
    private static final Color  DOT_FILL    = Color.web("#4a90d9");
    private static final Color  DOT_STROKE  = Color.WHITE;
    private static final double DOT_STROKE_W = 1.2;

    public static Group renderNode(DiagramElement element, ConnectorDragHandler onConnector,
                                   boolean highlighted) {
        Group container = new Group();
        ElementType type = element.getType();
        container.setLayoutX(element.getX());
        container.setLayoutY(element.getY());
        container.setUserData(element.getId());

        if (highlighted) {
            double hw = halfWidth(element) + 6;
            double hh = halfHeight(element) + 6;
            javafx.scene.shape.Rectangle ring = new javafx.scene.shape.Rectangle(
                -hw, -hh, hw * 2, hh * 2);
            ring.setArcWidth(14);
            ring.setArcHeight(14);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.web("#4a90d9"));
            ring.setStrokeWidth(2);
            ring.getStrokeDashArray().addAll(5.0, 4.0);
            ring.setMouseTransparent(true);
            container.getChildren().add(ring);
        }

        Group symbol = buildShape(element);
        container.getChildren().add(symbol);

        ImageView marker = buildMarker(type);
        if (marker != null) container.getChildren().add(marker);

        Tooltip tooltip = createNodeTooltip(element);
        if (tooltip != null) {
            Tooltip.install(container, tooltip);
            Tooltip.install(symbol, tooltip);
            if (marker != null) {
                Tooltip.install(marker, tooltip);
            }
        }

        // Connector dots at N/E/S/W — hidden until hover
        double hw = halfWidth(element);
        double hh = halfHeight(element);
        Circle[] dots = {
            makeDot(0,   -hh, element, 0,   -hh, onConnector),  // N
            makeDot(hw,  0,   element, hw,  0,   onConnector),  // E
            makeDot(0,   hh,  element, 0,   hh,  onConnector),  // S
            makeDot(-hw, 0,   element, -hw, 0,   onConnector),  // W
        };
        for (Circle d : dots) {
            d.setVisible(false);
            container.getChildren().add(d);
        }

        container.setOnMouseEntered(e -> { for (Circle d : dots) d.setVisible(true); });
        container.setOnMouseExited(e  -> { for (Circle d : dots) d.setVisible(false); });

        return container;
    }

    private static Tooltip createNodeTooltip(DiagramElement element) {
        if (element.getLabel() == null || element.getLabel().isBlank()) {
            return null;
        }

        Tooltip tooltip = new Tooltip(element.getLabel());
        tooltip.setShowDelay(Duration.millis(150));
        tooltip.setShowDuration(Duration.seconds(8));
        tooltip.setHideDelay(Duration.millis(80));
        return tooltip;
    }

    private static Circle makeDot(double cx, double cy, DiagramElement element,
                                   double anchorOffX, double anchorOffY,
                                   ConnectorDragHandler onConnector) {
        Circle dot = new Circle(cx, cy, DOT_R);
        dot.getProperties().put("connectorDot", true);
        dot.setFill(DOT_FILL);
        dot.setStroke(DOT_STROKE);
        dot.setStrokeWidth(DOT_STROKE_W);
        dot.setOnMouseEntered(e -> { dot.setScaleX(1.25); dot.setScaleY(1.25); });
        dot.setOnMouseExited(e  -> { dot.setScaleX(1.0); dot.setScaleY(1.0); });
        dot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (onConnector != null) {
                double worldX = element.getX() + anchorOffX;
                double worldY = element.getY() + anchorOffY;
                onConnector.start(element, worldX, worldY);
                dot.startFullDrag();
            }
            e.consume();
        });
        dot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, e -> {
            if (onConnector != null) {
                onConnector.drag(e.getSceneX(), e.getSceneY());
            }
            e.consume();
        });
        dot.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            if (onConnector != null) {
                onConnector.finish(e.getSceneX(), e.getSceneY());
            }
            e.consume();
        });
        return dot;
    }

    public static double[][] connectorOffsets(DiagramElement element) {
        double hw = halfWidth(element);
        double hh = halfHeight(element);
        return new double[][] {
            {0, -hh},
            {hw, 0},
            {0, hh},
            {-hw, 0}
        };
    }

    public static Group renderFlow(DiagramElement source, DiagramElement target,
                                   SequenceFlowElement sf) {
        double x1, y1, x2, y2;
        if (sf.getSourceAnchorX() != 0 || sf.getSourceAnchorY() != 0
                || sf.getTargetAnchorX() != 0 || sf.getTargetAnchorY() != 0) {
            x1 = sf.getSourceAnchorX();
            y1 = sf.getSourceAnchorY();
            x2 = sf.getTargetAnchorX();
            y2 = sf.getTargetAnchorY();
        } else {
            x1 = source.getX();
            y1 = source.getY();
            x2 = target.getX();
            y2 = target.getY();
        }

        boolean sourceHorizontal = isHorizontalAnchor(source, x1, y1);
        boolean targetHorizontal = isHorizontalAnchor(target, x2, y2);
        double[][] points = orthogonalPoints(x1, y1, x2, y2, sourceHorizontal, targetHorizontal);
        Path path = buildOrthogonalFlowPath(x1, y1, x2, y2, sourceHorizontal, targetHorizontal);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.BLACK);
        path.setStrokeWidth(1.5);

        Path hitPath = buildOrthogonalFlowPath(x1, y1, x2, y2, sourceHorizontal, targetHorizontal);
        hitPath.setFill(Color.TRANSPARENT);
        hitPath.setStroke(Color.TRANSPARENT);
        hitPath.setStrokeWidth(12);

        double angle      = flowArrowAngle(x1, y1, x2, y2, targetHorizontal);
        double arrowLen   = 12;
        double arrowAngle = Math.toRadians(25);
        Polygon arrowHead = new Polygon(
            x2, y2,
            x2 - arrowLen * Math.cos(angle - arrowAngle), y2 - arrowLen * Math.sin(angle - arrowAngle),
            x2 - arrowLen * Math.cos(angle + arrowAngle), y2 - arrowLen * Math.sin(angle + arrowAngle)
        );
        arrowHead.setFill(Color.BLACK);

        Group flowGroup = new Group(hitPath, path, arrowHead);
        if (sf.getLabel() != null && !sf.getLabel().isBlank()) {
            flowGroup.getChildren().add(buildFlowLabel(sf.getLabel(), points));
        }
        return flowGroup;
    }

    private static Label buildFlowLabel(String labelText, double[][] points) {
        double[] position = midpointAlongPath(points);
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 11));
        label.setTextFill(Color.web("#374151"));
        label.setStyle("-fx-background-color: rgba(255,255,255,0.88); " +
                       "-fx-background-radius: 5; -fx-padding: 2 5 2 5;");
        label.setLayoutX(position[0] - 28);
        label.setLayoutY(position[1] - 20);
        return label;
    }

    private static double[] midpointAlongPath(double[][] points) {
        double totalLength = 0;
        for (int i = 1; i < points.length; i++) {
            totalLength += distance(points[i - 1], points[i]);
        }

        double halfway = totalLength / 2.0;
        double traveled = 0;
        for (int i = 1; i < points.length; i++) {
            double segmentLength = distance(points[i - 1], points[i]);
            if (traveled + segmentLength >= halfway) {
                double ratio = segmentLength == 0 ? 0 : (halfway - traveled) / segmentLength;
                return new double[] {
                    points[i - 1][0] + (points[i][0] - points[i - 1][0]) * ratio,
                    points[i - 1][1] + (points[i][1] - points[i - 1][1]) * ratio
                };
            }
            traveled += segmentLength;
        }
        return points[points.length - 1];
    }

    private static double distance(double[] start, double[] end) {
        return Math.hypot(end[0] - start[0], end[1] - start[1]);
    }

    public static Path buildOrthogonalFlowPath(double x1, double y1, double x2, double y2) {
        return buildOrthogonalFlowPath(x1, y1, x2, y2, Math.abs(x2 - x1) >= Math.abs(y2 - y1), true);
    }

    public static Path buildOrthogonalFlowPath(double x1, double y1, double x2, double y2,
                                               boolean sourceHorizontal,
                                               boolean targetHorizontal) {
        double[][] points = orthogonalPoints(x1, y1, x2, y2, sourceHorizontal, targetHorizontal);
        Path path = new Path();
        path.getElements().add(new MoveTo(points[0][0], points[0][1]));

        for (int i = 1; i < points.length - 1; i++) {
            double[] prev = points[i - 1];
            double[] corner = points[i];
            double[] next = points[i + 1];
            double inDx = corner[0] - prev[0];
            double inDy = corner[1] - prev[1];
            double outDx = next[0] - corner[0];
            double outDy = next[1] - corner[1];
            double inLen = Math.hypot(inDx, inDy);
            double outLen = Math.hypot(outDx, outDy);

            if (inLen < 1 || outLen < 1) {
                continue;
            }

            double radius = Math.min(12, Math.min(inLen / 2.0, outLen / 2.0));
            double beforeX = corner[0] - (inDx / inLen) * radius;
            double beforeY = corner[1] - (inDy / inLen) * radius;
            double afterX = corner[0] + (outDx / outLen) * radius;
            double afterY = corner[1] + (outDy / outLen) * radius;

            path.getElements().add(new LineTo(beforeX, beforeY));
            path.getElements().add(new QuadCurveTo(corner[0], corner[1], afterX, afterY));
        }

        path.getElements().add(new LineTo(points[points.length - 1][0], points[points.length - 1][1]));
        return path;
    }

    private static double[][] orthogonalPoints(double x1, double y1, double x2, double y2,
                                               boolean sourceHorizontal,
                                               boolean targetHorizontal) {
        if (Math.abs(x2 - x1) < 1 || Math.abs(y2 - y1) < 1) {
            return new double[][] {{x1, y1}, {x2, y2}};
        }

        if (sourceHorizontal && targetHorizontal) {
            double midX = (x1 + x2) / 2.0;
            return new double[][] {{x1, y1}, {midX, y1}, {midX, y2}, {x2, y2}};
        }
        if (!sourceHorizontal && !targetHorizontal) {
            double midY = (y1 + y2) / 2.0;
            return new double[][] {{x1, y1}, {x1, midY}, {x2, midY}, {x2, y2}};
        }
        if (sourceHorizontal) {
            return new double[][] {{x1, y1}, {x2, y1}, {x2, y2}};
        }
        return new double[][] {{x1, y1}, {x1, y2}, {x2, y2}};
    }

    private static boolean isHorizontalAnchor(DiagramElement element, double anchorX, double anchorY) {
        return Math.abs(anchorX - element.getX()) >= Math.abs(anchorY - element.getY());
    }

    private static double flowArrowAngle(double x1, double y1, double x2, double y2,
                                         boolean targetHorizontal) {
        if (targetHorizontal) {
            return x2 >= x1 ? 0 : Math.PI;
        }
        return y2 >= y1 ? Math.PI / 2 : -Math.PI / 2;
    }

    private static Group buildShape(DiagramElement element) {
        ElementType t = element.getType();
        switch (t) {
            case START_EVENT:
            case MESSAGE_START:
            case TIMER_START:
            case CONDITIONAL_START:
            case SIGNAL_START:
            case ERROR_START:
                return circleGroup(EVENT_R, Color.WHITE, Color.GREEN, 1.5, false, false);

            case INTERMEDIATE_EVENT:
            case MESSAGE_CATCH:
            case TIMER_CATCH:
            case CONDITIONAL_CATCH:
            case LINK_CATCH:
            case SIGNAL_CATCH:
            case ERROR_CATCH:
                return circleGroup(EVENT_R, Color.WHITE, Color.GRAY, 1.5, true, false);

            case MESSAGE_THROW:
            case LINK_THROW:
            case SIGNAL_THROW:
                return circleGroup(EVENT_R, Color.WHITE, Color.GRAY, 1.5, true, true);

            case END_EVENT:
            case MESSAGE_END:
            case SIGNAL_END:
            case ERROR_END:
                return circleGroup(EVENT_R, Color.WHITE, Color.RED, 3.0, false, false);

            case TASK:
                return taskGroup(taskWidth(element.getLabel()));

            case EXCLUSIVE_GATEWAY:
                return gatewayGroup();

            default:
                return new Group();
        }
    }

    private static Group circleGroup(double r, Color fill, Color stroke, double sw,
                                     boolean doubleRing, boolean filledInner) {
        Circle outer = new Circle(0, 0, r);
        outer.setFill(fill);
        outer.setStroke(stroke);
        outer.setStrokeWidth(sw);
        Group g = new Group(outer);
        if (doubleRing) {
            Circle inner = new Circle(0, 0, r - 4);
            inner.setFill(Color.TRANSPARENT);
            inner.setStroke(stroke);
            inner.setStrokeWidth(sw);
            g.getChildren().add(inner);
        }
        if (filledInner) {
            Circle dot = new Circle(0, 0, r - 8);
            g.getChildren().add(dot);
        }
        return g;
    }

    private static Group taskGroup(double width) {
        Rectangle r = new Rectangle(-width / 2, -TASK_H / 2, width, TASK_H);
        r.setArcWidth(12);
        r.setArcHeight(12);
        r.setFill(Color.web("#d0e8ff"));
        r.setStroke(Color.web("#4a90d9"));
        r.setStrokeWidth(1.5);
        return new Group(r);
    }

    private static Group gatewayGroup() {
        Polygon d = new Polygon(
            0, -GW_HALF,
            GW_HALF, 0,
            0, GW_HALF,
            -GW_HALF, 0
        );
        d.setFill(Color.WHITE);
        d.setStroke(Color.BLACK);
        d.setStrokeWidth(1.5);
        return new Group(d);
    }

    private static ImageView buildMarker(ElementType type) {
        String iconPath = markerPath(type);
        if (iconPath == null) return null;
        ImageView marker = SvgIcon.renderRecolored(iconPath, EVENT_MARKER_SIZE, markerColor(type));
        if (marker.getImage() == null) return null;
        marker.setLayoutX(-EVENT_MARKER_SIZE / 2.0);
        marker.setLayoutY(-EVENT_MARKER_SIZE / 2.0);
        return marker;
    }

    private static int markerColor(ElementType type) {
        switch (type) {
            case MESSAGE_START:
            case TIMER_START:
            case CONDITIONAL_START:
            case SIGNAL_START:
            case ERROR_START:
                return 0x008000;

            case MESSAGE_END:
            case SIGNAL_END:
            case ERROR_END:
                return 0xFF0000;

            default:
                return 0x808080;
        }
    }

    private static Label buildLabel(String text, DiagramElement element) {
        Label label = new Label(text);
        if (element.getType() == ElementType.TASK) {
            double taskContentWidth = taskWidth(element.getLabel()) - TASK_PAD_X;
            label.setWrapText(true);
            label.setAlignment(javafx.geometry.Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setMaxWidth(taskContentWidth);
            label.setPrefWidth(taskContentWidth);
            label.setMinHeight(TASK_H);
            label.setStyle("-fx-font-size: 11px; -fx-text-fill: #1f2937; -fx-padding: 1 4 1 4;");
        } else {
            label.setStyle("-fx-font-size: 11px; -fx-background-color: rgba(248,248,248,0.92); -fx-padding: 1 4 1 4;");
        }
        double prefWidth = label.prefWidth(-1);
        double prefHeight = label.prefHeight(-1);
        label.resize(prefWidth, prefHeight);
        if (element.getType() == ElementType.TASK) {
            label.resize(taskWidth(element.getLabel()) - TASK_PAD_X, TASK_H);
            label.relocate(-(taskWidth(element.getLabel()) - TASK_PAD_X) / 2.0, -TASK_H / 2.0);
        } else {
            label.relocate(-prefWidth / 2.0, halfHeight(element) + LABEL_GAP);
        }
        return label;
    }

    private static String markerPath(ElementType type) {
        switch (type) {
            case MESSAGE_START:      return "/app/resources/icons/message-start.svg";
            case TIMER_START:        return "/app/resources/icons/timer-start.svg";
            case CONDITIONAL_START:  return "/app/resources/icons/conditional-start.svg";
            case SIGNAL_START:       return "/app/resources/icons/signal-start.svg";
            case ERROR_START:        return "/app/resources/icons/error-start.svg";
            case MESSAGE_CATCH:      return "/app/resources/icons/message-catch.svg";
            case TIMER_CATCH:        return "/app/resources/icons/timer-catch.svg";
            case CONDITIONAL_CATCH:  return "/app/resources/icons/conditional-catch.svg";
            case LINK_CATCH:         return "/app/resources/icons/link-catch.svg";
            case SIGNAL_CATCH:       return "/app/resources/icons/signal-catch.svg";
            case ERROR_CATCH:        return "/app/resources/icons/error-catch.svg";
            case MESSAGE_THROW:      return "/app/resources/icons/message-throw.svg";
            case LINK_THROW:         return "/app/resources/icons/link-throw.svg";
            case SIGNAL_THROW:       return "/app/resources/icons/signal-throw.svg";
            case MESSAGE_END:        return "/app/resources/icons/message-end.svg";
            case SIGNAL_END:         return "/app/resources/icons/signal-end.svg";
            case ERROR_END:          return "/app/resources/icons/error-end.svg";
            default:                 return null;
        }
    }

    private static double halfWidth(DiagramElement element) {
        switch (element.getType()) {
            case TASK:              return taskWidth(element.getLabel()) / 2;
            case EXCLUSIVE_GATEWAY: return GW_HALF;
            default:                return EVENT_R;
        }
    }

    private static double halfHeight(DiagramElement element) {
        switch (element.getType()) {
            case TASK:              return TASK_H / 2;
            case EXCLUSIVE_GATEWAY: return GW_HALF;
            default:                return EVENT_R;
        }
    }

    private static double taskWidth(String label) {
        Text measure = new Text(label == null ? "" : label);
        measure.setFont(Font.font(TASK_FONT_SIZE));
        double width = measure.getLayoutBounds().getWidth() + TASK_PAD_X;
        return Math.max(TASK_W, Math.min(TASK_MAX_W, width));
    }
}
