package app.view;

import app.model.ElementType;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Popup;

import java.util.function.Consumer;

public class PaletteView extends HBox {

    private static final int    ICON_SIZE  = 28;
    private static final int    VAR_SIZE   = 26;
    private static final String BTN_BASE   =
        "-fx-background-color: transparent; -fx-border-color: transparent; " +
        "-fx-background-radius: 8; -fx-border-radius: 8; " +
        "-fx-min-width: 42; -fx-min-height: 42; " +
        "-fx-max-width: 42; -fx-max-height: 42; -fx-cursor: hand;";
    private static final String BTN_ACTIVE =
        BTN_BASE +
        "-fx-border-color: transparent transparent #4a90d9 transparent; " +
        "-fx-border-width: 0 0 3 0;";
    private static final String VAR_BTN    =
        "-fx-background-color: transparent; -fx-border-color: transparent; " +
        "-fx-background-radius: 6; -fx-border-radius: 6; " +
        "-fx-min-width: 36; -fx-min-height: 36; " +
        "-fx-max-width: 36; -fx-max-height: 36; -fx-cursor: hand;";
    private static final String VAR_BTN_HV = VAR_BTN + "-fx-background-color: #ede9ff;";

    private Button activeBtn = null;
    private Popup  openPopup = null;

    private Consumer<ElementType> onElementSelected;
    private Runnable onLoad, onCycleLogger, onSelectTool, onHandTool;

    public PaletteView() {
        setStyle("-fx-background-color: white; -fx-background-radius: 40; " +
                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 4); " +
                 "-fx-padding: 8 14 8 14;");
        setAlignment(Pos.CENTER);
        setSpacing(2);
        setMaxWidth(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);

        // ── Tools ─────────────────────────────────────────────────────────────
        addPng("select", "/app/resources/icons/selection.png", "Select",     () -> { if (onSelectTool != null) onSelectTool.run(); });
        addPng("hand",   "/app/resources/icons/cursor.png",    "Pan Canvas", () -> { if (onHandTool   != null) onHandTool.run();   });

        sep();

        // ── Start Event (base) + variants flyout ──────────────────────────────
        addGroupBtn("start", "/app/resources/icons/start-event.svg", "Start Event",
            ElementType.START_EVENT,
            new String[]{"msg-start",   "timer-start",  "cond-start",  "sig-start",   "err-start"},
            new String[]{"/app/resources/icons/message-start.svg",
                         "/app/resources/icons/timer-start.svg",
                         "/app/resources/icons/conditional-start.svg",
                         "/app/resources/icons/signal-start.svg",
                         "/app/resources/icons/error-start.svg"},
            new String[]{"Message Start","Timer Start","Conditional Start","Signal Start","Error Start"},
            new ElementType[]{ElementType.MESSAGE_START, ElementType.TIMER_START,
                              ElementType.CONDITIONAL_START, ElementType.SIGNAL_START,
                              ElementType.ERROR_START});

        // ── Intermediate Event (base) + variants flyout ───────────────────────
        addGroupBtn("inter", "/app/resources/icons/intermediate-event.svg", "Intermediate Event",
            ElementType.INTERMEDIATE_EVENT,
            new String[]{"msg-catch","timer-catch","cond-catch","link-catch","sig-catch","err-catch",
                         "msg-throw","link-throw","sig-throw"},
            new String[]{"/app/resources/icons/message-catch.svg",
                         "/app/resources/icons/timer-catch.svg",
                         "/app/resources/icons/conditional-catch.svg",
                         "/app/resources/icons/link-catch.svg",
                         "/app/resources/icons/signal-catch.svg",
                         "/app/resources/icons/error-catch.svg",
                         "/app/resources/icons/message-throw.svg",
                         "/app/resources/icons/link-throw.svg",
                         "/app/resources/icons/signal-throw.svg"},
            new String[]{"Message Catch","Timer Catch","Conditional Catch","Link Catch",
                         "Signal Catch","Error Catch",
                         "Message Throw","Link Throw","Signal Throw"},
            new ElementType[]{ElementType.MESSAGE_CATCH, ElementType.TIMER_CATCH,
                              ElementType.CONDITIONAL_CATCH, ElementType.LINK_CATCH,
                              ElementType.SIGNAL_CATCH, ElementType.ERROR_CATCH,
                              ElementType.MESSAGE_THROW, ElementType.LINK_THROW,
                              ElementType.SIGNAL_THROW});

        // ── End Event (base) + variants flyout ───────────────────────────────
        addGroupBtn("end", "/app/resources/icons/end-event.svg", "End Event",
            ElementType.END_EVENT,
            new String[]{"msg-end", "sig-end", "err-end"},
            new String[]{"/app/resources/icons/message-end.svg",
                         "/app/resources/icons/signal-end.svg",
                         "/app/resources/icons/error-end.svg"},
            new String[]{"Message End","Signal End","Error End"},
            new ElementType[]{ElementType.MESSAGE_END, ElementType.SIGNAL_END, ElementType.ERROR_END});

        sep();

        // ── Single buttons ────────────────────────────────────────────────────
        addSvgBtn("task", "/app/resources/icons/task.svg",         "Task",          ElementType.TASK);
        addSvgBtn("xor",  "/app/resources/icons/gateway.svg",       "Gateway (XOR)", ElementType.EXCLUSIVE_GATEWAY);
        addSvgBtn("flow", "/app/resources/icons/sequence-flow.svg", "Sequence Flow", ElementType.SEQUENCE_FLOW);

        sep();

        // ── Actions ───────────────────────────────────────────────────────────
        addActionSvgBtn("/app/resources/icons/log-script.svg", "Cycle Logger",
            () -> { if (onCycleLogger != null) onCycleLogger.run(); });
    }

    // ── Group button with flyout ──────────────────────────────────────────────

    private void addGroupBtn(String key, String svgPath, String tip, ElementType baseType,
                             String[] varKeys, String[] varSvgs, String[] varTips,
                             ElementType[] varTypes) {
        Button btn = makeBtn(tip);
        btn.setGraphic(wrap(SvgIcon.render(svgPath, ICON_SIZE), ICON_SIZE));

        Popup popup = buildFlyout(btn, key, svgPath, varKeys, varSvgs, varTips, varTypes);

        btn.setOnAction(e -> {
            if (openPopup != null && openPopup == popup && popup.isShowing()) {
                popup.hide();
                openPopup = null;
                return;
            }
            closeOpenPopup();
            // Default: select base type immediately
            activateBase(btn, key, svgPath);
            selectTool(baseType);
            // Show flyout above button
            showPopupAbove(popup, btn);
            openPopup = popup;
        });

        getChildren().add(btn);
    }

    private Popup buildFlyout(Button parentBtn, String parentKey, String parentSvg,
                              String[] varKeys, String[] varSvgs, String[] varTips,
                              ElementType[] varTypes) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 12, 0, 0, 3); " +
                     "-fx-padding: 6 10 6 10;");

        // Separator label
        if (varKeys.length > 6) {
            // split catch / throw with a tiny label
            for (int i = 0; i < varKeys.length; i++) {
                if (i == 6) {
                    Label lbl = new Label("│");
                    lbl.setStyle("-fx-text-fill: #ccc; -fx-font-size: 16;");
                    row.getChildren().add(lbl);
                }
                row.getChildren().add(makeVarBtn(varKeys[i], varSvgs[i], varTips[i], varTypes[i],
                                                  parentBtn, parentKey, parentSvg));
            }
        } else {
            for (int i = 0; i < varKeys.length; i++) {
                row.getChildren().add(makeVarBtn(varKeys[i], varSvgs[i], varTips[i], varTypes[i],
                                                  parentBtn, parentKey, parentSvg));
            }
        }

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(row);
        popup.setOnAutoHide(e -> openPopup = null);
        return popup;
    }

    private Button makeVarBtn(String key, String svgPath, String tip, ElementType type,
                               Button parentBtn, String parentKey, String parentSvg) {
        Button vb = new Button();
        vb.setStyle(VAR_BTN);
        vb.setTooltip(new Tooltip(tip));
        vb.setGraphic(wrap(SvgIcon.render(svgPath, VAR_SIZE), VAR_SIZE));
        vb.setOnMouseEntered(e -> vb.setStyle(VAR_BTN_HV));
        vb.setOnMouseExited(e  -> vb.setStyle(VAR_BTN));
        vb.setOnAction(e -> {
            // Update parent button's icon to the chosen variant
            parentBtn.setGraphic(wrap(SvgIcon.render(svgPath, ICON_SIZE), ICON_SIZE));
            activateBase(parentBtn, parentKey, svgPath);
            selectTool(type);
            closeOpenPopup();
        });
        return vb;
    }

    // ── Simple SVG button ─────────────────────────────────────────────────────

    private void addSvgBtn(String key, String svgPath, String tip, ElementType type) {
        Button btn = makeBtn(tip);
        btn.setGraphic(wrap(SvgIcon.render(svgPath, ICON_SIZE), ICON_SIZE));
        btn.setOnAction(e -> {
            closeOpenPopup();
            activateBase(btn, key, svgPath);
            selectTool(type);
        });
        getChildren().add(btn);
    }

    private void addActionSvgBtn(String svgPath, String tip, Runnable action) {
        Button btn = makeBtn(tip);
        btn.setGraphic(wrap(SvgIcon.render(svgPath, ICON_SIZE), ICON_SIZE));
        btn.setOnAction(e -> {
            closeOpenPopup();
            if (activeBtn != null) activeBtn.setStyle(BTN_BASE);
            activeBtn = null;
            action.run();
        });
        getChildren().add(btn);
    }

    // ── PNG button ────────────────────────────────────────────────────────────

    private void addPng(String key, String path, String tip, Runnable action) {
        Button btn = makeBtn(tip);
        try {
            Image img = new Image(getClass().getResourceAsStream(path),
                                  ICON_SIZE * 2, ICON_SIZE * 2, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(ICON_SIZE); iv.setFitHeight(ICON_SIZE);
            iv.setSmooth(true); iv.setPreserveRatio(true);
            btn.setGraphic(iv);
        } catch (Exception ex) { btn.setText(key); }
        btn.setOnAction(e -> {
            closeOpenPopup();
            if (activeBtn != null) { activeBtn.setStyle(BTN_BASE); }
            activeBtn = btn;
            btn.setStyle(BTN_ACTIVE);
            action.run();
        });
        getChildren().add(btn);
    }

    // ── Text button ───────────────────────────────────────────────────────────

    private void addTextBtn(String key, String label, String tip, Runnable action) {
        Button btn = makeBtn(tip);
        btn.setText(label);
        boolean wide = label.length() > 2;
        btn.setStyle(wide
            ? BTN_BASE.replace("min-width: 36", "min-width: 52").replace("max-width: 36", "max-width: 52")
                      + "-fx-font-size: 12px;"
            : BTN_BASE + "-fx-font-size: 14px;");
        btn.setOnAction(e -> { closeOpenPopup(); action.run(); });
        getChildren().add(btn);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Button makeBtn(String tip) {
        Button btn = new Button();
        btn.setStyle(BTN_BASE);
        btn.setTooltip(new Tooltip(tip));
        return btn;
    }

    private void sep() {
        getChildren().add(new Separator(Orientation.VERTICAL));
    }

    private static ImageView wrap(ImageView iv, int size) {
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        return iv;
    }

    private void activateBase(Button btn, String key, String svgPath) {
        if (activeBtn != null) activeBtn.setStyle(BTN_BASE);
        activeBtn = btn;
        btn.setStyle(BTN_ACTIVE);
        btn.setGraphic(wrap(SvgIcon.render(svgPath, ICON_SIZE), ICON_SIZE));
    }

    public void clearActive() {
        if (activeBtn != null) { activeBtn.setStyle(BTN_BASE); activeBtn = null; }
        closeOpenPopup();
    }

    private void closeOpenPopup() {
        if (openPopup != null) { openPopup.hide(); openPopup = null; }
    }

    private void showPopupAbove(Popup popup, Button btn) {
        // Show after layout so we have screen coords
        btn.getScene().getWindow().requestFocus();
        javafx.application.Platform.runLater(() -> {
            javafx.geometry.Bounds b = btn.localToScreen(btn.getBoundsInLocal());
            if (b == null) return;
            popup.show(btn.getScene().getWindow(),
                       b.getMinX() - 40,
                       b.getMinY() - 60);
        });
    }

    private void selectTool(ElementType type) {
        if (onElementSelected != null) onElementSelected.accept(type);
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setOnElementSelected(Consumer<ElementType> h) { onElementSelected = h; }
    public void setOnLoad(Runnable h)        { onLoad = h; }
    public void setOnCycleLogger(Runnable h) { onCycleLogger = h; }
    public void setOnSelectTool(Runnable h)  { onSelectTool = h; }
    public void setOnHandTool(Runnable h)    { onHandTool = h; }
}
