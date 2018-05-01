package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.masonapps.sketchvr.math.PathUtils;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

/**
 * Created by Bob Mason on 3/22/2018.
 */

public class PlanarSplineInput extends ModelingInputProcessor {

    public static final int SPLINE_DEGREE = 2;
    private final Plane plane = new Plane();
    private final Array<Vector3> points = new Array<>();
    //    private final Vector2 hitPoint2D = new Vector2();
    private final Vector3 point = new Vector3();
    private final Vector3 hitPoint3D = new Vector3();
    private final OnPointAddedListener listener;
    protected boolean isCursorOver = false;
    private Ray transformedRay = new Ray();
    // TODO: 3/23/2018 remove spline test 
    private BSpline<Vector3> spline = new BSpline<>();

    public PlanarSplineInput(SketchProjectEntity project, OnPointAddedListener listener) {
        super(project);
        this.listener = listener;
    }

    public Plane getPlane() {
        return plane;
    }

    public Array<Vector3> getPoints() {
        return points;
    }

    public void reset() {
        points.clear();
    }

    @Override
    public boolean performRayTest(Ray ray) {
        transformedRay.set(ray).mul(project.getInverseTransform());
        isCursorOver = Intersector.intersectRayPlane(transformedRay, plane, point);
        if (isCursorOver) hitPoint3D.set(point).mul(project.getTransform());
        return isCursorOver;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    @Nullable
    @Override
    public Vector2 getHitPoint2D() {
        return null;
    }

    @Nullable
    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint3D;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        if (points.size == 0) return;
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < points.size; i++) {
            if (i == points.size - 1) {
                if (isCursorOver)
                    shapeRenderer.line(points.get(i), point);
            } else {
                shapeRenderer.line(points.get(i), points.get(i + 1));
            }
        }
        shapeRenderer.setColor(Color.GREEN);

        if (points.size > SPLINE_DEGREE) {
//        ElapsedTimer.getInstance().start("draw spline");
//        final int segments = Math.max(points.size, MathUtils.ceil(spline.approxLength(2)) * 10);
            final int segments = points.size * 4;
//        Logger.d("points.size = " + points.size + " segments = " + segments);
            PathUtils.drawPath(shapeRenderer, spline, segments);
//        ElapsedTimer.getInstance().print("draw spline");
        }

        final float r = 0.05f;
        final float d = 2f * r;
        points.forEach(p -> shapeRenderer.box(p.x - r, p.y - r, p.z + r, d, d, d));

        if (isCursorOver)
            shapeRenderer.box(point.x - r, point.y - r, point.z - r, d, d, d);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver) {
            final Vector3 cpy = point.cpy();
            points.add(cpy);
            listener.pointAdded(cpy);
            if (points.size > SPLINE_DEGREE)
                spline.set(points.toArray(Vector3.class), SPLINE_DEGREE, false);
        }
        return isCursorOver;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return isCursorOver;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    public interface OnPointAddedListener {
        void pointAdded(Vector3 point);
    }
}
