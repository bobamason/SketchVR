package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Bob Mason on 3/22/2018.
 */

public class MeshInfoCreator {

    private final Vector3 d = new Vector3();
    private final Vector3 pos = new Vector3();
    private final Vector3 nor = new Vector3();
    private final FloatArray vertices = new FloatArray();
    private final ShortArray indices = new ShortArray();

    public MeshInfoCreator() {
    }

    public void sweep(Polygon3D polygon, Path<Vector3> path, int sections) {
        final float step = 1f / sections;
        path.valueAt(pos, 0f);
        path.derivativeAt(d, 0f);
        for (int i = 0; i < sections; i++) {

        }
        path.derivativeAt(d, 1f);
    }

    public void extrude(Polygon3D polygon, Vector3 dir, float t) {
        if (polygon.plane.normal.dot(dir) < 0f)
            polygon.flip();
    }

    public static class Polygon3D {

        private final Plane plane;
        private Vector3[] vertices;

        public Polygon3D(Vector3[] vertices) {
            this.vertices = vertices;
            plane = new Plane();
            update();
        }

        public void update() {
            if (vertices.length < 3) throw new IllegalStateException("face is not valid");
            plane.set(vertices[0], vertices[1], vertices[2]);
        }

        public void flip() {
            final Vector3[] tmp = new Vector3[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                tmp[vertices.length - 1 - i] = vertices[i];
            }
            vertices = tmp;
            update();
        }
    }
}
