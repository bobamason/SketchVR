package net.masonapps.sketchvr.modeling;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;

import net.masonapps.sketchvr.math.ConversionUtils;

import java.util.List;

import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.Primitive;
import eu.mihosoft.vvecmath.Vector3d;

/**
 * Created by Bob Mason on 6/15/2018.
 */
public class CSG {

    public static SketchNode test(SketchMeshBuilder builder) {
        final Primitive c1 = new Cube(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.xyz(1.0, 1.0, 1.0));
        final Primitive c2 = new Cube(Vector3d.xyz(0.25, 0.5, 0.0), Vector3d.xyz(0.75, 0.75, 1.5));
        final List<Polygon> polygons = c1.toCSG().union(c2.toCSG()).getPolygons();
        builder.begin();
        final MeshPart meshPart = builder.part("csg", GL20.GL_TRIANGLES);
        for (Polygon polygon : polygons) {
            final List<Polygon> triangles = polygon.toTriangles();
            for (Polygon triangle : triangles) {
                if (triangle.vertices.size() == 3) {
                    final Vector3d pos0 = triangle.vertices.get(0).pos;
                    final Vector3d pos1 = triangle.vertices.get(1).pos;
                    final Vector3d pos2 = triangle.vertices.get(2).pos;
                    builder.triangle(ConversionUtils.toVector3(pos0), ConversionUtils.toVector3(pos1), ConversionUtils.toVector3(pos2));
                }
            }
        }
        builder.end();
        if (meshPart.mesh.getNumVertices() > 3)
            return new SketchNode(meshPart);
        else
            return null;
    }
}
