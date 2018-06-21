package net.masonapps.sketchvr.modeling;

import net.masonapps.sketchvr.jcsg.CSG;
import net.masonapps.sketchvr.jcsg.Cube;
import net.masonapps.sketchvr.jcsg.Cylinder;
import net.masonapps.sketchvr.jcsg.Polygon;
import net.masonapps.sketchvr.jcsg.Primitive;

import org.masonapps.libgdxgooglevr.utils.ElapsedTimer;

import java.util.List;

import eu.mihosoft.vvecmath.Transform;

/**
 * Created by Bob Mason on 6/19/2018.
 */
public class CSGTest {

    public static SketchNode cubeWithHole() {
        final Primitive primitive1 = new Cube();
        final Primitive primitive2 = new Cylinder();
        final ElapsedTimer et = ElapsedTimer.getInstance();
        et.start("csg test total");
        et.start("create csg");
        final CSG csg1 = primitive1.toCSG();
        final CSG csg2 = primitive2.toCSG();
        et.print("create csg");

        et.start("transform and copy csg");
        final CSG csg1Transformed = csg1.transformed(Transform.unity().scale(2.0, 0.25, 1.0));
        final CSG csg2Transformed = csg2.transformed(Transform.unity().translate(-0.5, 0.0, 0.0).scale(0.25, 0.5, 0.25f));
        final CSG csg2Transformed2 = csg2.transformed(Transform.unity().translate(0.5, 0.0, 0.0).scale(0.25, 0.5, 0.25f));
        et.print("transform and copy csg");

        et.start("create holes and extract polygons");
        final List<Polygon> polygons = csg1Transformed.difference(csg2Transformed, csg2Transformed2).getPolygons();
        et.print("create holes and extract polygons");

        et.start("create node");
        final SketchNode sketchNode = new SketchNode(polygons);
        et.print("create node");
        et.print("csg test total");
        return sketchNode;
    }
}
