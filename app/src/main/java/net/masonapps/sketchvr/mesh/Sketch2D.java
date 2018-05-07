package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bob Mason on 5/4/2018.
 */
public class Sketch2D {

    public List<Vertex> points = new ArrayList<>();
    private HashMap<Vertex, Vertex> backEdges = new HashMap<>();
    private HashMap<Vertex, Vertex> parents = new HashMap<>();
    private List<List<Vertex>> adj = new ArrayList<>();

    public void addPoint(Vector2 point) {
        final int index = points.size();
        final Vertex vertex = new Vertex(point, index);
        points.add(vertex);
        final ArrayList<Vertex> list = new ArrayList<>();
        if (index > 0) {
            adj.get(index - 1).add(vertex);
            list.add(points.get(index - 1));
        }
        adj.add(list);
        checkSelfIntersection();
    }

    private void checkSelfIntersection() {
        if (points.size() >= 4) {
            final Vector2 intersection = new Vector2();
            final int lastSegmentStart = points.size() - 2;
            final int lastSegmentEnd = points.size() - 1;
            Logger.d("lastSegmentStart = " + lastSegmentStart);
            Vertex p1 = points.get(lastSegmentStart);
            Vertex p2 = points.get(lastSegmentEnd);
            for (int i = 1; i < lastSegmentStart; i++) {
                if (Intersector.intersectSegments(points.get(i - 1).point, points.get(i).point, p1.point, p2.point, intersection)) {
                    Logger.d("self intersection [" + points.get(i - 1).point + ", " + points.get(i).point + " | " + p1.point + ", " + p2.point + "] " + "[" + (i - 1) + ", " + i + " | " + lastSegmentStart + ", " + lastSegmentEnd + "] ");
                    final Vertex vertex = new Vertex(intersection, points.size());
                    final Vertex prevVertex = points.get(i - 1);
                    final Vertex nextVertex = points.get(i);
                    adj.get(prevVertex.index).add(vertex);
                    adj.get(nextVertex.index).add(vertex);
                    adj.get(p1.index).add(vertex);
                    adj.get(p2.index).add(vertex);
                    points.add(vertex);
                    final List<Vertex> list = new ArrayList<>();
                    list.add(prevVertex);
                    list.add(nextVertex);
                    list.add(p1);
                    list.add(p2);
                    adj.add(list);
                }
            }
        }
    }

    public List<List<Vector2>> getLoops() {
        final ArrayList<List<Vector2>> loops = new ArrayList<>();
        dfs();
        for (Vertex u : backEdges.keySet()) {
            final ArrayList<Vector2> loop = new ArrayList<>();
            loops.add(loop);
            Vertex start = backEdges.get(u);
            Vertex v = u;
            do {
                loop.add(v.point);
                v = parents.get(v);
            } while (parents.containsKey(v) && v != start);
        }
        return loops;
    }

    private void dfs() {
        parents.clear();
        backEdges.clear();
        for (Vertex u : points) {
            if (!parents.containsKey(u))
                dfs_visit(u);
        }
        Logger.d("backEdges " + backEdges.size());
    }

    private void dfs_visit(Vertex u) {
        u.inStack = true;
        for (Vertex v : adj.get(u.index)) {
            if (!parents.containsKey(v)) {
                parents.put(v, u);
                dfs_visit(v);
            } else if (v.inStack) {
                backEdges.put(u, v);
                Logger.d("back edge (" + u.index + ", " + v.index + ")");
            }
        }
        u.inStack = false;
    }

    public void clear() {
        points.clear();
        adj.clear();
        backEdges.clear();
        parents.clear();
    }

    public static class Vertex {
        public final Vector2 point;
        private final int index;
        private boolean inStack = false;

        public Vertex(Vector2 point, int index) {
            this.point = point.cpy();
            this.index = index;
        }
    }
}
