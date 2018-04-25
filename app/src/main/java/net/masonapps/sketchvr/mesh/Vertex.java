package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.Arrays;

/**
 * Created by Bob Mason on 5/11/2017.
 */

public class Vertex {
    public final Vector3 position = new Vector3();
    public final Vector3 normal = new Vector3();
    public final Vector2 uv = new Vector2();
    public Color color = Color.GRAY.cpy();
    public int index = -1;
    public Triangle[] triangles = new Triangle[0];
    public Vertex[] adjacentVertices = new Vertex[0];
    public volatile int flag = 0;
    private Edge[] edges = new Edge[0];

    public Vertex() {
    }

    private static boolean isDuplicateVertex(Vertex[] vertices, Vertex vertex) {
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].index == vertex.index)
                return true;
        }
        return false;
    }

    public void addTriangle(Triangle triangle) {
        final int length = triangles.length;
        triangles = Arrays.copyOf(triangles, length + 1);
        triangles[length] = triangle;
    }

    public Vertex set(Vertex vertex) {
        position.set(vertex.position);
        normal.set(vertex.normal);
        uv.set(vertex.uv);
        color.set(vertex.color);
        index = vertex.index;
        triangles = Arrays.copyOf(vertex.triangles, vertex.triangles.length);
        edges = Arrays.copyOf(vertex.edges, vertex.edges.length);
        return this;
    }

    public Vertex lerp(Vertex vertex, float t) {
        position.lerp(vertex.position, t);
        normal.lerp(vertex.normal, t);
        uv.lerp(vertex.uv, t);
        color.lerp(vertex.color, t);
        return this;
    }

    public Vertex recalculateNormal() {
        normal.set(0, 0, 0);
        for (Triangle triangle : triangles) {
            normal.add(triangle.plane.normal).scl(triangle.getWeight(this));
        }
        normal.nor();
        return this;
    }

    public void addEdge(Edge edge) {
        final int length = edges.length;
        edges = Arrays.copyOf(edges, length + 1);
        edges[length] = edge;
        updateAdjacentVertices();
    }

    public Vertex[] getAdjacentVertices() {
        return adjacentVertices;
    }

    private void updateAdjacentVertices() {
        adjacentVertices = new Vertex[0];
        for (Edge edge : edges) {
            final Vertex v1 = edge.v1;
            final Vertex v2 = edge.v2;
            if (v1 != this && !isDuplicateVertex(adjacentVertices, v1)) {
                final int length = adjacentVertices.length;
                adjacentVertices = Arrays.copyOf(adjacentVertices, length + 1);
                adjacentVertices[length] = v1;
            }
            if (v2 != this && !isDuplicateVertex(adjacentVertices, v2)) {
                final int length = adjacentVertices.length;
                adjacentVertices = Arrays.copyOf(adjacentVertices, length + 1);
                adjacentVertices[length] = v2;
            }
        }
    }
}
