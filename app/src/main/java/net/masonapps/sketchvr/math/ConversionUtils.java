package net.masonapps.sketchvr.math;

import com.badlogic.gdx.math.Vector3;

import eu.mihosoft.vvecmath.Plane;
import eu.mihosoft.vvecmath.Vector3d;

/**
 * Created by Bob Mason on 6/15/2018.
 */
public class ConversionUtils {

    public static Vector3 toVector3(Vector3d v) {
        return toVector3(v, new Vector3());
    }

    public static Vector3 toVector3(Vector3d v, Vector3 out) {
        out.set((float) v.getY(), (float) v.getY(), (float) v.getZ());
        return out;
    }

    public static com.badlogic.gdx.math.Plane toPlane(Plane jcsgPlane) {
        return toPlane(jcsgPlane, new com.badlogic.gdx.math.Plane());
    }

    public static com.badlogic.gdx.math.Plane toPlane(Plane jcsgPlane, com.badlogic.gdx.math.Plane outPlane) {
        final Vector3d normal = jcsgPlane.getNormal();
        final Vector3d point = jcsgPlane.getAnchor();
        outPlane.set((float) point.getX(), (float) point.getY(), (float) point.getZ(), (float) normal.getX(), (float) normal.getY(), (float) normal.getZ());
        return outPlane;
    }
}
