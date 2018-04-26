package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import net.masonapps.sketchvr.modeling.EditableNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class SingleNodeSelector extends ModelingInputProcessor {

    private final OnNodeSelectedListener listener;
    @Nullable
    private EditableNode selectedNode = null;

    public SingleNodeSelector(SketchProjectEntity modelingProject, OnNodeSelectedListener listener) {
        super(modelingProject);
        this.listener = listener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver() && intersectionInfo.object instanceof EditableNode) {
            selectedNode = (EditableNode) intersectionInfo.object;
            listener.nodeSelected(selectedNode);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return selectedNode != null;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return selectedNode != null;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return selectedNode != null;
    }

    public interface OnNodeSelectedListener {
        void nodeSelected(EditableNode node);
    }
}
