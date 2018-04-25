package net.masonapps.sketchvr.actions;

import net.masonapps.sketchvr.modeling.EditableNode;
import net.masonapps.sketchvr.modeling.ModelingProjectEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class AddAction extends Action {

    private final ModelingProjectEntity project;

    public AddAction(EditableNode entity, ModelingProjectEntity project) {
        super(entity);
        this.project = project;
    }

    @Override
    public void redoAction() {
        project.add(getNode());
    }

    @Override
    public void undoAction() {
        project.remove(getNode());
    }
}
