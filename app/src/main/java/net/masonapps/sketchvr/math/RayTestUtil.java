package net.masonapps.sketchvr.math;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.sketchvr.jcsg.Polygon;

import eu.mihosoft.vvecmath.Vector3d;

/**
 * Created by Bob Mason on 6/20/2018.
 */
public class RayTestUtil {

    public static boolean intersectRayPolygon(Ray ray, Polygon polygon, Vector3 hitPoint) {
        final Plane tmpPlane = Pools.obtain(Plane.class);
        boolean intersects = false;
        if (Intersector.intersectRayPlane(ray, ConversionUtils.toPlane(polygon.getPlane(), tmpPlane), hitPoint)) {
            intersects = polygon.contains(Vector3d.xyz(hitPoint.x, hitPoint.y, hitPoint.z));
        }
        Pools.free(tmpPlane);
        return intersects;
    }
}
