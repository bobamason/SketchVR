package net.masonapps.sketchvr.actions;

import net.masonapps.sketchvr.modeling.EditableNode;
import net.masonapps.sketchvr.modeling.ModelingProjectEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class RemoveAction extends Action {

    private final ModelingProjectEntity project;

    public RemoveAction(EditableNode entity, ModelingProjectEntity project) {
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
    }
}
