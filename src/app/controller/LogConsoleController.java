package app.controller;

import app.service.LoggingService;
import app.view.LogConsoleView;

public class LogConsoleController {

    public LogConsoleController(LogConsoleView view, LoggingService loggingService) {
        syncActiveToggle(view, loggingService);

        view.getConsoleBtn().setOnAction(e -> {
            loggingService.setStrategyByIndex(0);
            loggingService.log("Logger switched to: Console");
        });

        view.getFileBtn().setOnAction(e -> {
            loggingService.setStrategyByIndex(1);
            loggingService.log("Logger switched to: File");
        });

        view.getDbBtn().setOnAction(e -> {
            boolean ok = loggingService.setStrategyByIndex(2);
            if (!ok) {
                view.getDbBtn().setDisable(true);
                view.getDbBtn().setSelected(false);
                loggingService.setStrategyByIndex(0);
                view.getConsoleBtn().setSelected(true);
                loggingService.log("DB logger unavailable — reverted to Console");
            } else {
                loggingService.log("Logger switched to: DB");
            }
        });

        view.getUiBtn().setOnAction(e -> {
            loggingService.setStrategyByIndex(3);
            loggingService.log("Logger switched to: UI (in-app list only)");
        });
    }

    private void syncActiveToggle(LogConsoleView view, LoggingService loggingService) {
        String current = loggingService.getCurrentStrategyName();
        if ("File".equals(current)) {
            view.getFileBtn().setSelected(true);
        } else if ("Database".equals(current)) {
            view.getDbBtn().setSelected(true);
        } else if ("UI".equals(current)) {
            view.getUiBtn().setSelected(true);
        } else {

            view.getConsoleBtn().setSelected(true);
        }
    }
}
