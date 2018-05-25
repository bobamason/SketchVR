package net.masonapps.sketchvr.sketch;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.sketchvr.modeling.SketchMeshBuilder;
import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.modeling.SketchProjectEntity;
import net.masonapps.sketchvr.ui.BackButtonListener;
import net.masonapps.sketchvr.ui.ShapeRenderableInput;

import org.locationtech.jts.geom.Polygon;
import org.masonapps.libgdxgooglevr.math.PlaneUtils;
import org.masonapps.libgdxgooglevr.ui.VirtualStage;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.Collection;

/**
 * Created by Bob Mason on 3/22/2018.
 */

public class SketchInput extends VirtualStage implements ShapeRenderableInput, BackButtonListener {

    private final Sketch2D sketch2D;
    private final SketchMeshBuilder builder;
    private Vector2 lastPoint = new Vector2();
    private SketchProjectEntity project;
    private boolean isDrawing = false;

    public SketchInput(SketchProjectEntity project, SpriteBatch batch, Skin skin) {
        super(batch, 1000, 1000);
        this.project = project;
        this.builder = project.getBuilder();
        sketch2D = new Sketch2D(getPlane());
    }

    @Override
    public void recalculateTransform() {
        PlaneUtils.getToSpaceMatrix(getPlane(), transform);

        final float hw = getViewport().getCamera().viewportWidth * pixelSizeWorld / 2f;
        final float hh = getViewport().getCamera().viewportHeight * pixelSizeWorld / 2f;
        bounds.set(-hw, -hh, hw, hh);

        radius = (float) Math.sqrt(bounds.width * bounds.width + bounds.height * bounds.height);
        updated = true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        if (!isVisible()) return;
        if (!updated) recalculateTransform();
        shapeRenderer.setTransformMatrix(transform);
        sketch2D.draw(shapeRenderer);
        if (isCursorOver && isDrawing)
            shapeRenderer.line(lastPoint, getHitPoint2D());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!isVisible() || !isCursorOver) return false;
        if (!isDrawing)
            isDrawing = true;
        else
            sketch2D.addLine(lastPoint, getHitPoint2D());
        lastPoint.set(getHitPoint2D());
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        isDrawing = false;
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
        final Collection polygons = sketch2D.getPolygons();
        if (polygons == null) {
            Logger.d("no polygons in sketch");
            return;
        }
        builder.begin();
        final MeshPart meshPart = builder.part("p", GL20.GL_TRIANGLES);

        for (Object poly : polygons) {
            if (poly instanceof Polygon)
                builder.polygonExtruded((Polygon) poly, getPlane(), 0.12f);
        }

        builder.end();
        sketch2D.clear();
        if (meshPart.mesh.getNumVertices() > 3) {
            final SketchNode node = new SketchNode(meshPart);
            project.add(node, true);
        }
    }
}
