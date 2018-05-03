package net.masonapps.sketchvr.modeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.sketchvr.math.Segment;
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
    private final Stroke stroke = new Stroke();
    private final Vector3 tmpPoint = new Vector3();
    private boolean isDrawing = false;
    private final Segment seg1 = new Segment();
    private final Segment seg2 = new Segment();
    private final Plane tmpPlane = new Plane();
    private final Plane tmpPlane2 = new Plane();

    public FreeDrawInput(SketchProjectEntity project) {
        super(project);
        this.builder = project.getBuilder();
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
        stroke.simplifyBySegmentLength(0.125f);
        builder.begin();
        final MeshPart meshPart = builder.part("p", GL20.GL_TRIANGLES);
        final int pointCount = stroke.getPointCount();
        for (int i = 1; i < pointCount; i++) {
            seg1.set(stroke.points.get(i - 1), stroke.points.get(i));
            if (i == 1) {
                tmpPlane.set(seg1.p1, seg1.direction);
                // TODO: 5/2/2018 start cap 
            } else if (i == pointCount - 1) {
                tmpPlane.set(seg1.p1.x, seg1.p1.y, seg1.p1.z, -seg1.direction.x, -seg1.direction.y, -seg1.direction.z);
                // TODO: 5/2/2018 end cap 
            } else {
                seg2.set(stroke.points.get(i), stroke.points.get(i + 1));
                tmpPlane2.set(seg1.p1, seg1.direction);
            }

        }
        builder.end();
        if (meshPart.size > 0) {
            final SketchNode node = new SketchNode(meshPart, new Material(ColorAttribute.createDiffuse(Color.GREEN), ColorAttribute.createAmbient(Color.GREEN)));
            project.add(node, true);
        }
        stroke.clear();
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
