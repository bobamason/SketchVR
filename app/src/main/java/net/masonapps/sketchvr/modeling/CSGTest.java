package net.masonapps.sketchvr.modeling;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.sketchvr.jcsg.CSG;
import net.masonapps.sketchvr.jcsg.Cube;
import net.masonapps.sketchvr.jcsg.Sphere;
import net.masonapps.sketchvr.math.ConversionUtils;

import eu.mihosoft.vvecmath.Transform;

/**
 * Created by Bob Mason on 6/19/2018.
 */
public class CSGTest {

    public static SketchNode cubeUnionSphere(SketchMeshBuilder builder) {
        final CSG cube = new Cube().toCSG();
        final CSG sphere = new Sphere().toCSG();
        return csgToSketchNode(builder, cube.transformed(Transform.unity().translate(0.0, -0.25, 0.0).scale(1.0, 1.0, 2.0)).union(sphere).transformed(Transform.unity().translate(0.25, 0.0, 0.0)));
    }

    private static SketchNode csgToSketchNode(SketchMeshBuilder builder, CSG csg) {
        builder.begin();
        final MeshPart meshPart = builder.part("csg", GL20.GL_TRIANGLES);
        csg.getPolygons()
                .forEach(p -> p.toTriangles().stream()
                        .filter(tri -> tri.isValid() && tri.vertices.size() == 3)
                        .forEach(tri -> {
                            final Vector3 p1 = ConversionUtils.toVector3(tri.vertices.get(0).pos);
                            final Vector3 p2 = ConversionUtils.toVector3(tri.vertices.get(1).pos);
                            final Vector3 p3 = ConversionUtils.toVector3(tri.vertices.get(2).pos);
                            builder.triangle(p1, p2, p3);
                        }));
        builder.end();
        if (meshPart.mesh.getNumVertices() > 3)
            return new SketchNode(meshPart);
        else
            return null;
    }
}
