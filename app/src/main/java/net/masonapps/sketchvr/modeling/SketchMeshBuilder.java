package net.masonapps.sketchvr.modeling;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Bob Mason on 4/27/2018. Modified Version of {@link com.badlogic.gdx.graphics.g3d.utils.MeshBuilder} written by Xoppa
 */
public class SketchMeshBuilder {
    private final static Vector3 vTmp = new Vector3();
    private final static Vector3 normal = new Vector3();
    private final MeshPartBuilder.VertexInfo vertTmp1 = new MeshPartBuilder.VertexInfo();
    private final MeshPartBuilder.VertexInfo vertTmp2 = new MeshPartBuilder.VertexInfo();
    private final MeshPartBuilder.VertexInfo vertTmp3 = new MeshPartBuilder.VertexInfo();
    private final MeshPartBuilder.VertexInfo vertTmp4 = new MeshPartBuilder.VertexInfo();
    private final BoundingBox bounds = new BoundingBox();
    private final Mesh mesh;
    private final Vector3 tmpNormal = new Vector3();
    private static int partNumber = 0;
    /**
     * The vertex attributes of the resulting mesh
     */
    private VertexAttributes attributes;
    /**
     * The vertices to construct, no size checking is done
     */
    private FloatArray vertices = new FloatArray();
    /**
     * The indices to construct, no size checking is done
     */
    private ShortArray indices = new ShortArray();
    /**
     * The size (in number of floats) of each vertex
     */
    private int stride;
    /**
     * The current vertex index, used for indexing
     */
    private short vindex;
    /**
     * The offset in the indices array when begin() was called, used to define a meshpart.
     */
    private int istart;
    /**
     * The offset within an vertex to position
     */
    private int posOffset;
    /**
     * The offset within an vertex to normal, or -1 if not available
     */
    private int norOffset;
    /**
     * The meshpart currently being created
     */
    private MeshPart part;
    /**
     * The current primitiveType
     */
    private int primitiveType;
    private float[] vertex;
    private short lastIndex;

    public SketchMeshBuilder() {
        mesh = new Mesh(false, Short.MAX_VALUE, Short.MAX_VALUE * 3, VertexAttribute.Position(), VertexAttribute.Normal());
        this.attributes = mesh.getVertexAttributes();
        this.vertices.clear();
        this.indices.clear();
        this.vindex = 0;
        this.lastIndex = -1;
        this.istart = 0;
        this.part = null;
        this.stride = attributes.vertexSize / 4;
        this.vertex = new float[stride];
        VertexAttribute a = attributes.findByUsage(VertexAttributes.Usage.Position);
        posOffset = a.offset / 4;
        a = attributes.findByUsage(VertexAttributes.Usage.Normal);
        norOffset = a.offset / 4;
        this.primitiveType = GL20.GL_TRIANGLES;
        bounds.inf();
    }

    private static void transformPosition(final float[] values, final int offset, final int size, Matrix4 transform) {
        if (size > 2) {
            vTmp.set(values[offset], values[offset + 1], values[offset + 2]).mul(transform);
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
            values[offset + 2] = vTmp.z;
        } else if (size > 1) {
            vTmp.set(values[offset], values[offset + 1], 0).mul(transform);
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
        } else
            values[offset] = vTmp.set(values[offset], 0, 0).mul(transform).x;
    }

    private static void transformNormal(final float[] values, final int offset, final int size, Matrix3 transform) {
        if (size > 2) {
            vTmp.set(values[offset], values[offset + 1], values[offset + 2]).mul(transform).nor();
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
            values[offset + 2] = vTmp.z;
        } else if (size > 1) {
            vTmp.set(values[offset], values[offset + 1], 0).mul(transform).nor();
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
        } else
            values[offset] = vTmp.set(values[offset], 0, 0).mul(transform).nor().x;
    }

    public void begin() {
        begin(new MeshPart());
    }

    public void begin(MeshPart meshPart) {
        begin("p" + (partNumber++), meshPart);
    }

    public void begin(final String id, MeshPart meshPart) {
        if (part != null)
            throw new RuntimeException("end() must be called before beginning a new MeshPart");
        part = meshPart;
        part.id = id;
        part.primitiveType = primitiveType;
    }

    public MeshPart end() {
        if (part == null)
            throw new RuntimeException("begin() must be called before ending a MeshPart");
        final MeshPart outPart = part;

        bounds.getCenter(outPart.center);
        bounds.getDimensions(outPart.halfExtents).scl(0.5f);
        outPart.radius = outPart.halfExtents.len();
        bounds.inf();
        outPart.offset = istart;
        outPart.size = indices.size - istart;
        istart = indices.size;
        part = null;

        mesh.setVertices(vertices.items, 0, vertices.size);
        mesh.setIndices(indices.items, 0, indices.size);

        return outPart;
    }

    /**
     * @return the size in number of floats of one vertex, multiply by four to get the size in bytes.
     */
    public int getFloatsPerVertex() {
        return stride;
    }

    /**
     * @return The number of vertices built up until now, only valid in between the call to begin() and end().
     */
    public int getNumVertices() {
        return vertices.size / stride;
    }

    /**
     * Get a copy of the vertices built so far.
     *
     * @param out        The float array to receive the copy of the vertices, must be at least `destOffset` + {@link #getNumVertices()} *
     *                   {@link #getFloatsPerVertex()} in size.
     * @param destOffset The offset (number of floats) in the out array where to start copying
     */
    public void getVertices(float[] out, int destOffset) {
        if ((destOffset < 0) || (destOffset > out.length - vertices.size))
            throw new GdxRuntimeException("Array to small or offset out of range");
        System.arraycopy(vertices.items, 0, out, destOffset, vertices.size);
    }

    /**
     * Provides direct access to the vertices array being built, use with care. The size of the array might be bigger, do not rely
     * on the length of the array. Instead use {@link #getFloatsPerVertex()} * {@link #getNumVertices()} to calculate the usable
     * size of the array. Must be called in between the call to #begin and #end.
     */
    protected float[] getVertices() {
        return vertices.items;
    }

    /**
     * @return The number of indices built up until now, only valid in between the call to begin() and end().
     */
    public int getNumIndices() {
        return indices.size;
    }

    /**
     * Get a copy of the indices built so far.
     *
     * @param out        The short array to receive the copy of the indices, must be at least `destOffset` + {@link #getNumIndices()} in
     *                   size.
     * @param destOffset The offset (number of shorts) in the out array where to start copying
     */
    public void getIndices(short[] out, int destOffset) {
        if (attributes == null)
            throw new GdxRuntimeException("Must be called in between #begin and #end");
        if ((destOffset < 0) || (destOffset > out.length - indices.size))
            throw new GdxRuntimeException("Array to small or offset out of range");
        System.arraycopy(indices.items, 0, out, destOffset, indices.size);
    }

    /**
     * Provides direct access to the indices array being built, use with care. The size of the array might be bigger, do not rely
     * on the length of the array. Instead use {@link #getNumIndices()} to calculate the usable size of the array. Must be called
     * in between the call to #begin and #end.
     */
    protected short[] getIndices() {
        return indices.items;
    }

    public VertexAttributes getAttributes() {
        return attributes;
    }

    public MeshPart getMeshPart() {
        return part;
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    public void ensureVertices(int numVertices) {
        vertices.ensureCapacity(stride * numVertices);
    }

    public void ensureIndices(int numIndices) {
        indices.ensureCapacity(numIndices);
    }

    public void ensureCapacity(int numVertices, int numIndices) {
        ensureVertices(numVertices);
        ensureIndices(numIndices);
    }

    public void ensureTriangleIndices(int numTriangles) {
        if (primitiveType == GL20.GL_LINES)
            ensureIndices(6 * numTriangles);
        else if (primitiveType == GL20.GL_TRIANGLES || primitiveType == GL20.GL_POINTS)
            ensureIndices(3 * numTriangles);
        else
            throw new GdxRuntimeException("Incorrect primtive type");
    }

    public void ensureTriangles(int numVertices, int numTriangles) {
        ensureVertices(numVertices);
        ensureTriangleIndices(numTriangles);
    }

    public void ensureTriangles(int numTriangles) {
        ensureVertices(3 * numTriangles);
        ensureTriangleIndices(numTriangles);
    }

    public void ensureRectangleIndices(int numRectangles) {
        if (primitiveType == GL20.GL_POINTS)
            ensureIndices(4 * numRectangles);
        else if (primitiveType == GL20.GL_LINES)
            ensureIndices(8 * numRectangles);
        else
            // GL_TRIANGLES
            ensureIndices(6 * numRectangles);
    }

    public void ensureRectangles(int numVertices, int numRectangles) {
        ensureVertices(numVertices);
        ensureRectangleIndices(numRectangles);
    }

    public void ensureRectangles(int numRectangles) {
        ensureVertices(4 * numRectangles);
        ensureRectangleIndices(numRectangles);
    }

    private void addVertex(final float[] values, final int offset) {
        final int o = vertices.size;
        vertices.addAll(values, offset, stride);
        lastIndex = vindex++;

//        if (vertexTransformationEnabled) {
//            transformPosition(vertices.items, o + posOffset, posSize, positionTransform);
//            if (norOffset >= 0) transformNormal(vertices.items, o + norOffset, 3, normalTransform);
//        }

        final float x = vertices.items[o + posOffset];
        final float y = vertices.items[o + posOffset + 1];
        final float z = vertices.items[o + posOffset + 2];
        bounds.ext(x, y, z);
    }

    public short vertex(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
        if (vindex >= Short.MAX_VALUE) throw new RuntimeException("Too many vertices used");

        vertex[posOffset] = pos.x;
        vertex[posOffset + 1] = pos.y;
        vertex[posOffset + 2] = pos.z;

        if (norOffset >= 0) {
            if (nor == null) nor = tmpNormal.set(pos).nor();
            vertex[norOffset] = nor.x;
            vertex[norOffset + 1] = nor.y;
            vertex[norOffset + 2] = nor.z;
        }

        addVertex(vertex, 0);
        return lastIndex;
    }

    public short vertex(final float... values) {
        final int n = values.length - stride;
        for (int i = 0; i <= n; i += stride)
            addVertex(values, i);
        return lastIndex;
    }

    public short vertex(final MeshPartBuilder.VertexInfo info) {
        return vertex(info.hasPosition ? info.position : null, info.hasNormal ? info.normal : null, info.hasColor ? info.color
                : null, info.hasUV ? info.uv : null);
    }

    private void index(final short value) {
        indices.add(value);
    }

    private void index(final short value1, final short value2) {
        ensureIndices(2);
        indices.add(value1);
        indices.add(value2);
    }

    private void index(final short value1, final short value2, final short value3) {
        ensureIndices(3);
        indices.add(value1);
        indices.add(value2);
        indices.add(value3);
    }

    private void index(final short value1, final short value2, final short value3, final short value4) {
        ensureIndices(4);
        indices.add(value1);
        indices.add(value2);
        indices.add(value3);
        indices.add(value4);
    }

    private void index(short value1, short value2, short value3, short value4, short value5, short value6) {
        ensureIndices(6);
        indices.add(value1);
        indices.add(value2);
        indices.add(value3);
        indices.add(value4);
        indices.add(value5);
        indices.add(value6);
    }

    private void index(short value1, short value2, short value3, short value4, short value5, short value6, short value7,
                       short value8) {
        ensureIndices(8);
        indices.add(value1);
        indices.add(value2);
        indices.add(value3);
        indices.add(value4);
        indices.add(value5);
        indices.add(value6);
        indices.add(value7);
        indices.add(value8);
    }

    private void triangle(short index1, short index2, short index3) {
        if (primitiveType == GL20.GL_TRIANGLES || primitiveType == GL20.GL_POINTS) {
            index(index1, index2, index3);
        } else if (primitiveType == GL20.GL_LINES) {
            index(index1, index2, index2, index3, index3, index1);
        } else
            throw new GdxRuntimeException("Incorrect primitive type");
    }

    private void triangle(MeshPartBuilder.VertexInfo p1, MeshPartBuilder.VertexInfo p2, MeshPartBuilder.VertexInfo p3) {
        ensureVertices(3);
        triangle(vertex(p1), vertex(p2), vertex(p3));
    }

    public void triangle(Vector3 p1, Vector3 p2, Vector3 p3) {
        normal.set(p1).sub(p2).crs(p2.x - p3.x, p2.y - p3.y, p2.z - p3.z).nor();
        triangle(vertTmp1.set(p1, normal, null, null), vertTmp2.set(p2, normal, null, null), vertTmp3.set(p3, normal, null, null));
    }

    public void triangle2sided(Vector3 p1, Vector3 p2, Vector3 p3) {
        normal.set(p1).sub(p2).crs(p2.x - p3.x, p2.y - p3.y, p2.z - p3.z).nor();
        triangle(vertTmp1.set(p1, normal, null, null), vertTmp2.set(p2, normal, null, null), vertTmp3.set(p3, normal, null, null));
    }

    public void triangle(float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3) {
        normal.set(x1, y1, z1).sub(x2, y2, z2).crs(x2 - x3, y2 - y3, z2 - z3).nor();
        triangle(vertTmp1.set(null, null, null, null).setPos(x1, y1, z1).setNor(normal.x, normal.y, normal.z),
                vertTmp2.set(null, null, null, null).setPos(x2, y2, z2).setNor(normal.x, normal.y, normal.z),
                vertTmp3.set(null, null, null, null).setPos(x3, y3, z3).setNor(normal.x, normal.y, normal.z));
    }

    public void triangle2sided(float x1, float y1, float z1,
                               float x2, float y2, float z2,
                               float x3, float y3, float z3) {
        triangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        triangle(x3, y3, z3, x2, y2, z2, x1, y1, z1);
    }


    private void rect(short corner00, short corner10, short corner11, short corner01) {
        if (primitiveType == GL20.GL_TRIANGLES) {
            index(corner00, corner10, corner11, corner11, corner01, corner00);
        } else if (primitiveType == GL20.GL_LINES) {
            index(corner00, corner10, corner10, corner11, corner11, corner01, corner01, corner00);
        } else if (primitiveType == GL20.GL_POINTS) {
            index(corner00, corner10, corner11, corner01);
        } else
            throw new GdxRuntimeException("Incorrect primitive type");
    }

    private void rect(MeshPartBuilder.VertexInfo corner00, MeshPartBuilder.VertexInfo corner10, MeshPartBuilder.VertexInfo corner11, MeshPartBuilder.VertexInfo corner01) {
        ensureVertices(4);
        rect(vertex(corner00), vertex(corner10), vertex(corner11), vertex(corner01));
    }

    public void rect(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01) {
        normal.set(corner00).sub(corner10).crs(corner10.x - corner11.x, corner10.y - corner11.y, corner10.z - corner11.z).nor();
        rect(vertTmp1.set(corner00, normal, null, null).setUV(0f, 1f), vertTmp2.set(corner10, normal, null, null).setUV(1f, 1f),
                vertTmp3.set(corner11, normal, null, null).setUV(1f, 0f), vertTmp4.set(corner01, normal, null, null).setUV(0f, 0f));
    }

    public void rect2sided(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01) {
        rect(corner00, corner10, corner11, corner01);
        rect(corner01, corner11, corner10, corner00);
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

    public void rect2sided(float x00, float y00, float z00,
                           float x10, float y10, float z10,
                           float x11, float y11, float z11,
                           float x01, float y01, float z01) {
        rect(x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
        rect(x01, y01, z01, x11, y11, z11, x10, y10, z10, x00, y00, z00);
    }

    public Mesh getMesh() {
        return mesh;
    }
}