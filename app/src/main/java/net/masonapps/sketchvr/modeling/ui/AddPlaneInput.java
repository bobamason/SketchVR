package net.masonapps.sketchvr.modeling.ui;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.collision.Ray;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class AddPlaneInput extends ModelingInputProcessor implements RenderableInput {

    private final OnPlaneAddedListener listener;
    private final Entity planeEntity;
    private float distance = 3f;

    public AddPlaneInput(SketchProjectEntity modelingProject, OnPlaneAddedListener listener) {
        super(modelingProject);
        this.listener = listener;
        planeEntity = new Entity(new ModelInstance(new Model()));
    }

    @Override
    public void update() {

    }

    @Override
    public void render(ModelBatch modelBatch) {
        modelBatch.render(planeEntity.modelInstance);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        final boolean rayTest = super.performRayTest(ray);
        if (rayTest) {
            planeEntity.getPosition().set(intersectionInfo.hitPoint);
//                previewNode.getRotation().setFromCross(Vector3.Y, normal);
            planeEntity.invalidate();
        } else {
            planeEntity.getPosition().set(ray.direction).scl(distance).add(ray.origin);
            planeEntity.getRotation().idt();
            planeEntity.invalidate();
        }
        return true;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {

    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return isVisible();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return isVisible();
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return isVisible();
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {

    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
        if (isVisible() && event.button == DaydreamButtonEvent.BUTTON_TOUCHPAD) {
            switch (event.action) {
                case DaydreamButtonEvent.ACTION_DOWN:
                    listener.planeAdded(new Plane());
                    break;
            }
        }
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {

    }

    @Override
    public boolean onBackButtonClicked() {
        return false;
    }

    public interface OnPlaneAddedListener {
        void planeAdded(Plane plane);
    }
}
