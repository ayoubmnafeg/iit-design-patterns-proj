package app.factory;



public interface BPMNElementFamilyFactory {

    NodeElementFactory nodes();

    FlowElementFactory flows();
}
