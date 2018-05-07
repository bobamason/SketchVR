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

    public List<Vector2> points = new ArrayList<>();
    private Graph<Vector2, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

    public void addPoint(Vector2 point) {
        final int index = points.size();
        final Vector2 vertex = point.cpy();
        points.add(vertex);
        graph.addVertex(vertex);
        if (index > 0) {
            graph.addEdge(points.get(index - 1), vertex);
        }
//        checkSelfIntersection();
    }

    private void checkSelfIntersection() {
        if (points.size() >= 4) {
            final Vector2 intersection = new Vector2();
            final int lastSegmentStart = points.size() - 2;
            final int lastSegmentEnd = points.size() - 1;
            Logger.d("lastSegmentStart = " + lastSegmentStart);
            Vector2 p1 = points.get(lastSegmentStart);
            Vector2 p2 = points.get(lastSegmentEnd);
            for (int i = 1; i < lastSegmentStart; i++) {
                if (Intersector.intersectSegments(points.get(i - 1), points.get(i), p1, p2, intersection)) {
                    Logger.d("self intersection [" + points.get(i - 1) + ", " + points.get(i) + " | " + p1 + ", " + p2 + "] " + "[" + (i - 1) + ", " + i + " | " + lastSegmentStart + ", " + lastSegmentEnd + "] ");
                    // TODO: 5/7/2018 insert vertex 
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
