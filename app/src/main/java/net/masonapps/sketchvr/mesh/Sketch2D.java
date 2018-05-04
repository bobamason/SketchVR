package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob Mason on 5/4/2018.
 */
public class Sketch2D {

    private Array<Vertex> points = new Array<>();

    public void addPoint(Vector2 point) {
        final Vertex vertex = new Vertex(point);
        vertex.edge = new Edge();
        if (points.size > 1) {
            final Vertex prevVertex = points.get(points.size - 1);
            prevVertex.edge.next = vertex.edge;
            prevVertex.edge.nextVertex = vertex;
        }
        points.add(vertex);
        checkSelfIntersection();
    }

    private void checkSelfIntersection() {
        if (points.size >= 4) {
            final Vector2 intersection = new Vector2();
            Vector2 p1 = points.get(points.size - 2).point;
            Vector2 p2 = points.get(points.size - 1).point;
            for (int i = 1; i < points.size - 2; i++) {
                if (Intersector.intersectLines(points.get(i - 1).point, points.get(i).point, p1, p2, intersection)) {
                    final Vertex vertex = new Vertex(intersection);
                    final Vertex prevVertex = points.get(i - 1);
                    prevVertex.edge.next = vertex.edge;
                    prevVertex.edge.nextVertex = vertex;
                    vertex.edge = new Edge();
                    final Vertex nextVertex = points.get(i);
                    vertex.edge.next = nextVertex.edge;
                    vertex.edge.nextVertex = nextVertex;
                    points.insert(i, vertex);
                }
            }
        }
    }

    private static class Edge {
        Vertex nextVertex;
        Loop loop;
        Edge next;
    }

    private static class Vertex {
        Vector2 point = new Vector2();
        Edge edge;

        Vertex(Vector2 point) {
            this.point.set(point);
        }
    }

    private static class Loop {
        Edge edge;
    }
}
