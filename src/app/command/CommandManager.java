package app.command;

import java.util.Stack;

public class CommandManager {
    private final Stack<Command> history = new Stack<>();
    private final Stack<Command> redoHistory = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        redoHistory.clear();
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            command.undo();
            redoHistory.push(command);
        }
    }

    public void redo() {
        if (!redoHistory.isEmpty()) {
            Command command = redoHistory.pop();
            command.execute();
            history.push(command);
        }
    }

    public boolean canUndo() { return !history.isEmpty(); }
    public boolean canRedo() { return !redoHistory.isEmpty(); }


    public void clear() {
        history.clear();
        redoHistory.clear();
    }
}
