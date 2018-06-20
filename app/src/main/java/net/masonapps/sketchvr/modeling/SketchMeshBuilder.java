package net.masonapps.sketchvr.modeling;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import org.locationtech.jts.geom.Coordinate;
import org.masonapps.libgdxgooglevr.math.PlaneUtils;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bob Mason on 5/3/2018.
 */
public class SketchMeshBuilder extends MeshBuilder {

    private static SketchMeshBuilder instance = null;

    public static SketchMeshBuilder getInstance() {
        if (instance == null)
            instance = new SketchMeshBuilder();
        return instance;
    }
    private final static Vector3 vTmp = new Vector3();
    private final static Vector3 dir = new Vector3();
    private final static Vector3 dir2 = new Vector3();
    private final static Plane plane1 = new Plane();
    private final static Plane plane2 = new Plane();
    private final static Array<Vector3> tmpVecs = new Array<>();
    private final static Array<Vector2> topPath = new Array<>();
    private final static Array<Vector2> bottomPath = new Array<>();
    private final static Vector2 p1 = new Vector2();
    private final static Vector2 p2 = new Vector2();
    private final static Vector2 p3 = new Vector2();
    private final static Vector2 p4 = new Vector2();
    private final static Vector3 v1 = new Vector3();
    private final static Vector3 v2 = new Vector3();
    private final static Vector3 v3 = new Vector3();
    private final static Vector3 v4 = new Vector3();
    private final static Vector3 offset = new Vector3();
    private final static Vector3 offset2 = new Vector3();
    private final static Vector2 off1 = new Vector2();
    private final static Vector2 off2 = new Vector2();
    private final static Vector2 intersectV2 = new Vector2();
    private final static Vector3 normal = new Vector3();
    private final static FloatArray tmpVertices = new FloatArray();
    private final static ShortArray tmpIndices = new ShortArray();
    private final MeshPartBuilder.VertexInfo vertTmp1 = new MeshPartBuilder.VertexInfo();
    private final MeshPartBuilder.VertexInfo vertTmp2 = new MeshPartBuilder.VertexInfo();
    private final MeshPartBuilder.VertexInfo vertTmp3 = new MeshPartBuilder.VertexInfo();
    private final MeshPartBuilder.VertexInfo vertTmp4 = new MeshPartBuilder.VertexInfo();
    private EarClippingTriangulator triangulator = new EarClippingTriangulator();

    public void begin() {
        super.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal()), GL20.GL_TRIANGLES);
    }

    public void triangle(float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3) {
        normal.set(x1, y1, z1).sub(x2, y2, z2).crs(x2 - x3, y2 - y3, z2 - z3).nor();
        triangle(vertTmp1.set(null, null, null, null).setPos(x1, y1, z1).setNor(normal.x, normal.y, normal.z),
                vertTmp2.set(null, null, null, null).setPos(x2, y2, z2).setNor(normal.x, normal.y, normal.z),
                vertTmp3.set(null, null, null, null).setPos(x3, y3, z3).setNor(normal.x, normal.y, normal.z));
    }

    public void rect(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01) {
        normal.set(corner00).sub(corner10).crs(corner10.x - corner11.x, corner10.y - corner11.y, corner10.z - corner11.z).nor();
        rect(vertTmp1.set(corner00, normal, null, null).setUV(0f, 1f), vertTmp2.set(corner10, normal, null, null).setUV(1f, 1f),
                vertTmp3.set(corner11, normal, null, null).setUV(1f, 0f), vertTmp4.set(corner01, normal, null, null).setUV(0f, 0f));
    }

    public void rect(float x00, float y00, float z00,
                     float x10, float y10, float z10,
                     float x11, float y11, float z11,
                     float x01, float y01, float z01) {
        normal.set(x00, y00, z00).sub(x10, y10, z10).crs(x10 - x11, y10 - y11, z10 - z11).nor();
        rect(vertTmp1.set(null, null, null, null).setPos(x00, y00, z00).setNor(normal.x, normal.y, normal.z).setUV(0f, 1f),
                vertTmp2.set(null, null, null, null).setPos(x10, y10, z10).setNor(normal.x, normal.y, normal.z).setUV(1f, 1f),
                vertTmp3.set(null, null, null, null).setPos(x11, y11, z11).setNor(normal.x, normal.y, normal.z).setUV(1f, 0f),
                vertTmp4.set(null, null, null, null).setPos(x01, y01, z01).setNor(normal.x, normal.y, normal.z).setUV(0f, 0f));
    }

    @Override
    public void triangle(Vector3 p1, Vector3 p2, Vector3 p3) {
        normal.set(p1).sub(p2).crs(p2.x - p3.x, p2.y - p3.y, p2.z - p3.z).nor();
        triangle(vertTmp1.set(p1, normal, null, null), vertTmp2.set(p2, normal, null, null), vertTmp3.set(p3, normal, null, null));
    }

    public void triangle2sided(Vector3 p1, Vector3 p2, Vector3 p3) {
        normal.set(p1).sub(p2).crs(p2.x - p3.x, p2.y - p3.y, p2.z - p3.z).nor();
        triangle(vertTmp1.set(p1, normal, null, null), vertTmp2.set(p2, normal, null, null), vertTmp3.set(p3, normal, null, null));
        normal.scl(-1);
        triangle(vertTmp1.set(p3, normal, null, null), vertTmp2.set(p2, normal, null, null), vertTmp3.set(p1, normal, null, null));
    }

    public void triangle2sided(float x1, float y1, float z1,
                               float x2, float y2, float z2,
                               float x3, float y3, float z3) {
        triangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        triangle(x3, y3, z3, x2, y2, z2, x1, y1, z1);
    }

    public void rect2sided(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01) {
        rect(corner00, corner10, corner11, corner01);
        rect(corner01, corner11, corner10, corner00);
    }

    public void rect2sided(float x00, float y00, float z00,
                           float x10, float y10, float z10,
                           float x11, float y11, float z11,
                           float x01, float y01, float z01) {
        rect(x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
        rect(x01, y01, z01, x11, y11, z11, x10, y10, z10, x00, y00, z00);
    }

    private static boolean areVerticesClockwise(float[] vertices, int offset, int count) {
        if (count <= 2) return false;
        float area = 0, p1x, p1y, p2x, p2y;
        for (int i = offset, n = offset + count - 3; i < n; i += 2) {
            p1x = vertices[i];
            p1y = vertices[i + 1];
            p2x = vertices[i + 2];
            p2y = vertices[i + 3];
            area += p1x * p2y - p2x * p1y;
        }
        p1x = vertices[offset + count - 2];
        p1y = vertices[offset + count - 1];
        p2x = vertices[offset];
        p2y = vertices[offset + 1];
        return area + p1x * p2y - p2x * p1y < 0;
    }

    public void polygon(FloatArray vertices, Plane plane) {
        if (vertices.size < 6) return;
        final ShortArray triIndices = triangulator.computeTriangles(vertices);
        tmpIndices.clear();
        for (int i = 0; i < vertices.size; i += 2) {
            p1.set(vertices.get(i), vertices.get(i + 1));
            PlaneUtils.toSpace(plane, p1, vTmp);
            tmpIndices.add(vertex(vertTmp1.set(vTmp, plane.normal, null, null)));
        }
        tmpIndices.reverse();
        ensureIndices(triIndices.size);
        for (int i = 0; i < triIndices.size; i++) {
            index(tmpIndices.get(triIndices.get(i)));
        }
    }

    public void polygonExtruded(FloatArray vertices, Plane plane, float depth) {
        if (vertices.size < 6) return;
        final ShortArray triIndices = triangulator.computeTriangles(vertices);
        tmpIndices.clear();
        tmpVecs.clear();
        for (int i = 0; i < vertices.size; i += 2) {
            p1.set(vertices.get(i), vertices.get(i + 1));
            PlaneUtils.toSpace(plane, p1, vTmp);
            vertTmp1.set(vTmp, plane.normal, null, null);
            vertTmp1.position.set(plane.normal).scl(-depth / 2f).add(vTmp);
            vertTmp1.normal.scl(-1);
            tmpVecs.add(vertTmp1.position.cpy());
            tmpIndices.add(vertex(vertTmp1));
        }
        ensureIndices(triIndices.size * 2);
        for (int i = 0; i < triIndices.size; i++) {
            index(tmpIndices.get(triIndices.get(i)));
        }
        tmpIndices.clear();
        int n = tmpVecs.size;
        for (int i = 0; i < n; i++) {
            vTmp.set(tmpVecs.get(i));
            vertTmp1.set(vTmp, plane.normal, null, null);
            vertTmp1.position.set(plane.normal).scl(depth / 2f).add(vTmp);
            tmpVecs.add(vertTmp1.position.cpy());
            tmpIndices.add(vertex(vertTmp1));
        }
        triIndices.reverse();
        for (int i = 0; i < triIndices.size; i++) {
            index(tmpIndices.get(triIndices.get(i)));
        }

        final int halfVerts = tmpVecs.size / 2;
        if (areVerticesClockwise(vertices.items, 0, vertices.size)) {
            for (int i = 0; i < halfVerts - 1; i++)
                rect(tmpVecs.get(i + halfVerts), tmpVecs.get(i + halfVerts + 1), tmpVecs.get(i + 1), tmpVecs.get(i));
        } else {
            for (int i = 0; i < halfVerts - 1; i++)
                rect(tmpVecs.get(i), tmpVecs.get(i + 1), tmpVecs.get(i + halfVerts + 1), tmpVecs.get(i + halfVerts));
        }
    }

    /**
     * @param points    first half are start points, second half are end points
     * @param clockwise direction of the polygon on the start of the extrude
     */
    public void extrude(Array<Vector3> points, boolean clockwise) {
        final int halfVerts = points.size / 2;
        if (clockwise) {
            for (int i = 0; i < halfVerts - 1; i++)
                rect(points.get(i + halfVerts), points.get(i + halfVerts + 1), points.get(i + 1), points.get(i));
        } else {
            for (int i = 0; i < halfVerts - 1; i++)
                rect(points.get(i), points.get(i + 1), points.get(i + halfVerts + 1), points.get(i + halfVerts));
        }
    }

    public void sweep(FloatArray polygon, Array<Vector3> path, boolean continuous) {
        final int n = path.size;
        if (n < 2 || polygon.size < 6) return;
        final Array<Ray> rays = new Array<>();
        final Array<Vector3> start = new Array<>();
        final Array<Vector3> end = new Array<>();
        tmpVecs.clear();
        final boolean capEnds = !continuous;
        final boolean clockwise = areVerticesClockwise(polygon.items, 0, polygon.size);
        if (capEnds) {
            dir.set(path.get(0)).sub(path.get(1)).nor();
            plane1.set(path.get(0), dir);
            polygon(polygon, plane1);
            plane1.normal.scl(-1);
            for (int j = 0; j < polygon.size; j += 2) {
                p1.set(polygon.get(j), polygon.get(j + 1));
                PlaneUtils.toSpace(plane1, p1, vTmp);
                start.add(vTmp.cpy());
            }
        } else {
            dir.set(path.get(0)).sub(path.get(n - 1)).nor();
            plane1.set(path.get(0), dir);
            dir2.set(path.get(1)).sub(path.get(0)).nor();
            plane2.set(path.get(1), dir.add(dir2).scl(0.5f));
            for (int j = 0; j < polygon.size; j += 2) {
                p1.set(polygon.get(j), polygon.get(j + 1));
                PlaneUtils.toSpace(plane1, p1, vTmp);
                rays.add(new Ray(vTmp, plane1.normal));
            }

            start.clear();
            for (int j = 0; j < rays.size; j++) {
                Intersector.intersectRayPlane(rays.get(j), plane2, vTmp);
                start.add(vTmp.cpy());
            }
        }
        for (int i = 0; i < n; i++) {
            if (capEnds)
                if (i == 0 || i == n - 1)
                    continue;
            final int iPrev = (i - 1) % n;
            final int iNext = (i + 1) % n;
            final Vector3 prev = path.get(iPrev);
            final Vector3 curr = path.get(i);
            final Vector3 next = path.get(iNext);
            // calculate rays to project onto current plane
            dir.set(curr).sub(prev).nor();
            plane1.set(prev, dir);
            rays.clear();
            for (int j = 0; j < polygon.size; j += 2) {
                p1.set(polygon.get(j), polygon.get(j + 1));
                PlaneUtils.toSpace(plane1, p1, vTmp);
                rays.add(new Ray(vTmp, plane1.normal));
            }

            dir2.set(next).sub(curr).nor();
            plane2.set(next, dir.add(dir2).scl(0.5f));

            end.clear();
            for (int j = 0; j < rays.size; j++) {
                final Ray ray = rays.get(j);
                final Vector3 s = start.get(j);
                Intersector.intersectRayPlane(ray, plane2, vTmp.set(s));
                end.add(vTmp.cpy());
            }
            extrude(start, end, clockwise);
            start.clear();
            start.addAll(end);
        }
        if (capEnds) {
            dir2.set(path.get(n - 1)).sub(path.get(n - 2)).nor();
            plane2.set(path.get(n - 1), dir2);
            polygon(polygon, plane2);
            for (int j = 0; j < polygon.size; j += 2) {
                p2.set(polygon.get(j), polygon.get(j + 1));
                PlaneUtils.toSpace(plane2, p2, vTmp);
                end.add(vTmp.cpy());
            }

            extrude(start, end, clockwise);
        }
    }

    private void extrude(Array<Vector3> start, Array<Vector3> end, boolean clockwise) {
        tmpVecs.clear();
        tmpVecs.ensureCapacity(start.size + end.size);
        tmpVecs.addAll(start);
        tmpVecs.addAll(end);
        extrude(tmpVecs, clockwise);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void drawStrokePath(List<Vector2> path, Plane plane, float strokeWidth, boolean continuous) {
        final int n = path.size();
        if (n < 2) return;
        topPath.clear();
        bottomPath.clear();
        tmpVertices.clear();
        final float hw = strokeWidth / 2;
        // path start
        off1.set(path.get(1)).sub(path.get(0));
        off1.nor().scl(hw).rotate90(1);
        p1.set(off1).add(path.get(0));
        // path start top
        topPath.add(p1.cpy());
        // path start bottom
        p1.set(off1).scl(-1).add(path.get(0));
        bottomPath.add(p1.cpy());
        for (int i = 1; i < n - 1; i++) {
            final Vector2 prev = path.get(i - 1);
            final Vector2 curr = path.get(i);
            final Vector2 next = path.get(i + 1);
            //  top
            //      first segment
            off1.set(curr).sub(prev);
            off1.nor().scl(hw).rotate90(1);
            //      first point of first segment
            p1.set(off1).add(prev);
            //      second point of first segment
            p2.set(off1).add(curr);

            //      second segment
            off2.set(next).sub(curr);
            off2.nor().scl(hw).rotate90(1);
            //      first point of second segment
            p3.set(off2).add(curr);
            //      second point of second segment
            p4.set(off2).add(next);
            if (off1.isCollinear(off2)) {
                topPath.add(p2.cpy());
            } else if (Intersector.intersectLines(p1, p2, p3, p4, intersectV2)) {
                topPath.add(intersectV2.cpy());
            } else {
                topPath.add(p2.cpy());
            }

            //  bottom
            //      first point of first segment
            p1.set(off1).scl(-1).add(prev);
            //      second point of first segment
            p2.set(off1).scl(-1).add(curr);
            //      first point of second segment
            p3.set(off2).scl(-1).add(curr);
            //      second point of second segment
            p4.set(off2).scl(-1).add(next);
            if (off1.isCollinear(off2)) {
                bottomPath.add(p2.cpy());
            } else if (Intersector.intersectLines(p1, p2, p3, p4, intersectV2)) {
                bottomPath.add(intersectV2.cpy());
            } else {
                bottomPath.add(p2.cpy());
            }
        }
        // path end
        off1.set(path.get(n - 1)).sub(path.get(n - 2));
        off1.nor().scl(hw).rotate90(1);
        p1.set(off1).add(path.get(n - 1));
        // path end top
        topPath.add(p1.cpy());
        // path end bottom
        p1.set(off1).scl(-1).add(path.get(n - 1));
        bottomPath.add(p1.cpy());

        for (int i = 0; i < topPath.size - 1; i++) {
            vertTmp1.set(Vector3.Zero, plane.normal, null, null);
            vertTmp2.set(Vector3.Zero, plane.normal, null, null);
            vertTmp3.set(Vector3.Zero, plane.normal, null, null);
            vertTmp4.set(Vector3.Zero, plane.normal, null, null);
            p1.set(bottomPath.get(i));
            p2.set(bottomPath.get(i + 1));
            p3.set(topPath.get(i + 1));
            p4.set(topPath.get(i));
            PlaneUtils.toSpace(plane, p1, vertTmp1.position);
            PlaneUtils.toSpace(plane, p2, vertTmp2.position);
            PlaneUtils.toSpace(plane, p3, vertTmp3.position);
            PlaneUtils.toSpace(plane, p4, vertTmp4.position);
            rect2sided(vertTmp1.position, vertTmp2.position, vertTmp3.position, vertTmp4.position);
        }

//        for (int i = 0; i < bottomPath.size; i++) {
//            final Vector2 v = bottomPath.get(i);
//            tmpVertices.add(v.x);
//            tmpVertices.add(v.y);
//        }
//        for (int i = topPath.size - 1; i >= 0; i++) {
//            final Vector2 v = topPath.get(i);
//            tmpVertices.add(v.x);
//            tmpVertices.add(v.y);
//        }
//        
//        polygonExtruded(tmpVertices, plane, strokeWidth);
    }

    public void polygon(org.locationtech.jts.geom.Polygon polygon, Plane plane) {
        final Coordinate[] ringCoords = polygon.getExteriorRing().getCoordinates();
        final List<PolygonPoint> points = Arrays.stream(ringCoords).map(c -> new PolygonPoint(c.x, c.y)).collect(Collectors.toList());
        final org.poly2tri.geometry.polygon.Polygon poly = new Polygon(points);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            final Coordinate[] holeCoords = polygon.getInteriorRingN(i).getCoordinates();
            final List<PolygonPoint> holePoints = Arrays.stream(holeCoords).map(c -> new PolygonPoint(c.x, c.y)).collect(Collectors.toList());
            poly.addHole(new Polygon(holePoints));
        }
        Poly2Tri.triangulate(poly);
        final List<DelaunayTriangle> triangles = poly.getTriangles();
        for (DelaunayTriangle triangle : triangles) {
            p1.set(triangle.points[0].getXf(), triangle.points[0].getYf());
            p2.set(triangle.points[1].getXf(), triangle.points[1].getYf());
            p3.set(triangle.points[2].getXf(), triangle.points[2].getYf());
            PlaneUtils.toSpace(plane, p1, v1);
            PlaneUtils.toSpace(plane, p2, v2);
            PlaneUtils.toSpace(plane, p3, v3);
            triangle2sided(v1, v2, v3);
        }
    }

    public void polygonExtruded(org.locationtech.jts.geom.Polygon polygon, Plane plane, float depth) {
        offset.set(plane.normal).nor().scl(depth / 2f);
        offset2.set(plane.normal).nor().scl(-depth / 2f);
        final Coordinate[] ringCoords = polygon.getExteriorRing().getCoordinates();
        for (int j = 0; j < ringCoords.length; j++) {
            p1.set((float) ringCoords[j].x, (float) ringCoords[j].y);
            final int j2 = (j + 1) % ringCoords.length;
            p2.set((float) ringCoords[j2].x, (float) ringCoords[j2].y);
            PlaneUtils.toSpace(plane, p1, v1);
            PlaneUtils.toSpace(plane, p2, v2);
            v3.set(v1).add(offset2);
            v4.set(v2).add(offset2);
            v1.add(offset);
            v2.add(offset);
            rect(v1, v2, v4, v3);
        }
        final List<PolygonPoint> points = Arrays.stream(ringCoords).map(c -> new PolygonPoint(c.x, c.y)).collect(Collectors.toList());
        final org.poly2tri.geometry.polygon.Polygon poly = new Polygon(points);
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            final Coordinate[] holeCoords = polygon.getInteriorRingN(i).getCoordinates();
            for (int j = 0; j < holeCoords.length; j++) {
                p1.set((float) holeCoords[j].x, (float) holeCoords[j].y);
                final int j2 = (j + 1) % holeCoords.length;
                p2.set((float) holeCoords[j2].x, (float) holeCoords[j2].y);
                PlaneUtils.toSpace(plane, p1, v1);
                PlaneUtils.toSpace(plane, p2, v2);
                v3.set(v1).add(offset2);
                v4.set(v2).add(offset2);
                v1.add(offset);
                v2.add(offset);
                rect(v1, v2, v4, v3);
            }
            final List<PolygonPoint> holePoints = Arrays.stream(holeCoords).map(c -> new PolygonPoint(c.x, c.y)).collect(Collectors.toList());
            poly.addHole(new Polygon(holePoints));
        }
        Poly2Tri.triangulate(poly);
        final List<DelaunayTriangle> triangles = poly.getTriangles();

        for (DelaunayTriangle triangle : triangles) {
            p1.set(triangle.points[0].getXf(), triangle.points[0].getYf());
            p2.set(triangle.points[1].getXf(), triangle.points[1].getYf());
            p3.set(triangle.points[2].getXf(), triangle.points[2].getYf());
            PlaneUtils.toSpace(plane, p1, v1);
            PlaneUtils.toSpace(plane, p2, v2);
            PlaneUtils.toSpace(plane, p3, v3);
            triangle(v1.add(offset), v2.add(offset), v3.add(offset));
        }

        for (DelaunayTriangle triangle : triangles) {
            p1.set(triangle.points[0].getXf(), triangle.points[0].getYf());
            p2.set(triangle.points[1].getXf(), triangle.points[1].getYf());
            p3.set(triangle.points[2].getXf(), triangle.points[2].getYf());
            PlaneUtils.toSpace(plane, p1, v1);
            PlaneUtils.toSpace(plane, p2, v2);
            PlaneUtils.toSpace(plane, p3, v3);
            triangle(v3.add(offset2), v2.add(offset2), v1.add(offset2));
        }
    }
}
