package app.dao;

import app.model.Diagram;
import java.util.List;

public interface DiagramDAO {
    int save(Diagram diagram);
    Diagram load(int diagramId);
    List<String[]> listAll();   // returns [{id, name}, ...]
}
