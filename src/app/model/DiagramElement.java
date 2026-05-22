package app.model;

public abstract class DiagramElement {
    protected String id;
    protected double x;
    protected double y;
    protected String label;

    public DiagramElement(String id, double x, double y, String label) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public abstract ElementType getType();

    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public String getLabel() { return label; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setLabel(String label) { this.label = label; }
}
