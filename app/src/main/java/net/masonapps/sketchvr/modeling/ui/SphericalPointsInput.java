package net.masonapps.sketchvr.modeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

import net.masonapps.sketchvr.math.Segment;
import net.masonapps.sketchvr.modeling.SketchMeshBuilder;
import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class SphericalPointsInput extends ModelingInputProcessor {

    private static final float TOUCHPAD_SCALE = 400f;
    private final SketchMeshBuilder builder;
    private final Vector3 point = new Vector3();
    private final Segment seg1 = new Segment();
    private final Segment seg2 = new Segment();
    private final Plane tmpPlane = new Plane();
    private final Plane tmpPlane2 = new Plane();
    private float drawDistance = 3f;
    private GestureDetector gestureDetector;
    private List<Vector3> points = new ArrayList<>();

    public SphericalPointsInput(SketchProjectEntity project) {
        super(project);
        this.builder = project.getBuilder();
        gestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                final float sensitivity = 4f;
                setDrawDistance(getDrawDistance() + deltaY / TOUCHPAD_SCALE * sensitivity);
                return true;
            }
        });
    }

    @Override
    public boolean performRayTest(Ray ray) {
        intersectionInfo.hitPoint.set(ray.origin).add(ray.direction).nor().scl(drawDistance);
        point.set(intersectionInfo.hitPoint).mul(project.getInverseTransform());
        return true;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        for (int i = 0; i < points.size() - 1; i++) {
            shapeRenderer.line(points.get(i), points.get(i + 1));
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        points.add(point.cpy());
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    private void buildMesh() {
        final FloatArray vertices = new FloatArray();

        builder.begin();

        final MeshPart meshPart = builder.part("p", GL20.GL_TRIANGLES);

        final Vector2 p = new Vector2();
        final float sides = 6;
        for (int i = 0; i < sides; i++) {
            float a = i * (360f / sides);
            p.set(0.1f, 0f).rotate(a);
            vertices.add(p.x);
            vertices.add(p.y);
        }
        final Array<Vector3> pointArray = new Array<>();
        for (Vector3 point : points) {
            pointArray.add(point);
        }
        builder.sweep(vertices, pointArray, false);

        builder.end();

        final SketchNode node = new SketchNode(meshPart);
        project.add(node, true);
    }

    @Override
    public boolean onBackButtonClicked() {
        if (points.size() > 2) {
            buildMesh();
            points.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    public float getDrawDistance() {
        return drawDistance;
    }

    public void setDrawDistance(float drawDistance) {
        this.drawDistance = drawDistance;
    }

    public void onControllerTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                gestureDetector.touchDown(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0, 0);
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                gestureDetector.touchDragged(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0);
                break;
            case DaydreamTouchEvent.ACTION_UP:
                gestureDetector.touchUp(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0, 0);
                break;
        }
    }
}
