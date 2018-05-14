package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 5/4/2018.
 */
public class Sketch2D {

    private static final float EPSILON = 1e-3f;
    public List<Vector2> points = new ArrayList<>();
    public List<Vector2> tmpPoints = new ArrayList<>();
    private Graph<Vector2, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

    public void addPoint(Vector2 point) {
        final int index = points.size();
        final Vector2 vertex = point.cpy();
        points.add(vertex);
        graph.addVertex(vertex);
        if (index > 0) {
            graph.addEdge(points.get(index - 1), vertex);
        }
        checkSelfIntersection();
    }

    private void checkSelfIntersection() {
        if (points.size() >= 4) {
            final Vector2 intersection = new Vector2();
            final int lastSegmentStart = points.size() - 2;
            final int lastSegmentEnd = points.size() - 1;
            Logger.d("lastSegmentStart = " + lastSegmentStart);
            Vector2 segStart = points.get(lastSegmentStart);
            Vector2 segEnd = points.get(lastSegmentEnd);
            tmpPoints.clear();
            for (int i = 0; i < lastSegmentStart; i++) {
                tmpPoints.add(i, points.get(i));
            }
            for (int i = 1; i < tmpPoints.size(); i++) {
                final Vector2 p1 = points.get(i - 1);
                final Vector2 p2 = points.get(i);
                if (Intersector.intersectSegments(p1, p2, segStart, segEnd, intersection)) {
                    Logger.d("self intersection [" + p1 + ", " + p2 + " | " + segStart + ", " + segEnd + "] " + "[" + (i - 1) + ", " + i + " | " + lastSegmentStart + ", " + lastSegmentEnd + "] ");

                    if (intersection.epsilonEquals(segStart, EPSILON)) {
                        graph.addEdge(p1, segStart);
                        graph.addEdge(segStart, p2);
                    } else if (intersection.epsilonEquals(segEnd, EPSILON)) {
                        graph.addEdge(p1, segEnd);
                        graph.addEdge(segEnd, p2);
                    } else if (intersection.epsilonEquals(p1, EPSILON)) {
                        graph.addEdge(segStart, p1);
                        graph.addEdge(p1, segEnd);
                    } else if (intersection.epsilonEquals(p2, EPSILON)) {
                        graph.addEdge(segStart, p2);
                        graph.addEdge(p2, segEnd);
                    } else {
                        final Vector2 v = intersection.cpy();
                        points.add(i, v);
                        graph.addVertex(v);
                        graph.addEdge(p1, v);
                        graph.addEdge(v, p2);
                        graph.addEdge(segStart, v);
                        graph.addEdge(v, segEnd);
                    }
                }
            }
        }
    }

    public List<List<Vector2>> getLoops() {
        return new PatonCycleBase<>(graph).findCycleBase();
    }

    public void clear() {
        points.clear();
    }

    public void closePath() {
        if (points.size() >= 3)
            graph.addEdge(points.get(points.size() - 1), points.get(0));
    }
}
