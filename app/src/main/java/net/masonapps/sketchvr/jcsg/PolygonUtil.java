package net.masonapps.sketchvr.jcsg;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import eu.mihosoft.vvecmath.Vector3d;

/**
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class PolygonUtil {

    private PolygonUtil() {
        throw new AssertionError("Don't instantiate me!", null);
    }

    /**
     * Converts a CSG polygon to a poly2tri polygon (including holes)
     *
     * @param polygon the polygon to convert
     * @return a CSG polygon to a poly2tri polygon (including holes)
     */
    private static org.poly2tri.geometry.polygon.Polygon fromCSGPolygon(
            Polygon polygon) {

        // convert polygon
        List<PolygonPoint> points = new ArrayList<>();
        for (Vertex v : polygon.vertices) {
            PolygonPoint vp = new PolygonPoint(v.pos.x(), v.pos.y(), v.pos.z());
            points.add(vp);
        }

        org.poly2tri.geometry.polygon.Polygon result
                = new org.poly2tri.geometry.polygon.Polygon(points);

        // convert holes
        Optional<List<Polygon>> holesOfPresult
                = polygon.
                getStorage().getValue(Edge.KEY_POLYGON_HOLES);
        if (holesOfPresult.isPresent()) {
            List<Polygon> holesOfP = holesOfPresult.get();

            holesOfP.stream().forEach((hP) -> {
                result.addHole(fromCSGPolygon(hP));
            });
        }

        return result;
    }

    public static List<Polygon> concaveToConvex(
            Polygon concave) {

        List<Polygon> result = new ArrayList<>();

        Vector3d normal = concave.vertices.get(0).normal.clone();

        boolean cw = !Extrude.isCCW(concave);

        org.poly2tri.geometry.polygon.Polygon p
                = fromCSGPolygon(concave);

        Poly2Tri.triangulate(p);

        List<DelaunayTriangle> triangles = p.getTriangles();

        List<Vertex> triPoints = new ArrayList<>();

        for (DelaunayTriangle t : triangles) {

            int counter = 0;
            for (TriangulationPoint tp : t.points) {

                triPoints.add(new Vertex(
                        Vector3d.xyz(tp.getX(), tp.getY(), tp.getZ()),
                        normal));

                if (counter == 2) {
                    if (!cw) {
                        Collections.reverse(triPoints);
                    }
                    Polygon poly =
                            new Polygon(
                                    triPoints, concave.getStorage());
                    result.add(poly);
                    counter = 0;
                    triPoints = new ArrayList<>();

                } else {
                    counter++;
                }
            }
        }

        return result;
    }
}