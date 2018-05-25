package net.masonapps.sketchvr.sketch;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import net.masonapps.sketchvr.ui.ShapeRenderableInput;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bob Mason on 5/4/2018.
 */
public class Sketch2D implements ShapeRenderableInput {

    private static final float EPSILON = 1e-3f;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    @Nullable
    private Geometry nodedGeometry;
    private List lines = new ArrayList();
    private List<RenderLine> renderLines = new ArrayList<>();

    public Sketch2D() {
    }

    public void addRect(Rectangle rect) {
        addRing(new Coordinate[]{
                new Coordinate(rect.x, rect.y),
                new Coordinate(rect.x + rect.width, rect.y),
                new Coordinate(rect.x + rect.width, rect.y + rect.height),
                new Coordinate(rect.x, rect.y + rect.height)});
    }

    public void addCircle(Vector2 center, float radius) {
        addCircle(center, radius, 12);
    }

    public void addCircle(Vector2 center, float radius, int segments) {
        final Coordinate[] coordinates = new Coordinate[segments];
        final float aStep = MathUtils.PI2 / segments;
        for (int i = 0; i < segments; i++) {
            final float a = i * aStep;
            coordinates[i] = new Coordinate(center.x + MathUtils.cos(a) * radius, center.y + MathUtils.sin(a) * radius);
        }
        addRing(coordinates);
    }

    public void addRing(List<Vector2> vecs) {
        final Coordinate[] coordinates = new Coordinate[vecs.size()];
        vecs.stream().map(v -> new Coordinate(v.x, v.y))
                .collect(Collectors.toList())
                .toArray(coordinates);
        addRing(coordinates);
    }

    public void addRing(Vector2[] vecs) {
        final Coordinate[] coordinates = new Coordinate[vecs.length];
        Arrays.stream(vecs).map(v -> new Coordinate(v.x, v.y))
                .collect(Collectors.toList())
                .toArray(coordinates);
        addRing(coordinates);
    }

    public void addRing(Coordinate[] coordinates) {
        final LinearRing linearRing = geometryFactory.createLinearRing(coordinates);
        addGeometry(linearRing);
    }

    public void addLine(Vector2 v1, Vector2 v2) {
        addLineString(new Coordinate[]{new Coordinate(v1.x, v1.y), new Coordinate(v2.x, v2.y)});
    }

    public void addLines(Vector2[] vecs) {
        final Coordinate[] coordinates = new Coordinate[vecs.length];
        Arrays.stream(vecs).map(v -> new Coordinate(v.x, v.y))
                .collect(Collectors.toList())
                .toArray(coordinates);
        addLineString(coordinates);
    }

    public void addLines(List<Vector2> vecs) {
        final Coordinate[] coordinates = new Coordinate[vecs.size()];
        vecs.stream().map(v -> new Coordinate(v.x, v.y))
                .collect(Collectors.toList())
                .toArray(coordinates);
        addLineString(coordinates);
    }

    public void addLineString(Coordinate[] coordinates) {
        final LineString lineStr = geometryFactory.createLineString(coordinates);
        addGeometry(lineStr);
    }

    private void addGeometry(Geometry g) {
        if (nodedGeometry == null)
            nodedGeometry = g;
        else
            nodedGeometry = nodedGeometry.union(g);
        updateRenderLines();
    }

    @Nullable
    public Collection getPolygons() {
        if (nodedGeometry == null) return null;
        Polygonizer polygonizer = new Polygonizer(true);
        polygonizer.add(nodedGeometry);
        return polygonizer.getPolygons();
    }

    @Nullable
    public Collection getBufferPolygons(float distance) {
        if (nodedGeometry == null) return null;
        Polygonizer polygonizer = new Polygonizer(true);
        polygonizer.add(nodedGeometry.copy().buffer(distance, 4));
        return polygonizer.getPolygons();
    }

    @Nullable
    public Geometry getNodedGeometry() {
        return nodedGeometry;
    }

    public void clear() {
        nodedGeometry = null;
    }

    @Override
    public void draw(ShapeRenderer renderer) {
        if (renderLines.isEmpty()) return;
        renderer.setColor(Color.GREEN);
        for (RenderLine renderLine : renderLines) {
            renderer.line(renderLine.p1, renderLine.p2);
        }
    }

    private void updateRenderLines() {
        if (nodedGeometry != null) {
            renderLines.clear();
            lines.clear();
            LineStringExtracter.getLines(nodedGeometry, lines);
            for (Object o : lines) {
                if (o instanceof LineString) {
                    final LineString line = (LineString) o;
                    final int n = line.getNumPoints();
                    for (int i = 0; i < n - 1; i++) {
                        renderLines.add(new RenderLine(line.getCoordinateN(i), line.getCoordinateN(i + 1)));
                    }
                }
            }
        }
    }

    private class RenderLine {
        final Vector2 p1 = new Vector2();
        final Vector2 p2 = new Vector2();

        public RenderLine(Coordinate c1, Coordinate c2) {
            p1.set((float) c1.x, (float) c1.y);
            p2.set((float) c2.x, (float) c2.y);
        }
    }
}
