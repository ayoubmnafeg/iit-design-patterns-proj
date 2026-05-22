package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class MainView extends StackPane {
    private final CanvasView  canvasView;
    private final PaletteView paletteView;
    private final CanvasControlsView controlsView;

    public MainView(app.model.Diagram diagram) {
        canvasView  = new CanvasView(diagram);
        paletteView = new PaletteView();
        controlsView = new CanvasControlsView();

        canvasView.prefWidthProperty().bind(widthProperty());
        canvasView.prefHeightProperty().bind(heightProperty());

        paletteView.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        paletteView.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        controlsView.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        controlsView.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        StackPane.setAlignment(paletteView, Pos.BOTTOM_CENTER);
        StackPane.setMargin(paletteView, new Insets(0, 0, 20, 0));
        StackPane.setAlignment(controlsView, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(controlsView, new Insets(0, 20, 20, 0));

        getChildren().addAll(canvasView, paletteView, controlsView);
        setStyle("-fx-background-color: #f8f8f8;");
    }

    public CanvasView  getCanvasView()  { return canvasView; }
    public PaletteView getPaletteView() { return paletteView; }
    public CanvasControlsView getControlsView() { return controlsView; }
}
