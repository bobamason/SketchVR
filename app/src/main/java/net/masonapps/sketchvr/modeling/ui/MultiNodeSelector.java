package net.masonapps.sketchvr.modeling.ui;

import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class MultiNodeSelector extends ModelingInputProcessor {

    private final OnSelectionChangedListener listener;
    private List<SketchNode> selectedNodes = new ArrayList<>();

    public MultiNodeSelector(SketchProjectEntity modelingProject, OnSelectionChangedListener listener) {
        super(modelingProject);
        this.listener = listener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver() && intersectionInfo.object instanceof SketchNode) {
            final SketchNode node = (SketchNode) intersectionInfo.object;
            if (selectedNodes.contains(node))
                selectedNodes.remove(node);
            else
                selectedNodes.add(node);
            listener.selectionChanged(selectedNodes);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return !selectedNodes.isEmpty();
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return !selectedNodes.isEmpty();
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return !selectedNodes.isEmpty();
    }

    public interface OnSelectionChangedListener {
        void selectionChanged(List<SketchNode> nodes);
    }
}
