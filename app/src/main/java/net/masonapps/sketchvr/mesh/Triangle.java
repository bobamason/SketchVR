package net.masonapps.sketchvr.mesh;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;

/**
 * Created by Bob Mason on 5/11/2017.
 */

public class Triangle implements AABBTree.AABBObject {

    public final Plane plane = new Plane();
    public final Vertex v1;
    public final Vertex v2;
    public final Vertex v3;
    public final Edge e1;
    public final Edge e2;
    public final Edge e3;
    private boolean needsUpdate = false;
    private BoundingBox aabb = new BoundingBox();
    private AABBTree.Node node = null;

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        v1.addTriangle(this);
        v2.addTriangle(this);
        v3.addTriangle(this);
        e1 = new Edge(v1, v2);
        e2 = new Edge(v2, v3);
        e3 = new Edge(v3, v1);
        update();
    }

    public void update() {
        aabb.inf();
        aabb.ext(v1.position);
        aabb.ext(v2.position);
        aabb.ext(v3.position);
        plane.set(v1.position, v2.position, v3.position);
        if (node != null)
            node.refit();
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

    @Nullable
    @Override
    public AABBTree.Node getNode() {
        return node;
    }

    @Override
    public void setNode(@Nullable AABBTree.Node node) {
        this.node = node;
    }

    @Override
    public BoundingBox getAABB() {
        return aabb;
    }

    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        final boolean intersectRayTriangle = Intersector.intersectRayTriangle(ray, v3.position, v2.position, v1.position, intersection.hitPoint);
        if (intersectRayTriangle)
            intersection.object = this;
        return intersectRayTriangle;
    }
}