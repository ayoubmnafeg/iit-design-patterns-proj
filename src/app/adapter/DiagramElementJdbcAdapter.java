package app.adapter;

import app.factory.BPMNElementFamilyFactory;
import app.factory.DefaultBPMNElementFamilyFactory;
import app.factory.FlowElementFactory;
import app.factory.NodeElementFactory;
import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.SequenceFlowElement;

import java.sql.ResultSet;
import java.sql.SQLException;



public class DiagramElementJdbcAdapter {

    private final NodeElementFactory nodeFactory;
    private final FlowElementFactory flowFactory;

    public DiagramElementJdbcAdapter() {
        this(new DefaultBPMNElementFamilyFactory());
    }

    public DiagramElementJdbcAdapter(BPMNElementFamilyFactory family) {
        this(family.nodes(), family.flows());
    }

    public DiagramElementJdbcAdapter(NodeElementFactory nodeFactory, FlowElementFactory flowFactory) {
        this.nodeFactory = nodeFactory;
        this.flowFactory = flowFactory;
    }


    public DiagramElement fromRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        ElementType type = ElementType.valueOf(rs.getString("type"));
        double x = rs.getDouble("x");
        double y = rs.getDouble("y");
        String label = rs.getString("label");
        String sourceId = rs.getString("source_id");
        String targetId = rs.getString("target_id");

        if (type == ElementType.SEQUENCE_FLOW) {
            SequenceFlowElement sf = (SequenceFlowElement) flowFactory.createFlowWithStoredId(
                    id, sourceId, targetId);
            try {
                sf.setSourceAnchor(rs.getDouble("src_anchor_x"), rs.getDouble("src_anchor_y"));
                sf.setTargetAnchor(rs.getDouble("tgt_anchor_x"), rs.getDouble("tgt_anchor_y"));
            } catch (SQLException ignored) {

            }
            return sf;
        }
        return nodeFactory.createElementWithStoredId(type, id, x, y, label);
    }
}
