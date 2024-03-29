package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.sketchvr.modeling.SketchProjectEntity;
import net.masonapps.sketchvr.ui.BackButtonListener;
import net.masonapps.sketchvr.ui.ShapeRenderableInput;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public abstract class ModelingInputProcessor implements VrInputProcessor, DaydreamControllerInputListener, ShapeRenderableInput, BackButtonListener {

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
    public boolean mouseMoved(int screenX, int screenY) {
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

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {

    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {

    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {

    }

    @Override
    public boolean onBackButtonClicked() {
        return false;
    }
}
