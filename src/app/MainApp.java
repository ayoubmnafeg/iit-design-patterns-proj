package app;

import app.controller.MainController;
import app.model.Diagram;
import app.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Diagram diagram   = new Diagram("Untitled");
        MainView mainView = new MainView(diagram);
        MainController controller = new MainController(mainView, diagram);

        Scene scene = new Scene(mainView, 1200, 800);
        try {
            java.net.URL cssUrl = getClass().getResource("/app/resources/canvas.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        } catch (Exception ignored) {}
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
            controller::handleSave
        );

        stage.setTitle("BPMN Modeler");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

