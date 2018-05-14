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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import org.masonapps.libgdxgooglevr.math.PlaneUtils;

import java.util.List;

/**
 * Created by Bob Mason on 5/3/2018.
 */
public class SketchMeshBuilder extends MeshBuilder {
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

    public void triangle2sided(Vector3 p1, Vector3 p2, Vector3 p3) {
        normal.set(p1).sub(p2).crs(p2.x - p3.x, p2.y - p3.y, p2.z - p3.z).nor();
        triangle(vertTmp1.set(p1, normal, null, null), vertTmp2.set(p2, normal, null, null), vertTmp3.set(p3, normal, null, null));
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

    public void extrude(FloatArray vertices, Plane plane, Plane plane2) {
        if (vertices.size < 6) return;
        tmpVecs.clear();

        for (int i = 0; i < vertices.size; i += 2) {
            p1.set(vertices.get(i), vertices.get(i + 1));
            PlaneUtils.toSpace(plane, p1, vTmp);
            tmpVecs.add(vTmp.cpy());
        }
        for (int i = 0; i < vertices.size; i += 2) {
            p1.set(vertices.get(i), vertices.get(i + 1));
            PlaneUtils.toSpace(plane2, p1, vTmp);
            tmpVecs.add(vTmp.cpy());
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

    public void sweep(FloatArray polygon, List<Vector3> path) {
        if (path.size() < 2 || polygon.size < 6) return;
        dir.set(path.get(0)).sub(path.get(1)).nor();
        plane1.set(path.get(0), dir);
        polygon(polygon, plane1);
        plane1.normal.scl(-1);
        for (int i = 1; i < path.size() - 1; i++) {
            dir.set(path.get(i)).sub(path.get(i - 1)).nor();
            dir2.set(path.get(i + 1)).sub(path.get(i)).nor();
            plane2.set(path.get(i), dir.add(dir2).scl(0.5f));
            extrude(polygon, plane1, plane2);
            plane1.set(plane2);
        }
        dir2.set(path.get(path.size() - 1)).sub(path.get(path.size() - 2)).nor();
        plane2.set(path.get(path.size() - 1), dir2);
        extrude(polygon, plane1, plane2);
        polygon(polygon, plane2);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void drawStrokePath(List<Vector2> path, Plane plane, float strokeWidth) {
        final int n = path.size();
        if (n < 2) return;
        topPath.clear();
        bottomPath.clear();
        tmpVertices.clear();
        final float hw = strokeWidth / 2;
        float x;
        float y;
        // path start
        off1.set(path.get(1)).sub(path.get(0));
        x = -off1.y;
        y = off1.x;
        off1.set(x, y).nor().scl(hw);
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
            x = -off1.y;
            y = off1.x;
            off1.set(x, y).nor().scl(hw);
            //      first point of first segment
            p1.set(off1).add(prev);
            //      second point of first segment
            p2.set(off1).add(curr);

            //      second segment
            off2.set(next).sub(curr);
            x = -off2.y;
            y = off2.x;
            off2.set(x, y).nor().scl(hw);
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
        x = -off1.y;
        y = off1.x;
        off1.set(x, y).nor().scl(hw);
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
}
