package net.masonapps.sketchvr.actions;

import net.masonapps.sketchvr.modeling.EditableNode;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public abstract class Action {

    private final EditableNode node;

    public Action(EditableNode node) {
        this.node = node;
    }

    public abstract void redoAction();

    public abstract void undoAction();

    public EditableNode getNode() {
        return node;
    }
}
