package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public abstract class ModelingInputProcessor implements VrInputProcessor {

    protected final SketchProjectEntity project;
    protected final AABBTree.IntersectionInfo intersectionInfo = new AABBTree.IntersectionInfo();
    protected boolean isCursorOver = false;
    protected boolean visible = true;

    public ModelingInputProcessor(SketchProjectEntity project) {
        this.project = project;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        isCursorOver = visible && project.rayTest(ray, intersectionInfo);
        return isCursorOver;
    }

    public abstract void draw(ShapeRenderer shapeRenderer);

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    @Nullable
    @Override
    public Vector2 getHitPoint2D() {
        return null;
    }

    @Nullable
    @Override
    public Vector3 getHitPoint3D() {
        return intersectionInfo.hitPoint;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean onBackButtonClicked() {
        return false;
    }
}
