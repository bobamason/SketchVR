package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bob Mason on 1/3/2018.
 */

public class Face {

    public final Plane plane = new Plane();
    public Vertex[] vertices = new Vertex[0];
    public int index;
    private boolean needsUpdate = false;

    public Face(Vertex... vertices) {
        this.vertices = new Vertex[vertices.length];
        System.arraycopy(vertices, 0, this.vertices, 0, this.vertices.length);
        update();
    }

    public Face(List<Vertex> vertices) {
        this.vertices = new Vertex[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            this.vertices[i] = vertices.get(i);
        }
    }

    public void addVertex(Vertex vertex) {
        final int length = vertices.length;
        vertices = Arrays.copyOf(vertices, length + 1);
        vertices[length] = vertex;
    }

    public Triangle[] toTriangles() {
        final Triangle[] triangles = new Triangle[vertices.length - 1];
        int ti = 0;
        for (int i = 2; i < vertices.length; i++) {
            final Vertex v1 = new Vertex().set(vertices[0]);
            final Vertex v2 = new Vertex().set(vertices[i - 1]);
            final Vertex v3 = new Vertex().set(vertices[i]);
            triangles[ti] = new Triangle(v1, v2, v3);
            ti++;
        }
        return triangles;
    }

    public BoundingBox extendBounds(BoundingBox bounds) {
        for (Vertex vertex : vertices) {
            bounds.ext(vertex.position);
        }
        return bounds;
    }

    public void update() {
        if (vertices.length < 3) throw new IllegalStateException("face is not valid");
        plane.set(vertices[0].position, vertices[1].position, vertices[2].position);
        clearUpdateFlag();
    }

    public void flagNeedsUpdate() {
        needsUpdate = true;
    }

    public void clearUpdateFlag() {
        needsUpdate = false;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }
}