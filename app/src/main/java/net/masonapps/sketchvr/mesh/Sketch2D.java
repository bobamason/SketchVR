package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Bob Mason on 5/4/2018.
 */
public class Sketch2D {

    private static final float EPSILON = 1e-3f;
    public List<Vector2> points = new ArrayList<>();
    public List<Vector2> tmpPoints = new ArrayList<>();
    private final List<Geometry> lines = new ArrayList<>();
    private final WKTReader rdr = new WKTReader();

    public void addPoint(Vector2 point) {
        Vector2 vertex = getDuplicate(point);
        if (vertex == null) {
            vertex = point.cpy();
            points.add(vertex);
        }
        if (points.size() >= 2)
            checkSelfIntersection();
    }

    private Vector2 getDuplicate(Vector2 vertex) {
        for (Vector2 point : points) {
            if (point.epsilonEquals(vertex, EPSILON)) return point;
        }
        return null;
    }

    private void checkSelfIntersection() {
        boolean hasIntersection = false;
        final int lastSegmentStart = points.size() - 2;
        final int lastSegmentEnd = points.size() - 1;
        Vector2 segStart = points.get(lastSegmentStart);
        Vector2 segEnd = points.get(lastSegmentEnd);
        if (points.size() >= 4) {
            final Vector2 intersection = new Vector2();
            tmpPoints.clear();
            for (int i = 0; i < lastSegmentStart; i++) {
                tmpPoints.add(i, points.get(i));
            }
            for (int i = 1; i < tmpPoints.size(); i++) {
                final Vector2 p1 = tmpPoints.get(i - 1);
                final Vector2 p2 = tmpPoints.get(i);
                if (Intersector.intersectSegments(p1, p2, segStart, segEnd, intersection)) {
                    hasIntersection = true;
                    Logger.d("self intersection [" + p1 + ", " + p2 + " | " + segStart + ", " + segEnd + "] " + "[" + (i - 1) + ", " + i + " | " + lastSegmentStart + ", " + lastSegmentEnd + "] ");

//                    if (intersection.epsilonEquals(segStart, EPSILON)) {
//                        addEdge(p1, segStart);
//                        addEdge(segStart, p2);
//                    } else if (intersection.epsilonEquals(segEnd, EPSILON)) {
//                        addEdge(p1, segEnd);
//                        addEdge(segEnd, p2);
//                    } else 
                    if (intersection.epsilonEquals(p1, EPSILON)) {
                        addEdge(segStart, p1);
                        addEdge(p1, segEnd);
                    } else if (intersection.epsilonEquals(p2, EPSILON)) {
                        addEdge(segStart, p2);
                        addEdge(p2, segEnd);
                    } else {
                        final Vector2 v = intersection.cpy();
                        points.add(i, v);
                        addEdge(p1, v);
                        addEdge(v, p2);
                        addEdge(segStart, v);
                        addEdge(v, segEnd);
                    }
                }
            }
        }
        if (!hasIntersection) {
            addEdge(segStart, segEnd);
        }
    }

    private void addEdge(Vector2 v1, Vector2 v2) {
//        final Geometry lineStr = new LineString(new CoordinateArraySequence(new Coordinate[]{new Coordinate(v1.x, v1.y), new Coordinate(v2.x, v2.y)}), new GeometryFactory());
//        lines.add(lineStr);

        try {
            final String lineStr = "LINESTRING (" + v1.x + " " + v1.y + ", " + v2.x + " " + v2.y + ")";
            Logger.d(lineStr);
            lines.add(rdr.read(lineStr));
        } catch (ParseException e) {
            Logger.e("ParseException", e);
        }
    }

    public List<List<Vector2>> getLoops() {
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        final ArrayList<List<Vector2>> loops = new ArrayList<>();
        final Collection polygons = polygonizer.getPolygons();
        Logger.d("polygon count = " + polygons.size());
        Logger.d("polygons = " + polygons);
        for (Object obj : polygons) {
            if (obj instanceof Polygon) {
                final Polygon poly = (Polygon) obj;
                final Coordinate[] coordinates = poly.getCoordinates();
                if (coordinates.length >= 3) {
                    final ArrayList<Vector2> loop = new ArrayList<>();
                    for (Coordinate coordinate : coordinates) {
                        loop.add(new Vector2((float) coordinate.x, (float) coordinate.y));
                    }
                    loops.add(loop);
                }
            }
        }
        return loops;
    }

    public void clear() {
//        remove edges;
        points.clear();
    }

    public void closePath() {
//        if (points.size() >= 3)
//            addEdge(points.get(points.size() - 1), points.get(0));
    }
}
