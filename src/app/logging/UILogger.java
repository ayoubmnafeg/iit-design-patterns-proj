package app.logging;



public class UILogger implements LoggerStrategy {
    @Override
    public void log(String action) {

    }

    @Override
    public String getName() {
        return "UI";
    }
}
