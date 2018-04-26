package net.masonapps.sketchvr.modeling.ui;

import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.sketchvr.modeling.SketchProjectEntity;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class FreeDrawInput extends ModelingInputProcessor {

    private float drawDistance = 3f;

    public FreeDrawInput(SketchProjectEntity modelingProject) {
        super(modelingProject);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        intersectionInfo.hitPoint.set(ray.direction).scl(drawDistance).add(ray.origin);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    public float getDrawDistance() {
        return drawDistance;
    }

    public void setDrawDistance(float drawDistance) {
        this.drawDistance = drawDistance;
    }
}
