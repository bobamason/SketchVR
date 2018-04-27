package net.masonapps.sketchvr.actions;

import net.masonapps.sketchvr.modeling.SketchNode;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public abstract class Action {

    private final SketchNode node;

    public Action(SketchNode node) {
        this.node = node;
    }

    public abstract void redoAction();

    public abstract void undoAction();

    public SketchNode getNode() {
        return node;
    }
}
