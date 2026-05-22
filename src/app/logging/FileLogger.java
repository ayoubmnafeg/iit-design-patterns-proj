package app.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

// Design Pattern: Strategy
// Purpose: Logger implementation that appends to a log file.
public class FileLogger implements LoggerStrategy {
    private static final String FILE_PATH = "bpmn-log.txt";

    @Override
    public void log(String action) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.println(LocalDateTime.now() + " | " + action);
        } catch (IOException e) {
            System.err.println("FileLogger error: " + e.getMessage());
        }
    }

    @Override public String getName() { return "File"; }
}
