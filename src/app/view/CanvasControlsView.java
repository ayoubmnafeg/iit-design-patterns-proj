package app.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class CanvasControlsView extends HBox {
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
    private Runnable onOpenLogs;

    public CanvasControlsView() {
        setAlignment(Pos.CENTER);
        setSpacing(6);
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);
        setPickOnBounds(false);

        HBox zoomGroup = new HBox(2);
        zoomGroup.setAlignment(Pos.CENTER);
        zoomGroup.setMaxWidth(Region.USE_PREF_SIZE);
        zoomGroup.setMaxHeight(Region.USE_PREF_SIZE);
        zoomGroup.setStyle(ZOOM_GROUP_STYLE);
        zoomGroup.getChildren().addAll(
            createZoomButton("-", "Zoom Out", () -> run(onZoomOut)),
            createZoomButton("+", "Zoom In", () -> run(onZoomIn))
        );

        Button loggerButton = createLoggerButton();

        getChildren().addAll(zoomGroup, loggerButton);
    }

    public void setOnZoomOut(Runnable handler) {
        onZoomOut = handler;
    }

    public void setOnZoomIn(Runnable handler) {
        onZoomIn = handler;
    }

    public void setOnOpenLogs(Runnable handler) {
        onOpenLogs = handler;
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

    private static void run(Runnable action) {
        if (action != null) {
            action.run();
        }
    }
}
