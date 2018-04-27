package net.masonapps.sketchvr.modeling.transform;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.sketchvr.math.SnapUtil;
import net.masonapps.sketchvr.modeling.SketchNode;

import org.masonapps.libgdxgooglevr.math.PlaneUtils;
import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class RotateHandle3D extends DragHandle3D {


    private static final float HANDLE_RADIUS = 0.075f;
    private final float circleRadius = 0.6f;
    private final float margin = circleRadius / 10f;
    private final Quaternion tmpQ = new Quaternion();
    private final Quaternion startRotation = new Quaternion();
    private int numCircleSegments = 64;
    private Plane plane = new Plane();
    private Vector2 vec2 = new Vector2();
    private float angle = 0f;
    private float startAngle = 0f;
    private Vector3 tmpV = new Vector3();

    public RotateHandle3D(ModelBuilder builder, Axis axis) {
        super(createModelInstance(builder, axis), createBounds(axis), axis);
        setLightingEnabled(false);
        switch (axis) {
            case AXIS_X:
                plane.set(0f, 0f, 0f, 1f, 0f, 0f);
                break;
            case AXIS_Y:
                plane.set(0f, 0f, 0f, 0f, 1f, 0f);
                break;
            case AXIS_Z:
                plane.set(0f, 0f, 0f, 0f, 0f, 1f);
                break;
        }
    }

    private static ModelInstance createModelInstance(ModelBuilder builder, Axis axis) {
//        final Color color = new Color();
//        final Matrix4 matrix = new Matrix4();
//        switch (axis) {
//            case AXIS_X:
//                color.set(Color.RED);
//                matrix.setToTranslation(0, 0, 1f);
//                break;
//            case AXIS_Y:
//                matrix.setToTranslation(1f, 0, 0);
//                color.set(Color.BLUE);
//                break;
//            case AXIS_Z:
//                matrix.setToTranslation(0, 1f, 0);
//                color.set(Color.GREEN);
//                break;
//        }
//        final Model model = builder.createSphere(HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, 12, 6, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)), VertexAttributes.Usage.Position);
//        model.meshes.get(0).transform(matrix);
//        return new ModelInstance(model);
        return null;
    }

    private static BoundingBox createBounds(Axis axis) {
        final float r = HANDLE_RADIUS / (float) Math.sqrt(3);
        final Matrix4 matrix = new Matrix4();
        switch (axis) {
            case AXIS_X:
                matrix.setToTranslation(0, 0, 1f);
                break;
            case AXIS_Y:
                matrix.setToTranslation(1f, 0, 0);
                break;
            case AXIS_Z:
                matrix.setToTranslation(0, 1f, 0);
                break;
        }
        return new BoundingBox(new Vector3(-r, -r, -r), new Vector3(r, r, r)).mul(matrix);
    }

    @Override
    public boolean touchDown() {
        if (transformable == null) return false;
        switch (axis) {
            case AXIS_X:
                startAngle = calculateAngleX();
                break;
            case AXIS_Y:
                startAngle = calculateAngleY();
                break;
            case AXIS_Z:
                startAngle = calculateAngleZ();
                break;
        }
        startRotation.set(transformable.getRotation());
        return super.touchDown();
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (transformable == null) return false;
        if (!updated) recalculateTransform();
        switch (axis) {
            case AXIS_X:
                plane.set(transformable.getPosition(), tmpV.set(1, 0, 0));
                break;
            case AXIS_Y:
                plane.set(transformable.getPosition(), tmpV.set(0, 1, 0));
                break;
            case AXIS_Z:
                plane.set(transformable.getPosition(), tmpV.set(0, 0, 1));
                break;
        }

        if (Intersector.intersectRayPlane(ray, plane, getHitPoint3D())) {
            if (isDragging()) {
                angleChanged();
                return true;
            } else {
                final float dst = getHitPoint3D().dst(transformable.getPosition());
                if (dst > circleRadius - margin && dst < circleRadius + margin)
                    return true;
            }
        }
        return false;
    }

    private void angleChanged() {
        if (transformable == null) return;
        final Quaternion rotation = transformable.getRotation();
        switch (axis) {
            case AXIS_X:
                angle = calculateAngleX();
                tmpQ.set(Vector3.X, SnapUtil.snap(angle - startAngle, 5));
                rotation.set(startRotation).mulLeft(tmpQ);
                break;
            case AXIS_Y:
                angle = calculateAngleY();
                tmpQ.set(Vector3.Y, SnapUtil.snap(angle - startAngle, 5));
                rotation.set(startRotation).mulLeft(tmpQ);
                break;
            case AXIS_Z:
                angle = calculateAngleZ();
                tmpQ.set(Vector3.Z, SnapUtil.snap(angle - startAngle, 5));
                rotation.set(startRotation).mulLeft(tmpQ);
                break;
        }
        Logger.d("axis = " + axis.name() + " angle = " + angle + " vec2 = " + vec2);
        transformable.invalidate();
    }

    private float calculateAngleX() {
        if (transformable != null)
            PlaneUtils.toSubSpace(plane, tmpV.set(transformable.getPosition()).sub(getHitPoint3D()), vec2);
        return -MathUtils.atan2(vec2.y, vec2.x) * MathUtils.radiansToDegrees;
    }

    private float calculateAngleY() {
        if (transformable != null)
            PlaneUtils.toSubSpace(plane, tmpV.set(transformable.getPosition()).sub(getHitPoint3D()), vec2);
        return MathUtils.atan2(-vec2.y, vec2.x) * MathUtils.radiansToDegrees;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private float calculateAngleZ() {
        if (transformable != null)
            PlaneUtils.toSubSpace(plane, tmpV.set(transformable.getPosition()).sub(getHitPoint3D()), vec2);
        return MathUtils.atan2(-vec2.x, vec2.y) * MathUtils.radiansToDegrees;
    }

    @Override
    public void update() {
        if (transformable != null) {
            setPosition(transformable.getPosition());
//            switch (axis) {
//                case AXIS_X:
//                    setRotation(0, angle, 0);
//                    break;
//                case AXIS_Y:
//                    setRotation(angle, 0, 0);
//                    break;
//                case AXIS_Z:
//                    setRotation(0, 0, angle);
//                    break;
//            }
        }
    }

    @Override
    public void setTransformable(@Nullable SketchNode transformable) {
        super.setTransformable(transformable);
        if (transformable != null) {
            setPosition(transformable.getPosition());
//            final Quaternion rotation = transformable.getRotation();
//            switch (axis) {
//                case AXIS_X:
//                    angle = rotation.getPitch();
//                    setRotation(0, angle, 0);
//                    break;
//                case AXIS_Y:
//                    angle = rotation.getYaw();
//                    setRotation(angle, 0, 0);
//                    break;
//                case AXIS_Z:
//                    angle = rotation.getRoll();
//                    setRotation(0, 0, angle);
//                    break;
//            }
        }
    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        final float r = circleRadius;
        for (int i = 0; i < numCircleSegments; i++) {
            final float a = MathUtils.PI2 / (float) numCircleSegments * i - MathUtils.PI;
            final float a2 = MathUtils.PI2 / (float) numCircleSegments * (i + 1) - MathUtils.PI;
            switch (axis) {
                case AXIS_X:
                    renderer.setColor(Color.RED);
                    renderer.line(tmp.set(0, MathUtils.sin(a) * r, MathUtils.cos(a) * r).add(position), tmp2.set(0, MathUtils.sin(a2) * r, MathUtils.cos(a2) * r).add(position));
                    break;
                case AXIS_Y:
                    renderer.setColor(Color.BLUE);
                    renderer.line(tmp.set(MathUtils.cos(a) * r, 0, -MathUtils.sin(a) * r).add(position), tmp2.set(MathUtils.cos(a2) * r, 0, -MathUtils.sin(a2) * r).add(position));
                    break;
                case AXIS_Z:
                    renderer.setColor(Color.GREEN);
                    renderer.line(tmp.set(MathUtils.cos(a) * r, MathUtils.sin(a) * r, 0).add(position), tmp2.set(MathUtils.cos(a2) * r, MathUtils.sin(a2) * r, 0).add(position));
                    break;
            }
        }
        if (isDragging()) {
            renderer.setColor(Color.WHITE);
            switch (axis) {
                case AXIS_X:
                    renderer.line(tmp.set(position), tmp2.set(0, MathUtils.sinDeg(startAngle) * r, MathUtils.cosDeg(startAngle) * r).add(position));
                    renderer.line(tmp.set(position), tmp2.set(0, MathUtils.sinDeg(angle) * r, MathUtils.cosDeg(angle) * r).add(position));
                    break;
                case AXIS_Y:
                    renderer.line(tmp.set(position), tmp2.set(MathUtils.cosDeg(startAngle) * r, 0, -MathUtils.sinDeg(startAngle) * r).add(position));
                    renderer.line(tmp.set(position), tmp2.set(MathUtils.cosDeg(angle) * r, 0, -MathUtils.sinDeg(angle) * r).add(position));
                    break;
                case AXIS_Z:
                    renderer.line(tmp.set(position), tmp2.set(MathUtils.cosDeg(startAngle) * r, MathUtils.sinDeg(startAngle) * r, 0).add(position));
                    renderer.line(tmp.set(position), tmp2.set(MathUtils.cosDeg(angle) * r, MathUtils.sinDeg(angle) * r, 0).add(position));
                    break;
            }
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
    public void dispose() {
        super.dispose();
        if (modelInstance != null) {
            modelInstance.model.dispose();
            modelInstance = null;
        }
    }

    public void setNumCircleSegments(int numCircleSegments) {
        this.numCircleSegments = numCircleSegments;
    }
}
