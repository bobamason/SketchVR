package net.masonapps.sketchvr.modeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.sketchvr.modeling.SketchProjectEntity;
import net.masonapps.sketchvr.ui.RenderableInput;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.TouchPadGestureDetector;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class AddPlaneInput extends ModelingInputProcessor implements RenderableInput {

    private final OnPlaneAddedListener listener;
    private final Entity planeEntity;
    private final Plane plane = new Plane();
    private final TouchPadGestureDetector gestureDetector;
    private float distance = 3f;
    private final Vector3 tmp = new Vector3();

    public AddPlaneInput(SketchProjectEntity modelingProject, OnPlaneAddedListener listener) {
        super(modelingProject);
        this.listener = listener;
        planeEntity = new Entity(new ModelInstance(createPlaneModel(new ModelBuilder(), 5f)));
        gestureDetector = new TouchPadGestureDetector(new TouchPadGestureDetector.TouchPadGestureAdapter() {
            @Override
            public void pan(float x, float y, float deltaX, float deltaY) {
                distance = MathUtils.clamp(distance + deltaY * 2f, 1.5f, 10f);
            }
        });
    }

    private static Model createPlaneModel(ModelBuilder builder, float radius) {
        final Material material = new Material(ColorAttribute.createDiffuse(Color.GOLDENROD), IntAttribute.createCullFace(0), new BlendingAttribute(true, 0.25f));
        return builder.createRect(
                -radius, -radius, 0f,
                radius, -radius, 0f,
                radius, radius, 0f,
                -radius, radius, 0f,
                0f, 1f, 0f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
    }

    @Override
    public void update() {

    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void render(ModelBatch modelBatch) {
        modelBatch.render(planeEntity.modelInstance);
    }

    @Override
    public boolean performRayTest(Ray ray) {
//        final boolean rayTest = super.performRayTest(ray);
//        if (rayTest) {
//            planeEntity.getPosition().set(intersectionInfo.hitPoint);
////                previewNode.getRotation().setFromCross(Vector3.Y, normal);
//            planeEntity.invalidate();
//        } else {
        planeEntity.getPosition().set(ray.direction).add(ray.origin).nor().scl(distance);
        planeEntity.lookAt(Vector3.Zero, Vector3.Y);
            planeEntity.invalidate();
        plane.set(planeEntity.getPosition(), tmp.set(planeEntity.getPosition()).scl(-1).nor());
//        }
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
                case DaydreamButtonEvent.ACTION_UP:
                    listener.planeAdded(plane);
                    break;
            }
        }
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        gestureDetector.onControllerTouchPadEvent(event);
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
