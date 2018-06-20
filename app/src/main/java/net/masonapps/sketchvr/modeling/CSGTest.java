package net.masonapps.sketchvr.modeling;

import net.masonapps.sketchvr.jcsg.CSG;
import net.masonapps.sketchvr.jcsg.Cube;
import net.masonapps.sketchvr.jcsg.Sphere;

import eu.mihosoft.vvecmath.Transform;

/**
 * Created by Bob Mason on 6/19/2018.
 */
public class CSGTest {

    public static SketchNode cubeUnionSphere() {
        final CSG cube = new Cube().toCSG();
        final CSG sphere = new Sphere().toCSG();
        return new SketchNode(cube.transformed(Transform.unity().translate(0.0, -0.25, 0.0).scale(1.0, 1.0, 2.0)).union(sphere).transformed(Transform.unity().translate(0.25, 0.0, 0.0)).getPolygons());
    }
}
