package net.masonapps.sketchvr.io;

import android.annotation.SuppressLint;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ShortArray;

import net.masonapps.sketchvr.Constants;
import net.masonapps.sketchvr.modeling.SketchNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bob on 7/11/2017.
 */

public class PLYWriter {

    public static void writeToFile(File file, List<SketchNode> nodes, Matrix4 transform) throws IOException {
        writeToOutputStream(new FileOutputStream(file), nodes, transform);
    }

    public static void writeToFile(File file, float[] vertices, short[] indices, VertexAttributes vertexAttributes, Matrix4 transform) throws IOException {
        writeToOutputStream(new FileOutputStream(file), vertices, indices, vertexAttributes, transform);
    }

    public static void writeToOutputStream(OutputStream outputStream, List<SketchNode> nodes, Matrix4 transform) throws IOException {
        if (nodes.isEmpty()) return;
        final FloatArray vertexArray = new FloatArray();
        final ShortArray indexArray = new ShortArray();
        final Vector3 pos = new Vector3();
        final Vector3 nor = new Vector3();

        for (SketchNode node : nodes) {
            final Mesh mesh = node.parts.get(0).meshPart.mesh;
            VertexAttributes vertexAttributes = mesh.getVertexAttributes();

            final float[] vertices = new float[mesh.getNumVertices()];
            mesh.getVertices(vertices);
            final short[] indices = new short[mesh.getNumIndices()];
            mesh.getIndices(indices);
            final int vertexSize = mesh.getVertexSize() / Float.BYTES;

            for (int i = 0; i < vertices.length; i += vertexSize) {
                pos.set(vertices[i], vertices[i + 1], vertices[i + 2]).mul(node.localTransform);
                vertexArray.add(pos.x);
                vertexArray.add(pos.y);
                vertexArray.add(pos.z);

                if ((vertexAttributes.getMask() & VertexAttributes.Usage.Normal) == VertexAttributes.Usage.Normal) {
                    final int nOffset = vertexAttributes.getOffset(VertexAttributes.Usage.Normal);
                    nor.set(vertices[i + nOffset], vertices[i + nOffset + 1], vertices[i + nOffset + 2]).rot(node.localTransform).nor();
                    vertexArray.add(nor.x);
                    vertexArray.add(nor.y);
                    vertexArray.add(nor.z);
                } else {
                    vertexArray.add(0f);
                    vertexArray.add(0f);
                    vertexArray.add(0f);
                }

//                if ((vertexAttributes.getMask() & VertexAttributes.Usage.TextureCoordinates) == VertexAttributes.Usage.TextureCoordinates) {
//                    final int tOffset = vertexAttributes.getOffset(VertexAttributes.Usage.TextureCoordinates);
//                    vertexArray.add(vertices[i + tOffset]);
//                    vertexArray.add(vertices[i + tOffset + 1]);
//                } else {
//                    vertexArray.add(0f);
//                    vertexArray.add(0f);
//                }

                vertexArray.add(node.getDiffuseColor().toFloatBits());
            }

            for (int i = 0; i < indices.length; i++) {
                indexArray.add(indices[i]);
            }
        }
        final VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorPacked());
        writeToOutputStream(outputStream, vertexArray.toArray(), indexArray.toArray(), vertexAttributes, transform);
    }

    @SuppressLint("DefaultLocale")
    public static void writeToOutputStream(OutputStream outputStream, float[] vertices, short[] indices, VertexAttributes vertexAttributes, Matrix4 transform) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        try {
            final boolean hasNormals = (vertexAttributes.getMask() & VertexAttributes.Usage.Normal) == VertexAttributes.Usage.Normal;
            final boolean hasColorPacked = (vertexAttributes.getMask() & VertexAttributes.Usage.ColorPacked) == VertexAttributes.Usage.ColorPacked;

            final int nOffset = vertexAttributes.getOffset(VertexAttributes.Usage.Normal, -1);
            final int cpOffset = vertexAttributes.getOffset(VertexAttributes.Usage.ColorPacked, -1);
            
            writer.write("ply");
            writer.newLine();
            writer.write("format ascii 1.0");
            writer.newLine();
            writer.write("comment Created by " + Constants.APP_NAME);
            writer.newLine();

            final String vs = "element vertex " + vertices.length;
            vs.replace(",", "");
            writer.write(vs);
            writer.newLine();

            writer.write("property float x");
            writer.newLine();
            writer.write("property float y");
            writer.newLine();
            writer.write("property float z");
            writer.newLine();

            if (hasNormals) {
                writer.write("property float nx");
                writer.newLine();
                writer.write("property float ny");
                writer.newLine();
                writer.write("property float nz");
                writer.newLine();
            }
            if (hasColorPacked) {
                writer.write("property uchar red");
                writer.newLine();
                writer.write("property uchar green");
                writer.newLine();
                writer.write("property uchar blue");
                writer.newLine();
            }

            writer.write("element face " + (indices.length / 3));
            writer.newLine();
            writer.write("property list uchar uint vertex_indices");
            writer.newLine();
            writer.write("end_header");
            writer.newLine();
            final Vector3 pos = new Vector3();
            final Vector3 nor = new Vector3();

            final int vertexSize = vertexAttributes.vertexSize / Float.BYTES;
            for (int i = 0; i < vertices.length; i += vertexSize) {
                pos.set(vertices[i], vertices[i + 1], vertices[i + 2]).mul(transform);
                writer.write(String.format(Locale.US, "%f %f %f",
                        pos.x,
                        pos.y,
                        pos.z));
                if (hasNormals) {
                    nor.set(vertices[i + nOffset], vertices[i + +nOffset + 1], vertices[i + nOffset + 2]).rot(transform).nor();
                    writer.write(String.format(Locale.US, " %f %f %f",
                            nor.x,
                            nor.y,
                            nor.z));
                }
                if (hasColorPacked) {
                    int c = NumberUtils.floatToIntColor(vertices[i + cpOffset]);
                    writer.write(String.format(Locale.US, " %d %d %d",
                            (c & 0x000000ff),
                            ((c & 0x0000ff00) >>> 8),
                            ((c & 0x00ff0000) >>> 16)));
                }
                writer.newLine();
            }

            for (int i = 0; i < indices.length; i += 3) {
                writer.write(String.format(Locale.US, "%d %d %d %d", 3, indices[i], indices[i + 1], indices[i + 2]));
                writer.newLine();
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }
}
