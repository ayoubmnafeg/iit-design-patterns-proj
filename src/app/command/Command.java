package app.command;

public interface Command {
    void execute();
    void undo();
}
