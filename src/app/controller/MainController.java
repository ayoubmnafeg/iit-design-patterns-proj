package app.controller;

import app.model.Diagram;
import app.service.DiagramService;
import app.service.LoggingService;
import app.view.CanvasView;
import app.view.CanvasControlsView;
import app.view.MainView;
import app.view.PaletteView;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import app.view.LogConsoleView;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class MainController {
    private final DiagramService diagramService;
    private final LoggingService loggingService;
    private final PaletteController paletteController;
    private final CanvasController canvasController;
    private Stage logConsoleStage;
    private LogConsoleView logConsoleView;
    private final MainView mainView;

    public MainController(MainView mainView, Diagram diagram) {
        this.mainView = mainView;
        loggingService         = new LoggingService();
        diagramService         = new DiagramService(diagram, loggingService);
        paletteController      = new PaletteController();

        CanvasView canvasView  = mainView.getCanvasView();
        PaletteView paletteView = mainView.getPaletteView();
        CanvasControlsView controlsView = mainView.getControlsView();
        CanvasControlsView topZoomControlsView = mainView.getTopZoomControlsView();

        canvasController = new CanvasController(
            diagramService,
            paletteController,
            canvasView,
            paletteView,
            this::refreshSaveAvailability
        );

        paletteView.setOnSelectTool(() -> {
            paletteController.setSelectMode();
            canvasView.setPanEnabled(false);
            canvasView.setEraseMode(false);
            canvasView.setSelectMode(true);
        });
        paletteView.setOnHandTool(() -> {
            paletteController.setHandMode();
            canvasView.setEraseMode(false);
            canvasView.setPanEnabled(true);
        });
        paletteView.setOnDeleteTool(() -> {
            paletteController.setDeleteMode();
            canvasView.setPanEnabled(false);
            canvasView.setSelectMode(false);
            canvasView.setEraseMode(true);
        });
        paletteView.setOnElementSelected(type -> {
            canvasView.setPanEnabled(false);
            canvasView.setEraseMode(false);
            paletteController.selectTool(type);
        });
        paletteView.setOnLoad(this::handleLoad);
        paletteView.setOnCycleLogger(() -> {
            paletteController.clearAll();
            cycleLogger();
        });
        mainView.setOnSaveProject(this::handleSave);
        mainView.setOnNewProject(this::handleNewProject);
        mainView.setOnImportJson(this::handleImportJson);
        mainView.setOnExportJson(this::handleExportJson);
        mainView.setOnRecentProjectSelected(this::handleRecentProject);
        mainView.setOnAppMenuShowing(this::refreshRecentProjectsMenu);

        controlsView.setOnUndo(() -> {
            diagramService.undo();
            refreshProjectState();
        });
        controlsView.setOnRedo(() -> {
            diagramService.redo();
            refreshProjectState();
        });
        controlsView.setOnOpenLogs(this::openLogConsole);
        topZoomControlsView.setOnZoomOut(() -> {
            canvasView.zoomOut();
            refreshZoomControls();
        });
        topZoomControlsView.setOnZoomIn(() -> {
            canvasView.zoomIn();
            refreshZoomControls();
        });
        refreshZoomControls();
        diagram.getElements().addListener((javafx.collections.ListChangeListener<app.model.DiagramElement>) change ->
            refreshProjectState()
        );
        refreshHistoryControls();
        refreshSaveAvailability();
    }

    public void handleSave() {
        if (diagramService.isSavedInDatabase()) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog(diagramService.getDiagram().getName());
        dialog.setTitle("Save Project");
        dialog.setHeaderText("Name this BPMN project");
        dialog.setContentText("Project name:");
        dialog.setOnShown(event -> dialog.getEditor().selectAll());

        Optional<String> result = dialog.showAndWait();
        result.map(String::trim)
            .filter(name -> !name.isEmpty())
            .ifPresent(name -> {
                diagramService.saveDiagram(name);
                refreshRecentProjectsMenu();
                refreshSaveAvailability();
            });
    }

    private void handleLoad() {
        List<String[]> diagrams = diagramService.listSavedDiagrams();
        if (diagrams.isEmpty()) return;

        List<String> choices = diagrams.stream()
            .map(d -> d[0] + " — " + d[1])
            .collect(java.util.stream.Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Load Diagram");
        dialog.setHeaderText("Select a diagram to load:");
        dialog.setContentText("Diagram:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(choice -> {
            int id = Integer.parseInt(choice.split(" — ")[0].trim());
            diagramService.loadDiagram(id);
            refreshProjectState();
        });
    }

    private void handleNewProject() {
        diagramService.newDiagram();
        paletteController.clearAll();
        CanvasView canvasView = mainView.getCanvasView();
        canvasView.setPanEnabled(false);
        canvasView.setEraseMode(false);
        canvasView.setSelectMode(true);
        refreshProjectState();
    }

    private void handleRecentProject(int diagramId) {
        diagramService.loadDiagram(diagramId);
        refreshProjectState();
    }

    private void handleExportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Diagram JSON");
        chooser.setInitialFileName(defaultExportFileName());
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        File file = chooser.showSaveDialog(mainView.getScene().getWindow());
        if (file != null) {
            diagramService.exportJson(file.toPath());
        }
    }

    private void handleImportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Diagram JSON");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        File file = chooser.showOpenDialog(mainView.getScene().getWindow());
        if (file != null) {
            diagramService.importJson(file.toPath());
            paletteController.clearAll();
            CanvasView canvasView = mainView.getCanvasView();
            canvasView.setPanEnabled(false);
            canvasView.setEraseMode(false);
            canvasView.setSelectMode(true);
            refreshProjectState();
        }
    }

    private void refreshRecentProjectsMenu() {
        try {
            mainView.setRecentProjects(diagramService.listSavedDiagrams());
        } catch (RuntimeException e) {
            mainView.setRecentProjects(java.util.Collections.emptyList());
        }
    }

    private String defaultExportFileName() {
        String name = diagramService.getDiagram().getName();
        if (name == null || name.trim().isEmpty()) {
            name = "diagram";
        }
        String safeName = name.trim().replaceAll("[^A-Za-z0-9._-]+", "-");
        return safeName + ".json";
    }

    private void cycleLogger() {
        String name = loggingService.cycleStrategy();
        loggingService.log("Logger switched to: " + name);
    }

    private void openLogConsole() {
        if (logConsoleStage == null) {
            if (logConsoleView == null) {
                logConsoleView = new LogConsoleView();
                new LogConsoleController(logConsoleView, loggingService);
            }
            Scene scene = new Scene(logConsoleView, 500, 350);
            logConsoleStage = new Stage();
            logConsoleStage.setTitle("Log Console");
            logConsoleStage.setScene(scene);
            if (mainView.getScene() != null) {
                logConsoleStage.initOwner(mainView.getScene().getWindow());
            }
            logConsoleStage.setOnCloseRequest(e -> {
                e.consume();
                logConsoleStage.hide();
            });
        }
        logConsoleStage.show();
        logConsoleStage.toFront();
    }

    private void refreshHistoryControls() {
        mainView.getControlsView().setHistoryAvailability(
            diagramService.canUndo(),
            diagramService.canRedo()
        );
    }

    private void refreshSaveAvailability() {
        mainView.setSaveProjectAvailable(!diagramService.isSavedInDatabase());
    }

    private void refreshProjectState() {
        refreshHistoryControls();
        refreshSaveAvailability();
    }

    private void refreshZoomControls() {
        mainView.getTopZoomControlsView().setZoomPercent(
            mainView.getCanvasView().getZoomPercent()
        );
    }
}
