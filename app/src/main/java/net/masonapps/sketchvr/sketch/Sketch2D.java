package net.masonapps.sketchvr.sketch;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bob Mason on 5/4/2018.
 */
public class Sketch2D {

    private static final float EPSILON = 1e-3f;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    @Nullable
    private Geometry nodedLines;

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
        if (nodedLines == null)
            nodedLines = g;
        else
            nodedLines = nodedLines.union(g);
    }

    @Nullable
    public Collection getPolygons() {
        if (nodedLines == null) return null;
        Polygonizer polygonizer = new Polygonizer(true);
        polygonizer.add(nodedLines);
        return polygonizer.getPolygons();
    }

    @Nullable
    public Collection getBufferPolygons(float distance) {
        if (nodedLines == null) return null;
        Polygonizer polygonizer = new Polygonizer(true);
        polygonizer.add(nodedLines.buffer(distance, 4));
        return polygonizer.getPolygons();
    }

    public void clear() {
        nodedLines = null;
    }
}
