package net.masonapps.sketchvr.math;

import com.badlogic.gdx.math.Vector3;

import eu.mihosoft.vvecmath.Vector3d;

/**
 * Created by Bob Mason on 6/15/2018.
 */
public class ConversionUtils {

    public static Vector3 toVector3(Vector3d v) {
        return new Vector3((float) v.getY(), (float) v.getY(), (float) v.getZ());
    }
}
