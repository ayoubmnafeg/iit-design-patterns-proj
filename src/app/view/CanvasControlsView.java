package app.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.SVGPath;

public class CanvasControlsView extends HBox {
    public enum ButtonMode {
        ZOOM,
        HISTORY
    }

    private static final String ZOOM_GROUP_STYLE =
        "-fx-background-color: white; -fx-background-radius: 18; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.16), 12, 0, 0, 3); " +
        "-fx-padding: 3 5 3 5;";
    private static final String LOG_BUTTON_STYLE =
        "-fx-background-color: white; -fx-border-color: transparent; " +
        "-fx-background-radius: 16; -fx-border-radius: 16; " +
        "-fx-min-width: 32; -fx-min-height: 32; " +
        "-fx-max-width: 32; -fx-max-height: 32; " +
        "-fx-cursor: hand; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.16), 12, 0, 0, 3);";
    private static final String LOG_BUTTON_HOVER_STYLE = LOG_BUTTON_STYLE.replace("-fx-background-color: white;", "-fx-background-color: #f3f4f6;");
    private static final String ZOOM_BUTTON_STYLE =
        "-fx-background-color: transparent; -fx-border-color: transparent; " +
        "-fx-background-radius: 14; -fx-border-radius: 14; " +
        "-fx-min-width: 24; -fx-min-height: 24; " +
        "-fx-max-width: 24; -fx-max-height: 24; " +
        "-fx-font-size: 12px; -fx-font-weight: bold; " +
        "-fx-text-fill: #5f6368; -fx-cursor: hand;";
    private static final String ZOOM_BUTTON_HOVER_STYLE = ZOOM_BUTTON_STYLE + "-fx-background-color: #f3f4f6;";
    private static final int LOGGER_ICON_SIZE = 18;

    private Runnable onZoomOut;
    private Runnable onZoomIn;
    private Runnable onUndo;
    private Runnable onRedo;
    private Runnable onOpenLogs;
    private Button undoButton;
    private Button redoButton;
    private Label zoomLabel;

    public CanvasControlsView() {
        this(ButtonMode.HISTORY, true);
    }

    public CanvasControlsView(boolean showLoggerButton) {
        this(ButtonMode.ZOOM, showLoggerButton);
    }

    public CanvasControlsView(ButtonMode buttonMode, boolean showLoggerButton) {
        setAlignment(Pos.CENTER);
        setSpacing(6);
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
        setPickOnBounds(false);

        HBox buttonGroup = new HBox(2);
        buttonGroup.setAlignment(Pos.CENTER);
        buttonGroup.setMaxWidth(Region.USE_PREF_SIZE);
        buttonGroup.setMaxHeight(Region.USE_PREF_SIZE);
        buttonGroup.setStyle(ZOOM_GROUP_STYLE);

        if (buttonMode == ButtonMode.ZOOM) {
            zoomLabel = createZoomLabel();
            buttonGroup.getChildren().addAll(
                createZoomButton("-", "Zoom Out", () -> run(onZoomOut)),
                zoomLabel,
                createZoomButton("+", "Zoom In", () -> run(onZoomIn))
            );
        } else {
            undoButton = createHistoryButton("Undo", false, () -> run(onUndo));
            redoButton = createHistoryButton("Redo", true, () -> run(onRedo));
            buttonGroup.getChildren().addAll(
                undoButton,
                redoButton
            );
        }

        getChildren().add(buttonGroup);
        if (showLoggerButton) {
            getChildren().add(createLoggerButton());
        }
    }

    public void setOnZoomOut(Runnable handler) {
        onZoomOut = handler;
    }

    public void setOnZoomIn(Runnable handler) {
        onZoomIn = handler;
    }

    public void setOnUndo(Runnable handler) {
        onUndo = handler;
    }

    public void setOnRedo(Runnable handler) {
        onRedo = handler;
    }

    public void setOnOpenLogs(Runnable handler) {
        onOpenLogs = handler;
    }

    public void setHistoryAvailability(boolean canUndo, boolean canRedo) {
        if (undoButton != null) {
            undoButton.setDisable(!canUndo);
        }
        if (redoButton != null) {
            redoButton.setDisable(!canRedo);
        }
    }

    public void setZoomPercent(int percent) {
        if (zoomLabel != null) {
            zoomLabel.setText(percent + "%");
        }
    }

    private Button createZoomButton(String label, String tooltip, Runnable action) {
        Button button = new Button(label);
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip(tooltip));
        button.setGraphic(createZoomGlyph(label));
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setStyle(ZOOM_BUTTON_STYLE);
        button.setOnMouseEntered(event -> button.setStyle(ZOOM_BUTTON_HOVER_STYLE));
        button.setOnMouseExited(event -> button.setStyle(ZOOM_BUTTON_STYLE));
        button.setOnAction(event -> action.run());
        return button;
    }

    private Label createZoomLabel() {
        Label label = new Label("100%");
        label.setAlignment(Pos.CENTER);
        label.setMinWidth(42);
        label.setPrefWidth(42);
        label.setMaxWidth(42);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #5f6368;");
        return label;
    }

    private Button createHistoryButton(String tooltip, boolean pointsRight, Runnable action) {
        Button button = new Button();
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip(tooltip));
        button.setGraphic(createHistoryGlyph(pointsRight));
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setStyle(ZOOM_BUTTON_STYLE);
        button.setOnMouseEntered(event -> {
            if (!button.isDisabled()) {
                button.setStyle(ZOOM_BUTTON_HOVER_STYLE);
            }
        });
        button.setOnMouseExited(event -> button.setStyle(ZOOM_BUTTON_STYLE));
        button.setOnAction(event -> action.run());
        return button;
    }

    private Button createLoggerButton() {
        Button button = new Button();
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip("Open Log Console"));
        button.setGraphic(SvgIcon.render("/app/resources/icons/log-script.svg", LOGGER_ICON_SIZE));
        button.setStyle(LOG_BUTTON_STYLE);
        button.setOnMouseEntered(event -> button.setStyle(LOG_BUTTON_HOVER_STYLE));
        button.setOnMouseExited(event -> button.setStyle(LOG_BUTTON_STYLE));
        button.setOnAction(event -> run(onOpenLogs));
        return button;
    }

    private StackPane createZoomGlyph(String label) {
        StackPane glyph = new StackPane();
        glyph.setMinSize(10, 10);
        glyph.setPrefSize(10, 10);
        glyph.setMaxSize(10, 10);

        Line horizontal = new Line(0, 5, 10, 5);
        horizontal.setStroke(Color.web("#5f6368"));
        horizontal.setStrokeWidth(1.6);
        horizontal.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        glyph.getChildren().add(horizontal);

        if ("+".equals(label)) {
            Line vertical = new Line(5, 0, 5, 10);
            vertical.setStroke(Color.web("#5f6368"));
            vertical.setStrokeWidth(1.6);
            vertical.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            glyph.getChildren().add(vertical);
        }

        return glyph;
    }

    private StackPane createHistoryGlyph(boolean pointsRight) {
        StackPane glyph = new StackPane();
        glyph.setMinSize(16, 16);
        glyph.setPrefSize(16, 16);
        glyph.setMaxSize(16, 16);

        SVGPath arrow = new SVGPath();
        arrow.setContent(pointsRight
            ? "M10.5 4 L14 7.5 L10.5 11 M13.5 7.5 H7.5 C4.8 7.5 3 9.1 3 11.5 C3 13.2 4.1 14.4 5.5 14.9"
            : "M5.5 4 L2 7.5 L5.5 11 M2.5 7.5 H8.5 C11.2 7.5 13 9.1 13 11.5 C13 13.2 11.9 14.4 10.5 14.9"
        );
        arrow.setFill(Color.TRANSPARENT);
        arrow.setStroke(Color.web("#5f6368"));
        arrow.setStrokeWidth(1.7);
        arrow.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        arrow.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        glyph.getChildren().add(arrow);
        return glyph;
    }

    private static void run(Runnable action) {
        if (action != null) {
            action.run();
        }
    }
}
