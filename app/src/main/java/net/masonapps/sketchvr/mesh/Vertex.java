package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ShortArray;

import java.util.Arrays;

/**
 * Created by Bob Mason on 5/11/2017.
 */

public class Vertex {
    public final Vector3 position = new Vector3();
    public ShortArray indices = new ShortArray();
    public Triangle[] triangles = new Triangle[0];
    public Vertex[] adjacentVertices = new Vertex[0];
    private Edge[] edges = new Edge[0];

    public Vertex() {
    }

    private static boolean isDuplicateVertex(Vertex[] vertices, Vertex vertex) {
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i] == vertex)
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
        indices.clear();
        indices.addAll(vertex.indices);
        triangles = Arrays.copyOf(vertex.triangles, vertex.triangles.length);
        edges = Arrays.copyOf(vertex.edges, vertex.edges.length);
        return this;
    }

    public Vertex lerp(Vertex vertex, float t) {
        position.lerp(vertex.position, t);
        return this;
    }

    public void addIndex(int index) {
        if (!indices.contains((short) index))
            indices.add(index);
    }

    public void addEdge(Edge edge) {
        final int length = edges.length;
        edges = Arrays.copyOf(edges, length + 1);
        edges[length] = edge;
        final Vertex v1 = edge.v1;
        final Vertex v2 = edge.v2;
        if (v1 != this && !isDuplicateVertex(adjacentVertices, v1)) {
            addAdjacentVertex(v1);
        }
        if (v2 != this && !isDuplicateVertex(adjacentVertices, v2)) {
            addAdjacentVertex(v2);
        }
    }

    public Vertex[] getAdjacentVertices() {
        return adjacentVertices;
    }

    private void addAdjacentVertex(Vertex v) {
        final int length = adjacentVertices.length;
        adjacentVertices = Arrays.copyOf(adjacentVertices, length + 1);
        adjacentVertices[length] = v;
    }
}
