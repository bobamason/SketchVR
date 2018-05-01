package net.masonapps.sketchvr.actions;

import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class RemoveAction extends Action {

    private final SketchProjectEntity project;

    public RemoveAction(SketchNode entity, SketchProjectEntity project) {
        super(entity);
        this.project = project;
    }

    @Override
    public void redoAction() {
        project.remove(getNode());
    }

    @Override
    public void undoAction() {
        project.add(getNode());
        project.insertIntoAABBTree(getNode());
    }
}
