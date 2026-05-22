package app.command;

import java.util.Stack;

public class CommandManager {
    private final Stack<Command> history = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
    }

    public void undo() {
        if (!history.isEmpty()) {
            history.pop().undo();
        }
    }

    public boolean canUndo() { return !history.isEmpty(); }
}
