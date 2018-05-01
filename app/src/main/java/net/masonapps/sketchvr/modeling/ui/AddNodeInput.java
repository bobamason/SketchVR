package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class AddNodeInput extends ModelingInputProcessor implements DaydreamControllerInputListener {

    private final OnNodeAddedListener listener;
    private final ModelInstance modelInstance;
    private final Material material;
    @Nullable
    private SketchNode previewNode = null;
    private float distance = 3f;

    public AddNodeInput(SketchProjectEntity modelingProject, OnNodeAddedListener listener) {
        super(modelingProject);
        this.listener = listener;
        modelInstance = new ModelInstance(new Model());
        material = new Material(ColorAttribute.createDiffuse(Color.CYAN), new BlendingAttribute(true, 0.5f));
    }

    public void render(ModelBatch modelBatch) {
        if (isVisible() && previewNode != null)
            modelBatch.render(modelInstance);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        final boolean rayTest = super.performRayTest(ray);
        if (previewNode != null) {
//            Logger.d("hit = " + String.valueOf(rayTest) + " normal = " + intersectionInfo.normal);
            if (rayTest) {
                if (intersectionInfo.normal.isZero(0.001f))
                    intersectionInfo.normal.set(Vector3.Y);
                final float offset = previewNode.getBounds().getHeight() * previewNode.getScaleY() / 2f;
                previewNode.getPosition().set(intersectionInfo.normal).scl(offset).add(intersectionInfo.hitPoint);
                previewNode.getRotation().setFromCross(Vector3.Y, intersectionInfo.normal);
                previewNode.invalidate();
            } else {
                previewNode.getPosition().set(ray.direction).scl(distance).add(ray.origin);
                previewNode.getRotation().idt();
                previewNode.invalidate();
            }
        }
        return rayTest;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return isVisible() && previewNode != null;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return isVisible() && previewNode != null;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return isVisible() && previewNode != null;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return isVisible() && previewNode != null;
    }

    public void setPreviewNode(@Nullable SketchNode previewNode) {
        this.previewNode = previewNode;
        modelInstance.nodes.clear();
        modelInstance.model.nodes.clear();

        modelInstance.model.meshParts.clear();
        modelInstance.model.meshes.clear();

        modelInstance.materials.clear();
        modelInstance.model.materials.clear();

        if (previewNode != null) {
            modelInstance.nodes.add(previewNode);
            modelInstance.model.nodes.add(previewNode);

            final NodePart nodePart = previewNode.parts.get(0);

            modelInstance.model.meshParts.add(nodePart.meshPart);
            modelInstance.model.meshes.add(nodePart.meshPart.mesh);

            modelInstance.materials.add(material);
            modelInstance.model.materials.add(material);
        }
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
                    if (previewNode != null) {
                        final SketchNode copy = previewNode.copy();
                        previewNode.getPosition().mul(project.getInverseTransform());
                        previewNode.getRotation().mul(new Quaternion(project.getRotation()).conjugate());
                        previewNode.invalidate();
                        project.add(copy);
                        project.insertIntoAABBTree(copy);
                        listener.nodeAdded(copy);
                    }
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

    public interface OnNodeAddedListener {
        void nodeAdded(SketchNode node);
    }
}
