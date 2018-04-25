package net.masonapps.sketchvr.modeling.transform;

import android.opengl.GLES20;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.sketchvr.modeling.EditableNode;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class ScaleHandle3D extends DragHandle3D {

    private static final float len = 0.675f;
    private final Plane plane = new Plane();
    private float scaleValue = 1f;
    private float lastDst = 0f;
    private Vector3 tmpV = new Vector3();
    private Vector3 normal = new Vector3();
    private boolean shouldSetPlane = true;

    public ScaleHandle3D(ModelBuilder builder, Axis axis) {
        super(createModelInstance(builder, axis), axis);
        setLightingEnabled(false);
        switch (axis) {
            case AXIS_X:
                plane.set(1f, 0f, 0f, 0f);
                break;
            case AXIS_Y:
                plane.set(0f, 1f, 0f, 0f);
                break;
            case AXIS_Z:
                plane.set(0f, 0f, 1f, 0f);
                break;
        }
    }

    private static ModelInstance createModelInstance(ModelBuilder builder, Axis axis) {
        final Color color = new Color();
        final float s = 0.125f;
        final Matrix4 matrix = new Matrix4();
        switch (axis) {
            case AXIS_X:
                color.set(Color.RED);
                break;
            case AXIS_Y:
                color.set(Color.BLUE);
                break;
            case AXIS_Z:
                color.set(Color.GREEN);
                break;
        }
        matrix.scale(s, s, s);

        builder.begin();
        final MeshPartBuilder part = builder.part("t" + axis.name(), GLES20.GL_TRIANGLES, VertexAttributes.Usage.Position, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)));
        BoxShapeBuilder.build(part, matrix);
        return new ModelInstance(builder.end());
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (transformable == null) return false;
        if (!updated) recalculateTransform();
        if (isDragging()) {
            if (shouldSetPlane) {
                normal.set(ray.origin).sub(position);
                setToClosestUnitVector(normal);
                plane.set(position, normal.mul(transformable.getRotation()));
                shouldSetPlane = false;
            }
            if (Intersector.intersectRayPlane(ray, plane, getHitPoint3D())) {
                handleDrag();
                return true;
            }
        }
        return super.intersectsRayBounds(ray, getHitPoint3D());
    }

    private void handleDrag() {
        if (transformable == null) return;
        final float dst = getHitPoint3D().dst(transformable.getPosition());
        final Vector3 s = transformable.getScale();
        float value = dst - lastDst + 1f;
        switch (axis) {
            case AXIS_X:
                s.x = Math.max(s.x * value, 1e-3f);
                scaleValue = s.x;
                break;
            case AXIS_Y:
                s.y = Math.max(s.y * value, 1e-3f);
                scaleValue = s.y;
                break;
            case AXIS_Z:
                s.z = Math.max(s.z * value, 1e-3f);
                scaleValue = s.z;
                break;
        }
        transformable.invalidate();
        lastDst = dst;
    }

    @Override
    public boolean touchDown() {
        if (transformable != null) {
            lastDst = getHitPoint3D().dst(transformable.getPosition());
        }
        return super.touchDown();
    }

    @Override
    public boolean touchUp() {
        shouldSetPlane = true;
        return super.touchUp();

    }

    @Override
    public void setTransformable(@Nullable EditableNode transformable) {
        super.setTransformable(transformable);
        if (transformable != null) {
            final Vector3 s = transformable.getScale();
            switch (axis) {
                case AXIS_X:
                    scaleValue = s.x;
                    break;
                case AXIS_Y:
                    scaleValue = s.y;
                    break;
                case AXIS_Z:
                    scaleValue = s.z;
                    break;
            }
        }
    }

    @Override
    public void update() {
        if (transformable != null) {
            setRotation(transformable.getRotation());
            final Vector3 tmp = Pools.obtain(Vector3.class);
            final Vector3 pos = transformable.getPosition();
            switch (axis) {
                case AXIS_X:
                    tmp.set(len * scaleValue, 0, 0).mul(rotation).add(pos);
                    break;
                case AXIS_Y:
                    tmp.set(0, len * scaleValue, 0).mul(rotation).add(pos);
                    break;
                case AXIS_Z:
                    tmp.set(0, 0, len * scaleValue).mul(rotation).add(pos);
                    break;
            }
            setPosition(tmp);
            Pools.free(tmp);
        }
    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        if (transformable != null) {
            final Vector3 tmp = Pools.obtain(Vector3.class);
            final Vector3 pos = transformable.getPosition();
            switch (axis) {
                case AXIS_X:
                    renderer.setColor(Color.RED);
                    renderer.line(pos, tmp.set(len * scaleValue, 0, 0).mul(rotation).add(pos));
                    break;
                case AXIS_Y:
                    renderer.setColor(Color.BLUE);
                    renderer.line(pos, tmp.set(0, len * scaleValue, 0).mul(rotation).add(pos));
                    break;
                case AXIS_Z:
                    renderer.setColor(Color.GREEN);
                    renderer.line(pos, tmp.set(0, 0, len * scaleValue).mul(rotation).add(pos));
                    break;
            }
            Pools.free(tmp);
        }

//        switch (axis) {
//            case AXIS_X:
//                renderer.setColor(Color.RED);
//                break;
//            case AXIS_Y:
//                renderer.setColor(Color.BLUE);
//                break;
//            case AXIS_Z:
//                renderer.setColor(Color.GREEN);
//                break;
//        }
//        PlaneUtils.debugDraw(renderer, plane);
    }
}
