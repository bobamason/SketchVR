package net.masonapps.sketchvr.modeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.sketchvr.mesh.Stroke;
import net.masonapps.sketchvr.modeling.SketchMeshBuilder;
import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;

import java.util.List;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class FreeDrawInput extends ModelingInputProcessor {

    private final SketchMeshBuilder builder;
    private float drawDistance = 3f;
    private Stroke stroke = new Stroke();
    private Vector3 tmpPoint = new Vector3();
    private boolean isDrawing = false;

    public FreeDrawInput(SketchProjectEntity project, SketchMeshBuilder builder) {
        super(project);
        this.builder = builder;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        intersectionInfo.hitPoint.set(ray.direction).scl(drawDistance).add(ray.origin);
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
        stroke.simplifyByPerpendicularDistance(0.025f);
        builder.begin();
        final int pointCount = stroke.getPointCount();
        for (int i = 1; i < pointCount; i++) {
            final Vector3 p1 = stroke.points.get(i - 1);
            final Vector3 p2 = stroke.points.get(i);
            final Vector3 p3 = new Vector3();
            // TODO: 4/30/2018 remove test 
            p3.set(p2).sub(p1).scl(0.5f).add(p1).add(0, 0.2f, 0);
        }
        project.add(new SketchNode(builder.end()));
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
}
