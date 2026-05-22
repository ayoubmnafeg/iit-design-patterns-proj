package app.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;



public class Diagram {
    private String name;
    private int dbId = -1;
    private final ObservableList<DiagramElement> elements = FXCollections.observableArrayList();

    public Diagram(String name) { this.name = name; }

    public void addElement(DiagramElement element) { elements.add(element); }

    public void removeElement(String id) {
        elements.removeIf(e -> e.getId().equals(id));
    }

    public Optional<DiagramElement> getElementById(String id) {
        return elements.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public ObservableList<DiagramElement> getElements() { return elements; }
    public String getName() { return name; }
    public int getDbId() { return dbId; }
    public void setDbId(int dbId) { this.dbId = dbId; }
    public void setName(String name) { this.name = name; }
}
