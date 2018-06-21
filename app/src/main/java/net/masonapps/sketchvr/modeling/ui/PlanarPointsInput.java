package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.masonapps.sketchvr.modeling.SketchMeshBuilder;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;
import net.masonapps.sketchvr.sketch.Sketch2D;

import org.locationtech.jts.geom.Polygon;
import org.masonapps.libgdxgooglevr.math.PlaneUtils;

import java.util.Collection;

/**
 * Created by Bob Mason on 3/22/2018.
 */

public class PlanarPointsInput extends ModelingInputProcessor {

    private final Plane plane = new Plane();
    private final Array<Vector3> points = new Array<>();
    private final Vector2 hitPoint2D = new Vector2();
    private final Sketch2D sketch2D = new Sketch2D();
    private final Vector3 point = new Vector3();
    private final Vector3 hitPoint3D = new Vector3();
    private final SketchMeshBuilder builder;
    private final OnPointAddedListener listener;
    protected boolean isCursorOver = false;
    private Ray transformedRay = new Ray();
    private Vector2 lastPoint = new Vector2();
    private final Matrix4 tmpM = new Matrix4();

    public PlanarPointsInput(SketchProjectEntity project, OnPointAddedListener listener) {
        super(project);
        this.builder = SketchMeshBuilder.getInstance();
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
        if (isCursorOver && points.size > 1 && point.dst(points.get(0)) < 0.1f)
            point.set(points.get(0));
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
        shapeRenderer.setTransformMatrix(project.getTransform());
        shapeRenderer.setColor(Color.WHITE);
        final float r = 0.05f;
        final float d = 2f * r;
        points.forEach(p -> shapeRenderer.box(p.x - r, p.y - r, p.z + r, d, d, d));

        if (isCursorOver)
            shapeRenderer.box(point.x - r, point.y - r, point.z - r, d, d, d);

        shapeRenderer.setColor(Color.GREEN);
//        shapeRenderer.setTransformMatrix(PlaneUtils.getToSpaceMatrix(plane, tmpM));
//        sketch2D.draw(shapeRenderer);
        for (int i = 0; i < points.size; i++) {
            if (i == points.size - 1)
                shapeRenderer.line(points.get(i), point);
            else
                shapeRenderer.line(points.get(i), points.get(i + 1));
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver) {
            final Vector3 cpy = point.cpy();
            if (points.size > 1 && cpy.epsilonEquals(points.get(0), 0.001f)) {
                closePath();
            } else {
                points.add(cpy);
                listener.pointAdded(cpy);
                PlaneUtils.toSubSpace(plane, cpy, hitPoint2D);
                if (points.size >= 2)
                    sketch2D.addLine(lastPoint.cpy(), hitPoint2D.cpy());
                lastPoint.set(hitPoint2D);
            }
        }
        return isCursorOver;
    }

    private void closePath() {
        if (points.size < 3) {
            points.clear();
            sketch2D.clear();
            return;
        }
        sketch2D.addLine(lastPoint, PlaneUtils.toSubSpace(plane, point, hitPoint2D));
        builder.begin();
        final MeshPart meshPart = builder.part("p", GL20.GL_TRIANGLES);

        final Collection polygons = sketch2D.getPolygons();
        for (Object poly : polygons) {
            if (poly instanceof Polygon)
                builder.polygonExtruded((Polygon) poly, new Plane(Vector3.Z, 0), 0.12f);
        }

        builder.end();
        points.clear();
        sketch2D.clear();
        if (meshPart.mesh.getNumVertices() > 3) {
            // TODO: 6/20/2018 fix 
//            final SketchNode node = new SketchNode(csg, meshPart);
//            PlaneUtils.getToSpaceMatrix(plane, tmpM);
//            node.translate(tmpM.getTranslation(new Vector3()));
//            node.setRotation(tmpM.getRotation(new Quaternion()));
//            project.add(node, true);
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return isCursorOver;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return isCursorOver;
    }

    @Override
    public boolean onBackButtonClicked() {
        if (points.size >= 2) {
            builder.begin();
            final MeshPart meshPart = builder.part("p", GL20.GL_TRIANGLES);

            final Collection polygons = sketch2D.getBufferPolygons(0.06f);
            for (Object poly : polygons) {
                if (poly instanceof Polygon)
                    builder.polygonExtruded((Polygon) poly, new Plane(Vector3.Z, 0), 0.12f);
            }

            builder.end();
            points.clear();
            sketch2D.clear();
            if (meshPart.mesh.getNumVertices() > 3) {
                // TODO: 6/20/2018 fix 
//                final SketchNode node = new SketchNode(csg, meshPart);
//                PlaneUtils.getToSpaceMatrix(plane, tmpM);
//                node.translate(tmpM.getTranslation(new Vector3()));
//                node.setRotation(tmpM.getRotation(new Quaternion()));
//                project.add(node, true);
            }
            return true;
        }
        return false;
    }

    public interface OnPointAddedListener {
        void pointAdded(Vector3 point);
    }
}
