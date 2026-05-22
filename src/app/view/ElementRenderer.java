package app.view;

import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.SequenceFlowElement;
import app.view.CanvasView.ConnectorClickHandler;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
    private static final double DOT_STROKE_W = 1.5;

    public static Group renderNode(DiagramElement element, ConnectorClickHandler onConnector,
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

        Label label = buildLabel(element.getLabel(), element);
        container.getChildren().add(label);

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

    private static Circle makeDot(double cx, double cy, DiagramElement element,
                                   double anchorOffX, double anchorOffY,
                                   ConnectorClickHandler onConnector) {
        Circle dot = new Circle(cx, cy, DOT_R);
        dot.setFill(DOT_FILL);
        dot.setStroke(DOT_STROKE);
        dot.setStrokeWidth(DOT_STROKE_W);
        dot.setOnMouseEntered(e -> { dot.setScaleX(1.3); dot.setScaleY(1.3); });
        dot.setOnMouseExited(e  -> { dot.setScaleX(1.0); dot.setScaleY(1.0); });
        dot.setOnMouseClicked(e -> {
            if (onConnector != null) {
                double worldX = element.getX() + anchorOffX;
                double worldY = element.getY() + anchorOffY;
                onConnector.accept(element, worldX, worldY);
            }
            e.consume();
        });
        return dot;
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

        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.5);

        double angle      = Math.atan2(y2 - y1, x2 - x1);
        double arrowLen   = 12;
        double arrowAngle = Math.toRadians(25);
        Polygon arrowHead = new Polygon(
            x2, y2,
            x2 - arrowLen * Math.cos(angle - arrowAngle), y2 - arrowLen * Math.sin(angle - arrowAngle),
            x2 - arrowLen * Math.cos(angle + arrowAngle), y2 - arrowLen * Math.sin(angle + arrowAngle)
        );
        arrowHead.setFill(Color.BLACK);
        return new Group(line, arrowHead);
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
