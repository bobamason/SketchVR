package net.masonapps.sketchvr.modeling.transform;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.sketchvr.actions.TransformAction;
import net.masonapps.sketchvr.math.RotationUtil;
import net.masonapps.sketchvr.math.SnapUtil;
import net.masonapps.sketchvr.modeling.EditableNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.vr.ArmModel;

/**
 * Created by Bob Mason on 2/19/2018.
 */

public class SimpleGrabControls {

    @Nullable
    private EditableNode node = null;
    private Ray ray = new Ray();
    private Plane plane = new Plane();
    private TransformAction.Transform startTransform = new TransformAction.Transform();
    ;
    private Vector3 hitPoint1 = new Vector3();
    private Vector3 hitPoint2 = new Vector3();
    private Vector3 tmp = new Vector3();
    private Vector3 offset = new Vector3();
    private float startAngle = 0f;
    private Quaternion tmpQ = new Quaternion();
    @Nullable
    private TransformWidget3D.OnTransformActionListener listener = null;

    public void begin(EditableNode node, Vector3 hitPoint, SketchProjectEntity modelingProject2) {
        this.node = node;
        if (listener != null)
            listener.onTransformStarted(node);
        final ArmModel armModel = GdxVr.input.getArmModel();
        node.getTransform(startTransform);
        ray.set(GdxVr.input.getInputRay());
        startAngle = armModel.pointerRotation.getAngleAround(ray.direction);
        ray.mul(modelingProject2.getInverseTransform());
        final Vector3 point = node.getPosition();
        offset.set(hitPoint).sub(point);
        plane.set(point.x, point.y, point.z, -ray.direction.x, -ray.direction.y, -ray.direction.z);
        RotationUtil.setToClosestUnitVector(plane.normal);

        Intersector.intersectRayPlane(ray, plane, hitPoint1);
//        Logger.d("start angle = " + startAngle);
//        Logger.d("ray = " + ray.toString());
//        Logger.d("hitPoint1 = " + hitPoint1);
//        Logger.d("plane = " + plane.toString());
    }

    public boolean update(Vector3 outHitPoint, SketchProjectEntity modelingProject2) {
        if (node != null) {
            final ArmModel armModel = GdxVr.input.getArmModel();
            ray.set(GdxVr.input.getInputRay());
            float angle = armModel.pointerRotation.getAngleAround(ray.direction);
            ray.mul(modelingProject2.getInverseTransform());
            if (Intersector.intersectRayPlane(ray, plane, hitPoint2)) {
                node.getRotation().set(startTransform.rotation).mulLeft(tmpQ.set(plane.normal, -SnapUtil.snap(angle - startAngle, 5f)));
                final Vector3 nodePosition = node.getPosition();
                nodePosition.set(hitPoint2).sub(hitPoint1).add(startTransform.position);
                outHitPoint.set(nodePosition).add(offset);
//                Logger.d("angle = " + angle);
//                Logger.d("hitPoint1 = " + hitPoint1);
//                Logger.d("hitPoint2 = " + hitPoint2);
//                Logger.d("position = " + nodePosition);
                SnapUtil.snap(nodePosition, 0.05f);
                node.calculateTransforms(false);
                return true;
            }
        }
        return false;
    }

    public void end() {
        if (listener != null && node != null)
            listener.onTransformFinished(node);
        node = null;
    }

    public void setListener(@Nullable TransformWidget3D.OnTransformActionListener listener) {
        this.listener = listener;
    }

    public Plane getPlane() {
        return plane;
    }

    public boolean isTransforming() {
        return node != null;
    }
}
