package net.masonapps.sketchvr.controller;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.sketchvr.R;
import net.masonapps.sketchvr.Style;
import net.masonapps.sketchvr.math.Animator;
import net.masonapps.sketchvr.math.RotationUtil;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import org.masonapps.libgdxgooglevr.GdxVr;


/**
 * Created by Bob on 8/15/2017.
 */

public class ViewControlsVirtualStage extends QuadButtonVirtualStage {

    private static final float SENSITIVITY = 0.5f;
    private static final float LIMIT = 2f;
    private final SketchProjectEntity project;
    private final Quaternion rotation = new Quaternion();
    private final Quaternion lastRotation = new Quaternion();
    private final Quaternion startRotation = new Quaternion();
    private final Quaternion snappedRotation = new Quaternion();
    private final Vector3 projectPosition = new Vector3(0, -0.5f, -2);
    private final Vector3 snappedPosition = new Vector3(projectPosition);
    private final Vector3 startHitPoint = new Vector3();
    private final Vector3 hitPoint = new Vector3();
    private final Vector3 startPosition = new Vector3();
    private final Vector3 center = new Vector3();
    private final Vector3 tmp = new Vector3();
    private final Vector3 nor = new Vector3();
    private final Animator rotationAnimator;
    private final Animator positionAnimator;
    private final OnTransformChangedListener listener;
    private final Plane hitPlane = new Plane();
    private float zoom = 1f;
    private float startZoom = 1f;
    private TransformAction transformAction = TransformAction.NONE;

    public ViewControlsVirtualStage(SketchProjectEntity project, Batch batch, Skin skin, float diameter, OnTransformChangedListener listener) {
        super(batch, skin, diameter,
                skin.newDrawable(Style.Drawables.ic_pan),
                Style.getStringResource(R.string.pan, "pan"),
                null,
                null,
                skin.newDrawable(Style.Drawables.ic_zoom),
                Style.getStringResource(R.string.zoom, "zoom"),
                skin.newDrawable(Style.Drawables.ic_rotate),
                Style.getStringResource(R.string.rotate, "rotate"));
        this.project = project;
        this.listener = listener;


        project.setPosition(projectPosition);

        rotationAnimator = new Animator(new Animator.AnimationListener() {
            @Override
            public void apply(float value) {
                final Quaternion rot = project.getRotation();
                rot.set(rotation).slerp(snappedRotation, value);
                lastRotation.set(rot);
                project.invalidate();
                listener.onTransformChanged(project.getTransform());
            }

            @Override
            public void finished() {
                rotation.set(snappedRotation);
                lastRotation.set(rotation);
            }
        });
        rotationAnimator.setInterpolation(Interpolation.linear);

        positionAnimator = new Animator(new Animator.AnimationListener() {
            @Override
            public void apply(float value) {
                project.getPosition().set(projectPosition).slerp(snappedPosition, value);
                project.invalidate();
                listener.onTransformChanged(project.getTransform());
            }

            @Override
            public void finished() {
                projectPosition.set(snappedPosition);
            }
        });
        positionAnimator.setInterpolation(Interpolation.linear);
    }

    @Override
    public void act(float delta) {
        if (!isVisible()) return;
        super.act(delta);
        rotationAnimator.update(delta);
        positionAnimator.update(delta);
        final Ray ray = GdxVr.input.getInputRay();
        switch (transformAction) {
            case PAN:
                pan(ray);
                break;
            case ROTATE:
                rotate();
                break;
            case ZOOM:
                zoom(ray);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onButtonDown(int focusedButton) {
        startRotation.set(GdxVr.input.getControllerOrientation());
        lastRotation.set(GdxVr.input.getControllerOrientation());
        final Ray ray = GdxVr.input.getInputRay();
        RotationUtil.setToClosestUnitVector(tmp.set(ray.direction));
        nor.set(tmp).scl(-1);
        hitPlane.set(tmp.scl(2f).add(ray.origin), nor);
        Intersector.intersectRayPlane(ray, hitPlane, startHitPoint);
        switch (focusedButton) {
            case QuadButtonListener.TOP:
                startPosition.set(projectPosition);
                transformAction = TransformAction.PAN;
                break;
            case QuadButtonListener.BOTTOM:
                break;
            case QuadButtonListener.LEFT:
                startZoom = zoom;
                transformAction = TransformAction.ZOOM;
                break;
            case QuadButtonListener.RIGHT:
                transformAction = TransformAction.ROTATE;
                break;
        }
    }

    @Override
    public void onButtonUp() {
        transformAction = TransformAction.NONE;
        if (RotationUtil.snap(rotation, snappedRotation, 0.1f)) {
            final Quaternion rotDiff = Pools.obtain(Quaternion.class);
            rotDiff.set(rotation).conjugate().mulLeft(snappedRotation);
            final float angleRad = rotDiff.getAngleRad();
            final float duration = Math.abs(angleRad < MathUtils.PI ? angleRad : MathUtils.PI2 - angleRad) / MathUtils.PI;
            Pools.free(rotDiff);
            rotationAnimator.setDuration(duration);
            rotationAnimator.start();

            snappedPosition.set(center).scl(-1).mul(snappedRotation).add(projectPosition);
            positionAnimator.setDuration(duration);
            positionAnimator.start();
        }
    }

    private void pan(Ray ray) {
        if (Intersector.intersectRayPlane(ray, hitPlane, hitPoint)) {
            projectPosition.set(hitPoint).sub(startHitPoint).scl(SENSITIVITY).limit(LIMIT).add(startPosition);
            tmp.set(center).scl(-1).mul(rotation).add(projectPosition);
            project.setPosition(tmp);
            listener.onTransformChanged(project.getTransform());
        }
    }

    private void rotate() {
        final Quaternion rotDiff = Pools.obtain(Quaternion.class);
        rotDiff.set(lastRotation).conjugate().mulLeft(GdxVr.input.getControllerOrientation());
//        RotationUtil.snapAxisAngle(rotDiff);
//        Logger.d("rotDiff " + rotDiff);

        rotation.mulLeft(rotDiff);
        project.setRotation(rotation);
        tmp.set(center).scl(-1).mul(rotation).add(projectPosition);
        project.setPosition(tmp);
//        gridEntity.setPosition(project.getPosition());
//        gridEntity.setRotation(project.getRotation());
        lastRotation.set(GdxVr.input.getControllerOrientation());
        Pools.free(rotDiff);
        listener.onTransformChanged(project.getTransform());
    }

    private void zoom(Ray ray) {
        // TODO: 5/9/2018 scale model
//        listener.onTransformChanged(project.getTransform());
    }

    private enum TransformAction {
        NONE, PAN, ROTATE, ZOOM
    }

    public interface OnTransformChangedListener {
        void onTransformChanged(Matrix4 transform);
    }
}
