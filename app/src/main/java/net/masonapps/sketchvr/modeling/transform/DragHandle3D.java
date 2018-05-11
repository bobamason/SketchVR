package net.masonapps.sketchvr.modeling.transform;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.sketchvr.modeling.SketchNode;

import org.masonapps.libgdxgooglevr.gfx.Entity;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public abstract class DragHandle3D extends Entity {

    protected final Axis axis;
    private final Vector3 hitPoint = new Vector3();
    protected boolean dragging = false;
    @Nullable
    protected SketchNode sketchNode = null;
    private boolean isCursorOver = false;
    private Matrix4 parentTransform = new Matrix4();

    public DragHandle3D(@Nullable ModelInstance modelInstance, BoundingBox bounds, Axis axis) {
        super(modelInstance, bounds);
        this.axis = axis;
    }

    public DragHandle3D(@Nullable ModelInstance modelInstance, Axis axis) {
        super(modelInstance);
        this.axis = axis;
    }

    @Override
    public boolean intersectsRayBoundsFast(Ray ray) {
        isCursorOver = super.intersectsRayBounds(ray, hitPoint);
        return isCursorOver;
    }

    public abstract void update();

    public void render(ModelBatch batch) {
        if (!updated) recalculateTransform();
        if (modelInstance != null)
            batch.render(modelInstance);
    }

    public abstract void drawShapes(ShapeRenderer renderer);

    public boolean performRayTest(Ray ray) {
        return intersectsRayBoundsFast(ray);
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        if (modelInstance != null)
            getTransform(modelInstance.transform).mulLeft(parentTransform);
    }

    @Override
    public Entity setTransform(Matrix4 transform) {
        super.setTransform(transform);
        if (modelInstance != null && parentTransform != null)
            getTransform(modelInstance.transform).mulLeft(parentTransform);
        return this;
    }

    public Matrix4 getParentTransform() {
        return parentTransform;
    }

    public void setParentTransform(Matrix4 parentTransform) {
        this.parentTransform.set(parentTransform);
        if (modelInstance != null)
            getTransform(modelInstance.transform).mulLeft(parentTransform);
    }

    protected void setToClosestUnitVector(Vector3 v) {
        switch (axis) {
            case AXIS_X:
                if (Math.abs(v.y) > Math.abs(v.z)) {
                    v.x = 0f;
                    v.y = Math.signum(v.y);
                    v.z = 0f;
                } else {
                    v.x = 0f;
                    v.y = 0f;
                    v.z = Math.signum(v.z);
                }
                break;
            case AXIS_Y:
                if (Math.abs(v.x) > Math.abs(v.z)) {
                    v.x = Math.signum(v.x);
                    v.y = 0f;
                    v.z = 0f;
                } else {
                    v.x = 0f;
                    v.y = 0f;
                    v.z = Math.signum(v.z);
                }
                break;
            case AXIS_Z:
                if (Math.abs(v.x) > Math.abs(v.y)) {
                    v.x = Math.signum(v.x);
                    v.y = 0f;
                    v.z = 0f;
                } else {
                    v.x = 0f;
                    v.y = Math.signum(v.y);
                    v.z = 0f;
                }
                break;
        }
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isCursorOver() {
        return isCursorOver;
    }

    @NonNull
    public Vector3 getHitPoint3D() {
        return hitPoint;
    }

    public boolean touchDown() {
        if (sketchNode == null) return false;
        dragging = true;
        return true;
    }

    public boolean touchUp() {
        dragging = false;
        return sketchNode != null;
    }

    public void setSketchNode(@Nullable SketchNode sketchNode) {
        this.sketchNode = sketchNode;
    }

    public enum Axis {
        AXIS_X, AXIS_Y, AXIS_Z
    }
}
