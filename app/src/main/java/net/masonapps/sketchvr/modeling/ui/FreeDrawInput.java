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
import com.google.vr.sdk.controller.Controller;

import net.masonapps.sketchvr.math.Segment;
import net.masonapps.sketchvr.mesh.Stroke;
import net.masonapps.sketchvr.modeling.SketchMeshBuilder;
import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

import java.util.List;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class FreeDrawInput extends ModelingInputProcessor {

    private static final float TOUCHPAD_SCALE = 400f;
    private final SketchMeshBuilder builder;
    private float drawDistance = 3f;
    private final Stroke stroke = new Stroke();
    private final Vector3 tmpPoint = new Vector3();
    private boolean isDrawing = false;
    private final Segment seg1 = new Segment();
    private final Segment seg2 = new Segment();
    private final Plane tmpPlane = new Plane();
    private final Plane tmpPlane2 = new Plane();
    private GestureDetector gestureDetector;

    public FreeDrawInput(SketchProjectEntity project) {
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
        if (isDrawing) {
            stroke.addPoint(tmpPoint.set(intersectionInfo.hitPoint).mul(project.getInverseTransform()));
        }
        return true;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        if (!isDrawing) return;
        shapeRenderer.setColor(Color.GREEN);
        final List<Vector3> points = stroke.points;
        for (int i = 0; i < points.size() - 1; i++) {
            shapeRenderer.line(points.get(i), points.get(i + 1));
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        isDrawing = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        isDrawing = false;
        stroke.simplifyBySegmentLength(0.125f);
        if (stroke.getPointCount() > 2) {
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
            final Array<Vector3> points = new Array<>();
            for (Vector3 point : stroke.points) {
                points.add(point);
            }
            builder.sweep(vertices, points, false);

            builder.end();

            final SketchNode node = new SketchNode(meshPart);
            project.add(node, true);
            stroke.clear();
        }
        return true;
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

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
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
