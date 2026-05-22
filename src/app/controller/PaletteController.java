package app.controller;

import app.model.ElementType;

public class PaletteController {
    public enum Tool { SELECT, HAND, DELETE, NONE }

    private ElementType selectedType = null;
    private boolean deleteMode = false;
    private boolean flowMode = false;
    private Tool activeTool = Tool.SELECT;

    public void selectTool(ElementType type) {
        if (type == ElementType.SEQUENCE_FLOW) {
            flowMode = true;
            deleteMode = false;
            selectedType = null;
            activeTool = Tool.NONE;
        } else {
            selectedType = type;
            flowMode = false;
            deleteMode = false;
            activeTool = Tool.NONE;
        }
    }

    public void setSelectMode() {
        activeTool = Tool.SELECT;
        selectedType = null;
        flowMode = false;
        deleteMode = false;
    }

    public void setHandMode() {
        activeTool = Tool.HAND;
        selectedType = null;
        flowMode = false;
        deleteMode = false;
    }

    public void toggleDeleteMode() {
        deleteMode = !deleteMode;
        if (deleteMode) {
            selectedType = null;
            flowMode = false;
            activeTool = Tool.DELETE;
        }
    }

    public void setDeleteMode() {
        deleteMode = true;
        selectedType = null;
        flowMode = false;
        activeTool = Tool.DELETE;
    }

    public void clearAll() {
        selectedType = null;
        deleteMode = false;
        flowMode = false;
        activeTool = Tool.SELECT;
    }

    public ElementType getSelectedType() { return selectedType; }
    public boolean isDeleteMode()        { return deleteMode; }
    public boolean isFlowMode()          { return flowMode; }
    public boolean isSelectMode()        { return activeTool == Tool.SELECT; }
    public boolean isHandMode()          { return activeTool == Tool.HAND; }
    public boolean isDeleteModeActive()  { return activeTool == Tool.DELETE; }
}
