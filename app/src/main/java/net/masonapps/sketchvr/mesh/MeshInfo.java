package net.masonapps.sketchvr.mesh;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

/**
 * Created by Bob Mason on 2/16/2018.
 */

public class MeshInfo {
    public float[] vertices;
    public short[] indices;
    public int numVertices = 0;
    public int numIndices = 0;
    public VertexAttributes vertexAttributes;

    public Mesh createMesh() {
        final Mesh mesh = new Mesh(true, true, numVertices, numIndices, vertexAttributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        return mesh;
    }

    public Mesh createDynamicMesh() {
        if (vertexAttributes == null)
            vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal());
        final Mesh mesh = new Mesh(false, false, 1024 * 10, 1024 * 20, vertexAttributes);
        if (vertices == null)
            vertices = new float[0];
        mesh.setVertices(vertices);
        if (indices == null)
            indices = new short[0];
        mesh.setIndices(indices);
        return mesh;
    }
}
