package app.factory;

// Design Pattern: Abstract Factory
// Purpose: Exposes related factories for node elements vs. flow elements without coupling callers to concrete factory classes.
public interface BPMNElementFamilyFactory {

    NodeElementFactory nodes();

    FlowElementFactory flows();
}
