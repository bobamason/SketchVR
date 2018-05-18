package net.masonapps.sketchvr.sketch;

import com.badlogic.gdx.math.Vector2;

import net.masonapps.sketchvr.modeling.PolygonUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 5/4/2018.
 */
public class Sketch2D {

    private static final float EPSILON = 1e-3f;
    private final List<LineString> lines = new ArrayList<>();
    private final GeometryFactory geometryFactory = new GeometryFactory();
    public List<Vector2> points = new ArrayList<>();
    ;

    public void addPoint(Vector2 point) {
        Vector2 vertex = getDuplicate(point);
        if (vertex == null) {
            vertex = point.cpy();
            points.add(vertex);
        }
        if (points.size() >= 2)
            addEdge(points.get(points.size() - 2), points.get(points.size() - 1));
    }

    private Vector2 getDuplicate(Vector2 vertex) {
        for (Vector2 point : points) {
            if (point.epsilonEquals(vertex, EPSILON)) return point;
        }
        return null;
    }

    private void addEdge(Vector2 v1, Vector2 v2) {
        final LineString lineStr = new LineString(new CoordinateArraySequence(new Coordinate[]{new Coordinate(v1.x, v1.y), new Coordinate(v2.x, v2.y)}), geometryFactory);
        lines.add(lineStr);
    }

    public List<List<Vector2>> getLoops() {
        Polygonizer polygonizer = new Polygonizer(true);
        Geometry nodedLines = lines.get(0);
        for (int i = 1; i < lines.size(); i++) {
            nodedLines = nodedLines.union(lines.get(i));
        }
        polygonizer.add(nodedLines);
        return PolygonUtils.getLoops(polygonizer.getPolygons());
    }

    public void clear() {
        lines.clear();
        points.clear();
    }

    public void closePath() {
        if (points.size() >= 3)
            addEdge(points.get(points.size() - 1), points.get(0));
    }
}
