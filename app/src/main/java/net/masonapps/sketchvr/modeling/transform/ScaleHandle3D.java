package net.masonapps.sketchvr.modeling.transform;

import android.opengl.GLES20;

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

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class ScaleHandle3D extends DragHandle3D {

    private static final float BOX_SIZE = 0.125f;
    private final Plane plane = new Plane();
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
        matrix.scale(BOX_SIZE, BOX_SIZE, BOX_SIZE);

        builder.begin();
        final MeshPartBuilder part = builder.part("t" + axis.name(), GLES20.GL_TRIANGLES, VertexAttributes.Usage.Position, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)));
        BoxShapeBuilder.build(part, matrix);
        return new ModelInstance(builder.end());
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (sketchNode == null) return false;
        if (!updated) recalculateTransform();
        if (isDragging()) {
            if (shouldSetPlane) {
                normal.set(ray.origin).sub(position);
                setToClosestUnitVector(normal);
                plane.set(position, normal.mul(sketchNode.getRotation()));
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
        if (sketchNode == null) return;
        float diff;
        float startD;
        switch (axis) {
            case AXIS_X:
                diff = Math.max(getHitPoint3D().x - sketchNode.getPosition().x, 1e-3f);
                startD = Math.max(sketchNode.getBounds().getWidth() / 2, 1e-3f);
                sketchNode.setScaleX(diff / startD);
                break;
            case AXIS_Y:
                diff = Math.max(getHitPoint3D().y - sketchNode.getPosition().y, 1e-3f);
                startD = Math.max(sketchNode.getBounds().getHeight() / 2, 1e-3f);
                sketchNode.setScaleY(diff / startD);
                break;
            case AXIS_Z:
                diff = Math.max(getHitPoint3D().z - sketchNode.getPosition().z, 1e-3f);
                startD = Math.max(sketchNode.getBounds().getDepth() / 2, 1e-3f);
                sketchNode.setScaleZ(diff / startD);
                break;
        }
    }

    @Override
    public boolean touchDown() {
        return super.touchDown();
    }

    @Override
    public boolean touchUp() {
        shouldSetPlane = true;
        return super.touchUp();

    }

    @Override
    public void update() {
        if (sketchNode != null) {
            setRotation(sketchNode.getRotation());
            final Vector3 tmp = Pools.obtain(Vector3.class);
            final Vector3 pos = sketchNode.getPosition();
            switch (axis) {
                case AXIS_X:
                    tmp.set(sketchNode.getAABB().getWidth() / 2 + BOX_SIZE, 0, 0).mul(rotation).add(pos);
                    break;
                case AXIS_Y:
                    tmp.set(0, sketchNode.getAABB().getHeight() / 2 + BOX_SIZE, 0).mul(rotation).add(pos);
                    break;
                case AXIS_Z:
                    tmp.set(0, 0, sketchNode.getAABB().getDepth() / 2 + BOX_SIZE).mul(rotation).add(pos);
                    break;
            }
            setPosition(tmp);
            Pools.free(tmp);
        }
    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        if (sketchNode != null) {
            final Vector3 tmp = Pools.obtain(Vector3.class);
            final Vector3 pos = sketchNode.getPosition();
            switch (axis) {
                case AXIS_X:
                    renderer.setColor(Color.RED);
                    break;
                case AXIS_Y:
                    renderer.setColor(Color.BLUE);
                    break;
                case AXIS_Z:
                    renderer.setColor(Color.GREEN);
                    break;
            }

            renderer.line(pos, getPosition());
            Pools.free(tmp);
        }
    }
}
