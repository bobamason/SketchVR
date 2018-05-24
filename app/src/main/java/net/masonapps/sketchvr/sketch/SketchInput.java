package net.masonapps.sketchvr.sketch;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.sketchvr.modeling.SketchMeshBuilder;
import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;
import net.masonapps.sketchvr.ui.BackButtonListener;
import net.masonapps.sketchvr.ui.ShapeRenderableInput;

import org.locationtech.jts.geom.Polygon;
import org.masonapps.libgdxgooglevr.ui.VirtualStage;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.Collection;

/**
 * Created by Bob Mason on 3/22/2018.
 */

public class SketchInput extends VirtualStage implements ShapeRenderableInput, BackButtonListener {

    private final Plane plane = new Plane();
    private final Vector2 hitPoint2D = new Vector2();
    private final Sketch2D sketch2D = new Sketch2D(plane);
    private final Vector3 point = new Vector3();
    private final Vector3 hitPoint3D = new Vector3();
    private final SketchMeshBuilder builder;
    protected boolean isCursorOver = false;
    private Ray transformedRay = new Ray();
    private Vector2 lastPoint = new Vector2();
    private SketchProjectEntity project;

    public SketchInput(SketchProjectEntity project, SpriteBatch batch, Skin skin) {
        super(batch, 1000, 1000);
        this.project = project;
        this.builder = project.getBuilder();
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
    }

    @Override
    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        plane.set(plane);
        invalidate();
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        sketch2D.draw(shapeRenderer);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver) {
            // TODO: 5/24/2018  
        }
        return isCursorOver;
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
    public boolean mouseMoved(int screenX, int screenY) {
        Logger.d("mouseMoved(" + screenX + ", " + screenY + ")");
        return super.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean onBackButtonClicked() {
        return false;
    }

    private void buildMesh() {
        builder.begin();
        final MeshPart meshPart = builder.part("p", GL20.GL_TRIANGLES);

        final Collection polygons = sketch2D.getPolygons();
        for (Object poly : polygons) {
            if (poly instanceof Polygon)
                builder.polygonExtruded((Polygon) poly, plane, 0.12f);
        }

        builder.end();
        sketch2D.clear();
        if (meshPart.mesh.getNumVertices() > 3) {
            final SketchNode node = new SketchNode(meshPart);
            project.add(node, true);
        }
    }
}
