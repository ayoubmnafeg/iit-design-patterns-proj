package app.model.elements;

import app.model.DiagramElement;
import app.model.ElementType;

public class SequenceFlowElement extends DiagramElement {
    private final String sourceId;
    private final String targetId;
    private double sourceAnchorX, sourceAnchorY;
    private double targetAnchorX, targetAnchorY;

    public SequenceFlowElement(String id, String sourceId, String targetId) {
        super(id, 0, 0, "");
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    @Override public ElementType getType() { return ElementType.SEQUENCE_FLOW; }
    public String getSourceId() { return sourceId; }
    public String getTargetId() { return targetId; }

    public void setSourceAnchor(double x, double y) { sourceAnchorX = x; sourceAnchorY = y; }
    public void setTargetAnchor(double x, double y) { targetAnchorX = x; targetAnchorY = y; }
    public double getSourceAnchorX() { return sourceAnchorX; }
    public double getSourceAnchorY() { return sourceAnchorY; }
    public double getTargetAnchorX() { return targetAnchorX; }
    public double getTargetAnchorY() { return targetAnchorY; }
}
