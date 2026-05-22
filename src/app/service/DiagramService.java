package app.service;

import app.command.AddElementCommand;
import app.command.CommandManager;
import app.command.RemoveElementCommand;
import app.dao.DiagramDAO;
import app.dao.DiagramDAOImpl;
import app.factory.BPMNElementFactory;
import app.factory.BPMNElementFamilyFactory;
import app.factory.DefaultBPMNElementFamilyFactory;
import app.model.Diagram;
import app.model.DiagramElement;
import app.model.ElementType;
import app.model.elements.SequenceFlowElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiagramService {
    private final Diagram diagram;
    private final BPMNElementFactory factory = new BPMNElementFactory();
    private final BPMNElementFamilyFactory importFactory = new DefaultBPMNElementFamilyFactory();
    private final CommandManager commandManager = new CommandManager();
    private final LoggingService loggingService;
    private boolean dirty = true;

    public DiagramService(Diagram diagram, LoggingService loggingService) {
        this.diagram = diagram;
        this.loggingService = loggingService;
    }

    public DiagramElement addElement(ElementType type, double x, double y) {
        DiagramElement element = factory.createElement(type, x, y);
        commandManager.executeCommand(new AddElementCommand(diagram, element));
        dirty = true;
        autoSaveIfSaved();
        loggingService.log("Added " + type + " at (" + (int)x + "," + (int)y + ")");
        return element;
    }

    public void addFlow(String sourceId, String targetId,
                        double srcAx, double srcAy, double tgtAx, double tgtAy) {
        boolean exists = diagram.getElements().stream()
            .filter(e -> e.getType() == app.model.ElementType.SEQUENCE_FLOW)
            .map(e -> (app.model.elements.SequenceFlowElement) e)
            .anyMatch(sf -> sf.getSourceId().equals(sourceId) && sf.getTargetId().equals(targetId));
        if (exists) return;

        app.model.elements.SequenceFlowElement flow =
            (app.model.elements.SequenceFlowElement) factory.createFlow(sourceId, targetId);
        flow.setSourceAnchor(srcAx, srcAy);
        flow.setTargetAnchor(tgtAx, tgtAy);
        commandManager.executeCommand(new AddElementCommand(diagram, flow));
        dirty = true;
        autoSaveIfSaved();
        loggingService.log("Added SEQUENCE_FLOW from " + sourceId + " to " + targetId);
    }

    public void removeElement(String id) {
        diagram.getElementById(id).ifPresent(el -> {
            List<DiagramElement> elementsToRemove = new java.util.ArrayList<>();
            elementsToRemove.add(el);
            if (el.getType() != ElementType.SEQUENCE_FLOW) {
                diagram.getElements().stream()
                    .filter(candidate -> candidate.getType() == ElementType.SEQUENCE_FLOW)
                    .map(candidate -> (SequenceFlowElement) candidate)
                    .filter(flow -> flow.getSourceId().equals(id) || flow.getTargetId().equals(id))
                    .forEach(elementsToRemove::add);
            }
            commandManager.executeCommand(new RemoveElementCommand(diagram, elementsToRemove));
            dirty = true;
            autoSaveIfSaved();
            loggingService.log("Removed " + el.getType() + " id=" + id);
        });
    }

    public void moveElement(String id, double x, double y) {
        diagram.getElementById(id).ifPresent(el -> {
            if (el.getX() == x && el.getY() == y) return;
            double dx = x - el.getX();
            double dy = y - el.getY();
            el.setX(x);
            el.setY(y);
            moveConnectedFlowAnchors(id, dx, dy);
            dirty = true;
        });
    }

    private void moveConnectedFlowAnchors(String elementId, double dx, double dy) {
        if (dx == 0 && dy == 0) return;

        diagram.getElements().stream()
            .filter(e -> e.getType() == ElementType.SEQUENCE_FLOW)
            .map(e -> (SequenceFlowElement) e)
            .forEach(flow -> {
                if (flow.getSourceId().equals(elementId)) {
                    flow.setSourceAnchor(
                        flow.getSourceAnchorX() + dx,
                        flow.getSourceAnchorY() + dy
                    );
                }
                if (flow.getTargetId().equals(elementId)) {
                    flow.setTargetAnchor(
                        flow.getTargetAnchorX() + dx,
                        flow.getTargetAnchorY() + dy
                    );
                }
            });
    }

    public void updateElementLabel(String id, String label) {
        diagram.getElementById(id).ifPresent(el -> {
            if (java.util.Objects.equals(el.getLabel(), label)) return;
            el.setLabel(label);
            dirty = true;
            autoSaveIfSaved();
            loggingService.log("Updated " + el.getType() + " label id=" + id);
        });
    }

    public void undo() {
        commandManager.undo();
        dirty = true;
        autoSaveIfSaved();
        loggingService.log("Undo");
    }

    public void redo() {
        commandManager.redo();
        dirty = true;
        autoSaveIfSaved();
        loggingService.log("Redo");
    }

    public void saveDiagram() {
        createDiagramDAO().save(diagram);
        dirty = false;
        loggingService.log("Saved diagram: " + diagram.getName());
    }

    public void saveDiagram(String name) {
        diagram.setName(name);
        saveDiagram();
    }

    public void autoSaveIfSaved() {
        if (!isSavedInDatabase() || !dirty) {
            return;
        }
        createDiagramDAO().save(diagram);
        dirty = false;
        loggingService.log("Auto-saved diagram: " + diagram.getName());
    }

    public void newDiagram() {
        diagram.getElements().clear();
        diagram.setName("Untitled");
        diagram.setDbId(-1);
        commandManager.clear();
        dirty = true;
        loggingService.log("Started new diagram");
    }

    public void loadDiagram(int diagramId) {
        Diagram loaded = createDiagramDAO().load(diagramId);
        diagram.getElements().setAll(loaded.getElements());
        diagram.setName(loaded.getName());
        diagram.setDbId(loaded.getDbId());
        commandManager.clear();
        dirty = false;
        loggingService.log("Loaded diagram id=" + diagramId);
    }

    public List<String[]> listSavedDiagrams() {
        return createDiagramDAO().listAll();
    }

    public void exportJson(Path path) {
        try {
            Files.writeString(path, toJson(), StandardCharsets.UTF_8);
            loggingService.log("Exported diagram JSON: " + path.getFileName());
        } catch (IOException e) {
            throw new RuntimeException("Export JSON failed: " + e.getMessage(), e);
        }
    }

    public void importJson(Path path) {
        try {
            Object parsed = new JsonParser(Files.readString(path, StandardCharsets.UTF_8)).parse();
            if (!(parsed instanceof Map)) {
                throw new IllegalArgumentException("Project JSON must contain an object.");
            }
            Map<?, ?> project = (Map<?, ?>) parsed;
            Object elementsValue = project.get("elements");
            if (!(elementsValue instanceof List)) {
                throw new IllegalArgumentException("Project JSON must contain an elements array.");
            }

            List<DiagramElement> importedElements = new ArrayList<>();
            for (Object value : (List<?>) elementsValue) {
                if (!(value instanceof Map)) {
                    throw new IllegalArgumentException("Each element must be an object.");
                }
                importedElements.add(toElement((Map<?, ?>) value));
            }

            diagram.getElements().setAll(importedElements);
            diagram.setName(readString(project, "name", "Imported Project"));
            diagram.setDbId(-1);
            commandManager.clear();
            dirty = true;
            loggingService.log("Imported diagram JSON: " + path.getFileName());
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Import JSON failed: " + e.getMessage(), e);
        }
    }

    public Diagram getDiagram() { return diagram; }
    public boolean canUndo() { return commandManager.canUndo(); }
    public boolean canRedo() { return commandManager.canRedo(); }
    public boolean hasUnsavedChanges() { return dirty; }
    public boolean isSavedInDatabase() { return diagram.getDbId() > 0; }

    private DiagramDAO createDiagramDAO() {
        return new DiagramDAOImpl();
    }

    private DiagramElement toElement(Map<?, ?> data) {
        String id = requireString(data, "id");
        ElementType type = ElementType.valueOf(requireString(data, "type"));
        if (type == ElementType.SEQUENCE_FLOW) {
            SequenceFlowElement flow = (SequenceFlowElement) importFactory.flows().createFlowWithStoredId(
                id,
                requireString(data, "sourceId"),
                requireString(data, "targetId")
            );
            flow.setLabel(readString(data, "label", ""));
            flow.setSourceAnchor(readDouble(data, "sourceAnchorX", 0), readDouble(data, "sourceAnchorY", 0));
            flow.setTargetAnchor(readDouble(data, "targetAnchorX", 0), readDouble(data, "targetAnchorY", 0));
            return flow;
        }
        return importFactory.nodes().createElementWithStoredId(
            type,
            id,
            readDouble(data, "x", 0),
            readDouble(data, "y", 0),
            readString(data, "label", "")
        );
    }

    private String requireString(Map<?, ?> data, String key) {
        Object value = data.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Missing text field: " + key);
        }
        return (String) value;
    }

    private String readString(Map<?, ?> data, String key, String fallback) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : fallback;
    }

    private double readDouble(Map<?, ?> data, String key, double fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    private String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"name\": \"").append(escapeJson(diagram.getName())).append("\",\n");
        json.append("  \"dbId\": ").append(diagram.getDbId()).append(",\n");
        json.append("  \"elements\": [\n");
        for (int i = 0; i < diagram.getElements().size(); i++) {
            DiagramElement element = diagram.getElements().get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(escapeJson(element.getId())).append("\",\n");
            json.append("      \"type\": \"").append(element.getType().name()).append("\",\n");
            json.append("      \"x\": ").append(element.getX()).append(",\n");
            json.append("      \"y\": ").append(element.getY()).append(",\n");
            json.append("      \"label\": \"").append(escapeJson(element.getLabel())).append("\"");
            if (element instanceof SequenceFlowElement) {
                SequenceFlowElement flow = (SequenceFlowElement) element;
                json.append(",\n");
                json.append("      \"sourceId\": \"").append(escapeJson(flow.getSourceId())).append("\",\n");
                json.append("      \"targetId\": \"").append(escapeJson(flow.getTargetId())).append("\",\n");
                json.append("      \"sourceAnchorX\": ").append(flow.getSourceAnchorX()).append(",\n");
                json.append("      \"sourceAnchorY\": ").append(flow.getSourceAnchorY()).append(",\n");
                json.append("      \"targetAnchorX\": ").append(flow.getTargetAnchorX()).append(",\n");
                json.append("      \"targetAnchorY\": ").append(flow.getTargetAnchorY()).append("\n");
            } else {
                json.append("\n");
            }
            json.append("    }");
            if (i < diagram.getElements().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static class JsonParser {
        private final String text;
        private int index;

        JsonParser(String text) {
            this.text = text;
        }

        Object parse() {
            Object value = parseValue();
            skipWhitespace();
            if (index != text.length()) {
                throw new IllegalArgumentException("Unexpected JSON content.");
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON.");
            }
            char ch = text.charAt(index);
            if (ch == '{') return parseObject();
            if (ch == '[') return parseArray();
            if (ch == '"') return parseString();
            if (ch == '-' || Character.isDigit(ch)) return parseNumber();
            if (text.startsWith("null", index)) {
                index += 4;
                return null;
            }
            throw new IllegalArgumentException("Unexpected JSON value.");
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> object = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (peek('}')) {
                index++;
                return object;
            }
            while (true) {
                String key = parseString();
                skipWhitespace();
                expect(':');
                object.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> array = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (peek(']')) {
                index++;
                return array;
            }
            while (true) {
                array.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    return array;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '"') {
                    return result.toString();
                }
                if (ch == '\\') {
                    if (index >= text.length()) {
                        throw new IllegalArgumentException("Invalid JSON escape.");
                    }
                    char escaped = text.charAt(index++);
                    switch (escaped) {
                        case '"': result.append('"'); break;
                        case '\\': result.append('\\'); break;
                        case '/': result.append('/'); break;
                        case 'b': result.append('\b'); break;
                        case 'f': result.append('\f'); break;
                        case 'n': result.append('\n'); break;
                        case 'r': result.append('\r'); break;
                        case 't': result.append('\t'); break;
                        default: throw new IllegalArgumentException("Unsupported JSON escape.");
                    }
                } else {
                    result.append(ch);
                }
            }
            throw new IllegalArgumentException("Unclosed JSON string.");
        }

        private Number parseNumber() {
            int start = index;
            if (peek('-')) index++;
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            if (peek('.')) {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                index++;
                if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            return Double.valueOf(text.substring(start, index));
        }

        private void expect(char expected) {
            skipWhitespace();
            if (index >= text.length() || text.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "'.");
            }
            index++;
        }

        private boolean peek(char expected) {
            return index < text.length() && text.charAt(index) == expected;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }
    }
}
