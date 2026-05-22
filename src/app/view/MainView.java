package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.List;
import java.util.function.IntConsumer;

public class MainView extends StackPane {
    private final CanvasView  canvasView;
    private final PaletteView paletteView;
    private final CanvasControlsView controlsView;
    private final CanvasControlsView topZoomControlsView;
    private final MenuButton appMenuButton;
    private final Menu recentProjectsMenu;
    private final MenuItem saveProjectItem;
    private Runnable onSaveProject;
    private Runnable onNewProject;
    private Runnable onImportJson;
    private Runnable onExportJson;
    private Runnable onAppMenuShowing;
    private IntConsumer onRecentProjectSelected;

    public MainView(app.model.Diagram diagram) {
        canvasView  = new CanvasView(diagram);
        paletteView = new PaletteView();
        controlsView = new CanvasControlsView();
        topZoomControlsView = new CanvasControlsView(false);
        recentProjectsMenu = new Menu("Recent Projects");
        saveProjectItem = new MenuItem("Save Project...");
        appMenuButton = createAppMenuButton();

        canvasView.prefWidthProperty().bind(widthProperty());
        canvasView.prefHeightProperty().bind(heightProperty());

        paletteView.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        paletteView.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        controlsView.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        controlsView.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        topZoomControlsView.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        topZoomControlsView.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        appMenuButton.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        appMenuButton.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        StackPane.setAlignment(appMenuButton, Pos.TOP_LEFT);
        StackPane.setMargin(appMenuButton, new Insets(20, 0, 0, 20));
        StackPane.setAlignment(paletteView, Pos.BOTTOM_CENTER);
        StackPane.setMargin(paletteView, new Insets(0, 0, 20, 0));
        StackPane.setAlignment(controlsView, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(controlsView, new Insets(0, 20, 20, 0));
        StackPane.setAlignment(topZoomControlsView, Pos.TOP_RIGHT);
        StackPane.setMargin(topZoomControlsView, new Insets(20, 20, 0, 0));

        getChildren().addAll(canvasView, appMenuButton, paletteView, controlsView, topZoomControlsView);
        setStyle("-fx-background-color: #f8f8f8;");
    }

    public CanvasView  getCanvasView()  { return canvasView; }
    public PaletteView getPaletteView() { return paletteView; }
    public CanvasControlsView getControlsView() { return controlsView; }
    public CanvasControlsView getTopZoomControlsView() { return topZoomControlsView; }

    public void setOnSaveProject(Runnable handler) {
        onSaveProject = handler;
    }

    public void setSaveProjectAvailable(boolean available) {
        saveProjectItem.setDisable(!available);
    }

    public void setOnNewProject(Runnable handler) {
        onNewProject = handler;
    }

    public void setOnExportJson(Runnable handler) {
        onExportJson = handler;
    }

    public void setOnImportJson(Runnable handler) {
        onImportJson = handler;
    }

    public void setOnAppMenuShowing(Runnable handler) {
        onAppMenuShowing = handler;
    }

    public void setOnRecentProjectSelected(IntConsumer handler) {
        onRecentProjectSelected = handler;
    }

    public void setRecentProjects(List<String[]> diagrams) {
        recentProjectsMenu.getItems().clear();
        if (diagrams.isEmpty()) {
            MenuItem emptyItem = new MenuItem("No recent projects");
            emptyItem.setDisable(true);
            recentProjectsMenu.getItems().add(emptyItem);
            recentProjectsMenu.setDisable(true);
            return;
        }

        recentProjectsMenu.setDisable(false);
        for (String[] diagram : diagrams) {
            int id = Integer.parseInt(diagram[0]);
            MenuItem item = new MenuItem(diagram[0] + " - " + diagram[1]);
            item.setOnAction(event -> {
                if (onRecentProjectSelected != null) {
                    onRecentProjectSelected.accept(id);
                }
            });
            recentProjectsMenu.getItems().add(item);
        }
    }

    private MenuButton createAppMenuButton() {
        MenuButton button = new MenuButton();
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip("Project Menu"));
        button.setGraphic(createHamburgerGlyph());
        button.getStyleClass().add("project-menu-button");

        MenuItem newProjectItem = new MenuItem("Start New Project");
        newProjectItem.setOnAction(event -> run(onNewProject));

        saveProjectItem.setOnAction(event -> run(onSaveProject));

        MenuItem exportJsonItem = new MenuItem("Export as JSON...");
        exportJsonItem.setOnAction(event -> run(onExportJson));

        MenuItem importJsonItem = new MenuItem("Import Project...");
        importJsonItem.setOnAction(event -> run(onImportJson));

        button.getItems().addAll(
            newProjectItem,
            saveProjectItem,
            importJsonItem,
            exportJsonItem,
            new SeparatorMenuItem(),
            recentProjectsMenu
        );
        button.setOnShowing(event -> run(onAppMenuShowing));
        return button;
    }

    private StackPane createHamburgerGlyph() {
        StackPane glyph = new StackPane();
        glyph.setMinSize(16, 14);
        glyph.setPrefSize(16, 14);
        glyph.setMaxSize(16, 14);

        javafx.scene.layout.VBox lines = new javafx.scene.layout.VBox(4);
        lines.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Line line = new Line(0, 0, 16, 0);
            line.setStroke(Color.web("#374151"));
            line.setStrokeWidth(1.8);
            line.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            lines.getChildren().add(line);
        }
        glyph.getChildren().add(lines);
        return glyph;
    }

    private static void run(Runnable action) {
        if (action != null) {
            action.run();
        }
    }
}
