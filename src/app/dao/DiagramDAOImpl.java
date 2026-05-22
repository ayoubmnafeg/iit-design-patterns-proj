package app.dao;

import app.adapter.DiagramElementJdbcAdapter;
import app.config.DBConnectionManager;
import app.model.Diagram;
import app.model.DiagramElement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Persistence: handles saving and loading diagrams from SQL database.
public class DiagramDAOImpl implements DiagramDAO {
    private final Connection conn = DBConnectionManager.getInstance().getConnection();
    private final DiagramElementJdbcAdapter elementAdapter = new DiagramElementJdbcAdapter();

    @Override
    public int save(Diagram diagram) {
        try {
            // Insert diagram row
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO diagrams (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, diagram.getName());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int diagramId = rs.getInt(1);
            diagram.setDbId(diagramId);

            // Insert elements
            PreparedStatement eps = conn.prepareStatement(
                "INSERT INTO diagram_elements " +
                "(id, diagram_id, type, x, y, label, source_id, target_id, " +
                "src_anchor_x, src_anchor_y, tgt_anchor_x, tgt_anchor_y) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (DiagramElement el : diagram.getElements()) {
                eps.setString(1, el.getId());
                eps.setInt(2, diagramId);
                eps.setString(3, el.getType().name());
                eps.setDouble(4, el.getX());
                eps.setDouble(5, el.getY());
                eps.setString(6, el.getLabel());
                if (el instanceof app.model.elements.SequenceFlowElement) {
                    app.model.elements.SequenceFlowElement sf = (app.model.elements.SequenceFlowElement) el;
                    eps.setString(7, sf.getSourceId());
                    eps.setString(8, sf.getTargetId());
                    eps.setDouble(9,  sf.getSourceAnchorX());
                    eps.setDouble(10, sf.getSourceAnchorY());
                    eps.setDouble(11, sf.getTargetAnchorX());
                    eps.setDouble(12, sf.getTargetAnchorY());
                } else {
                    eps.setNull(7,  Types.VARCHAR);
                    eps.setNull(8,  Types.VARCHAR);
                    eps.setDouble(9,  0);
                    eps.setDouble(10, 0);
                    eps.setDouble(11, 0);
                    eps.setDouble(12, 0);
                }
                eps.addBatch();
            }
            eps.executeBatch();
            return diagramId;
        } catch (SQLException e) {
            throw new RuntimeException("Save failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Diagram load(int diagramId) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT name FROM diagrams WHERE id = ?");
            ps.setInt(1, diagramId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new RuntimeException("Diagram not found: " + diagramId);
            Diagram diagram = new Diagram(rs.getString("name"));
            diagram.setDbId(diagramId);

            PreparedStatement eps = conn.prepareStatement(
                "SELECT * FROM diagram_elements WHERE diagram_id = ?");
            eps.setInt(1, diagramId);
            ResultSet ers = eps.executeQuery();
            while (ers.next()) {
                diagram.addElement(elementAdapter.fromRow(ers));
            }
            return diagram;
        } catch (SQLException e) {
            throw new RuntimeException("Load failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String[]> listAll() {
        try {
            List<String[]> result = new ArrayList<>();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name FROM diagrams ORDER BY created_at DESC");
            while (rs.next()) {
                result.add(new String[]{rs.getString("id"), rs.getString("name")});
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("listAll failed: " + e.getMessage(), e);
        }
    }
}
