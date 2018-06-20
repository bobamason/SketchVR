package net.masonapps.sketchvr.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.sketchvr.jcsg.Polygon;
import net.masonapps.sketchvr.math.ConversionUtils;
import net.masonapps.sketchvr.math.RayTestUtil;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;

/**
 * Created by Bob Mason on 6/20/2018.
 */
public class PolygonAABBObject implements AABBTree.AABBObject {


    private final Polygon polygon;
    private AABBTree.Node node;
    private BoundingBox aabb = new BoundingBox();

    public PolygonAABBObject(Polygon polygon) {
        this.polygon = polygon;
    }

    public Polygon getPolygon() {
        return polygon;
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
        return aabb.set(ConversionUtils.toVector3(polygon.getBounds().getMin(), aabb.min), ConversionUtils.toVector3(polygon.getBounds().getMax(), aabb.max));
    }

    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        final boolean rayTest = RayTestUtil.intersectRayPolygon(ray, polygon, intersection.hitPoint);
        if (rayTest) {
            intersection.object = this;
            intersection.t = ray.origin.dst(intersection.hitPoint);
        }
        return rayTest;
    }
}
