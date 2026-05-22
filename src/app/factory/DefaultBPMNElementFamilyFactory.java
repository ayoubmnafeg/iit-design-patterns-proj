package app.factory;



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
