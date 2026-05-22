package app.controller;

import app.model.Diagram;
import app.service.DiagramService;
import app.service.LoggingService;
import app.view.CanvasView;
import app.view.CanvasControlsView;
import app.view.MainView;
import app.view.PaletteView;
import javafx.scene.control.ChoiceDialog;
import app.view.LogConsoleView;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

        canvasController = new CanvasController(diagramService, paletteController, canvasView, paletteView);

        paletteView.setOnSelectTool(() -> {
            paletteController.setSelectMode();
            canvasView.setPanEnabled(false);
            canvasView.setSelectMode(true);
        });
        paletteView.setOnHandTool(() -> {
            paletteController.setHandMode();
            canvasView.setPanEnabled(true);
        });
        paletteView.setOnElementSelected(type -> {
            canvasView.setPanEnabled(false);
            paletteController.selectTool(type);
        });
        paletteView.setOnLoad(this::handleLoad);
        paletteView.setOnCycleLogger(() -> {
            paletteController.clearAll();
            cycleLogger();
        });

        controlsView.setOnZoomOut(canvasView::zoomOut);
        controlsView.setOnZoomIn(canvasView::zoomIn);
        controlsView.setOnOpenLogs(this::openLogConsole);
    }

    public void handleSave() {
        diagramService.saveDiagram();
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
        });
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
            logConsoleStage.setOnCloseRequest(e -> logConsoleStage = null);
        }
        logConsoleStage.show();
        logConsoleStage.toFront();
    }
}
