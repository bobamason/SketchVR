package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class SingleNodeSelector extends ModelingInputProcessor {

    private final OnNodeSelectedListener listener;
    @Nullable
    private SketchNode selectedNode = null;

    public SingleNodeSelector(SketchProjectEntity modelingProject, OnNodeSelectedListener listener) {
        super(modelingProject);
        this.listener = listener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver() && intersectionInfo.object instanceof SketchNode) {
            selectedNode = (SketchNode) intersectionInfo.object;
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

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        if (selectedNode != null) {
            shapeRenderer.setColor(Color.LIGHT_GRAY);
            final BoundingBox bb = selectedNode.getAABB();
            shapeRenderer.box(bb.min.x, bb.min.y, bb.min.z, bb.getWidth(), bb.getHeight(), bb.getDepth());
        }
    }

    public interface OnNodeSelectedListener {
        void nodeSelected(SketchNode node);
    }
}
