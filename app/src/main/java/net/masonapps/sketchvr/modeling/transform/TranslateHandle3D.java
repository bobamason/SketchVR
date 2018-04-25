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
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.sketchvr.math.SnapUtil;
import net.masonapps.sketchvr.modeling.EditableNode;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class TranslateHandle3D extends DragHandle3D {

    private final Plane plane = new Plane();
    private Vector3 normal = new Vector3();
    private Vector3 startHitPoint = new Vector3();
    private Vector3 startPosition = new Vector3();
    private boolean shouldSetPlane = true;

    public TranslateHandle3D(ModelBuilder builder, Axis axis) {
        super(createModelInstance(builder, axis), createBounds(axis), axis);
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
        final Vector3 from = new Vector3();
        final Vector3 to = new Vector3();
        final float len = 0.8f;
        switch (axis) {
            case AXIS_X:
                from.set(len * 0.5f, 0, 0);
                to.set(len, 0, 0);
                color.set(Color.RED);
                break;
            case AXIS_Y:
                from.set(0, len * 0.5f, 0);
                to.set(0, len, 0);
                color.set(Color.BLUE);
                break;
            case AXIS_Z:
                from.set(0, 0, len * 0.5f);
                to.set(0, 0, len);
                color.set(Color.GREEN);
                break;
        }
        builder.begin();
        final MeshPartBuilder part = builder.part("t" + axis.name(), GLES20.GL_TRIANGLES, VertexAttributes.Usage.Position, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)));
        ArrowShapeBuilder.build(part,
                from.x, from.y, from.z,
                to.x, to.y, to.z,
                Vector3.dst(from.x, from.y, from.z, to.x, to.y, to.z) * 0.45f,
                0.35f, 8);
        return new ModelInstance(builder.end());
    }

    private static BoundingBox createBounds(Axis axis) {
        final float margin = 0.125f;
        final float len = 0.8f;
        final BoundingBox bb = new BoundingBox();
        switch (axis) {
            case AXIS_X:
                bb.min.set(len * 0.5f - margin, -margin, -margin);
                bb.max.set(len + margin, margin, margin);
                break;
            case AXIS_Y:
                bb.min.set(-margin, len * 0.5f - margin, -margin);
                bb.max.set(margin, len + margin, margin);
                break;
            case AXIS_Z:
                bb.min.set(-margin, -margin, len * 0.5f - margin);
                bb.max.set(margin, margin, len + margin);
                break;
        }
        return bb;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (transformable == null) return false;
        if (!updated) recalculateTransform();
        if (isDragging()) {
            if (shouldSetPlane) {
                normal.set(ray.origin).sub(position);
                setToClosestUnitVector(normal);
                plane.set(position, normal);
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
        final Vector3 hitPoint = getHitPoint3D();
        final Vector3 position = transformable.getPosition();
        position.set(startPosition);
        float value;
        switch (axis) {
            case AXIS_X:
                value = MathUtils.clamp(hitPoint.x - startHitPoint.x, -10f, 10f);
                position.add(value, 0, 0);
                break;
            case AXIS_Y:
                value = MathUtils.clamp(hitPoint.y - startHitPoint.y, -10f, 10f);
                position.add(0, value, 0);
                break;
            case AXIS_Z:
                value = MathUtils.clamp(hitPoint.z - startHitPoint.z, -10f, 10f);
                position.add(0, 0, value);
                break;
        }
        SnapUtil.snap(position, 0.01f);
        transformable.invalidate();
    }

    @Override
    public void update() {
        if (transformable != null)
            setPosition(transformable.getPosition());
    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        if (!isDragging()) return;
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        switch (axis) {
            case AXIS_X:
                renderer.setColor(Color.RED);
                renderer.line(tmp.set(position).add(-20, 0, 0), tmp2.set(position).add(20, 0, 0));
                break;
            case AXIS_Y:
                renderer.setColor(Color.BLUE);
                renderer.line(tmp.set(position).add(0, -20, 0), tmp2.set(position).add(0, 20, 0));
                break;
            case AXIS_Z:
                renderer.setColor(Color.GREEN);
                renderer.line(tmp.set(position).add(0, 0, -20), tmp2.set(position).add(0, 0, 20));
                break;
        }
        Pools.free(tmp);
        Pools.free(tmp2);

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

    @Override
    public boolean touchDown() {
        if (transformable != null) {
            startHitPoint.set(getHitPoint3D());
            startPosition.set(transformable.getPosition());
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
            setPosition(transformable.getPosition());
        }
    }
}
