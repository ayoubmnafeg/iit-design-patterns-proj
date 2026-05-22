package app.view;

import app.logging.AppLogger;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class LogConsoleView extends VBox {

    private static final String TOGGLE_BASE =
        "-fx-background-color: transparent; -fx-border-color: #d1d5db; " +
        "-fx-border-radius: 6; -fx-background-radius: 6; " +
        "-fx-padding: 4 10 4 10; -fx-font-size: 12px; -fx-cursor: hand;";
    private static final String TOGGLE_ACTIVE =
        "-fx-background-color: #ede9ff; -fx-border-color: #7c3aed; " +
        "-fx-border-radius: 6; -fx-background-radius: 6; " +
        "-fx-padding: 4 10 4 10; -fx-font-size: 12px; -fx-cursor: hand;";

    private final ToggleButton consoleBtn;
    private final ToggleButton fileBtn;
    private final ToggleButton dbBtn;
    private final ToggleButton uiBtn;
    private final TextArea textArea;

    public LogConsoleView() {
        setStyle("-fx-background-color: #ffffff;");

        // ── Header ────────────────────────────────────────────────────────────
        Label title = new Label("Log Console");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");

        consoleBtn = new ToggleButton("Console");
        fileBtn    = new ToggleButton("File");
        dbBtn      = new ToggleButton("DB");
        uiBtn      = new ToggleButton("UI");

        ToggleGroup group = new ToggleGroup();
        consoleBtn.setToggleGroup(group);
        fileBtn.setToggleGroup(group);
        dbBtn.setToggleGroup(group);
        uiBtn.setToggleGroup(group);

        consoleBtn.setStyle(TOGGLE_BASE);
        fileBtn.setStyle(TOGGLE_BASE);
        dbBtn.setStyle(TOGGLE_BASE);
        uiBtn.setStyle(TOGGLE_BASE);

        consoleBtn.selectedProperty().addListener((obs, o, n) -> consoleBtn.setStyle(n ? TOGGLE_ACTIVE : TOGGLE_BASE));
        fileBtn.selectedProperty().addListener((obs, o, n)    -> fileBtn.setStyle(n ? TOGGLE_ACTIVE : TOGGLE_BASE));
        dbBtn.selectedProperty().addListener((obs, o, n)      -> dbBtn.setStyle(n ? TOGGLE_ACTIVE : TOGGLE_BASE));
        uiBtn.selectedProperty().addListener((obs, o, n)      -> uiBtn.setStyle(n ? TOGGLE_ACTIVE : TOGGLE_BASE));

        // Prevent deselecting all — at least one always selected
        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) oldToggle.setSelected(true);
        });

        Region spacer = new Region();
        HBox header = new HBox(8, title, spacer, consoleBtn, fileBtn, dbBtn, uiBtn);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 12, 10, 12));

        // ── TextArea ──────────────────────────────────────────────────────────
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setFont(Font.font("Monospace", 12));
        textArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #00ff88; " +
                          "-fx-background-color: #1e1e1e;");
        VBox.setVgrow(textArea, Priority.ALWAYS);

        // ── Observer: append entries ──────────────────────────────────────────
        AppLogger.getInstance().getEntries().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(entry -> textArea.appendText(entry + "\n"));
                    javafx.application.Platform.runLater(() -> textArea.setScrollTop(Double.MAX_VALUE));
                }
            }
        });

        getChildren().addAll(header, new Separator(), textArea);
        setPrefWidth(500);
        setPrefHeight(350);
    }

    public ToggleButton getConsoleBtn() { return consoleBtn; }
    public ToggleButton getFileBtn()    { return fileBtn; }
    public ToggleButton getDbBtn()      { return dbBtn; }
    public ToggleButton getUiBtn()     { return uiBtn; }
}
