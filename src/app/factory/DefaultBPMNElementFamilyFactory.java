package app.factory;

// Design Pattern: Abstract Factory
// Purpose: Produces the concrete node and flow factories used by the BPMN modeler.
public class DefaultBPMNElementFamilyFactory implements BPMNElementFamilyFactory {

    @Override
    public NodeElementFactory nodes() {
        return new BPMNNodeElementFactory();
    }

    @Override
    public FlowElementFactory flows() {
        return new BPMNFlowElementFactory();
    }
}
