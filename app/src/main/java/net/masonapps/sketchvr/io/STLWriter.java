package net.masonapps.sketchvr.io;

import android.annotation.SuppressLint;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.sketchvr.modeling.EditableNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bob on 8/1/2017.
 */

public class STLWriter {

    public static void writeToFile(File file, List<EditableNode> nodes, Matrix4 transform) throws IOException {
        writeToOutputStream(new FileOutputStream(file), nodes, transform);
    }

    public static void writeToFile(File file, float[] vertices, short[] indices, int vertexSize, Matrix4 transform) throws IOException {
        writeToOutputStream(new FileOutputStream(file), vertices, indices, vertexSize, transform);
    }

    @SuppressLint("DefaultLocale")
    public static void writeToOutputStream(OutputStream outputStream, List<EditableNode> nodes, Matrix4 globalTransform) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        try {
            writer.write("solid");
            writer.newLine();

            for (EditableNode node : nodes) {
                writeNode(writer, node, globalTransform);
            }

            writer.write("endsolid");
            writer.newLine();
        } finally {
            writer.flush();
            writer.close();
        }
    }

    public static void writeNode(BufferedWriter writer, EditableNode node, Matrix4 globalTransform) throws IOException {
        final Mesh mesh = node.parts.get(0).meshPart.mesh;

        final float[] vertices = new float[mesh.getNumVertices()];
        mesh.getVertices(vertices);
        final short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);
        final int vertexSize = mesh.getVertexSize() / Float.BYTES;

        final Matrix4 transform = new Matrix4(globalTransform).mul(node.localTransform);

        Vector3 va = new Vector3();
        Vector3 vb = new Vector3();
        Vector3 vc = new Vector3();
        Plane plane = new Plane();

        for (int i = 0; i < indices.length; i += 3) {
            int ia = indices[i] * vertexSize;
            int ib = indices[i + 1] * vertexSize;
            int ic = indices[i + 2] * vertexSize;
            va.set(vertices[ia], vertices[ia + 1], vertices[ia + 2]).mul(transform);
            vb.set(vertices[ib], vertices[ib + 1], vertices[ib + 2]).mul(transform);
            vc.set(vertices[ic], vertices[ic + 1], vertices[ic + 2]).mul(transform);
            plane.set(va, vb, vc);
                writer.write(String.format(Locale.US, "facet normal %f %f %f",
                        plane.normal.x,
                        plane.normal.y,
                        plane.normal.z));
                writer.newLine();

                writer.write("\touter loop");
                writer.newLine();

                writer.write(String.format(Locale.US, "\t\tvertex %f %f %f",
                        va.x,
                        va.y,
                        va.z));
                writer.newLine();

                writer.write(String.format(Locale.US, "\t\tvertex %f %f %f",
                        vb.x,
                        vb.y,
                        vb.z));
                writer.newLine();

                writer.write(String.format(Locale.US, "\t\tvertex %f %f %f",
                        vc.x,
                        vc.y,
                        vc.z));
                writer.newLine();

                writer.write("\tendloop");
                writer.newLine();

                writer.write("endfacet");
                writer.newLine();
            }
    }

    public static void writeToOutputStream(OutputStream outputStream, float[] vertices, short[] indices, int vertexSize, Matrix4 transform) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        try {
            writer.write("solid");
            writer.newLine();

            Vector3 va = new Vector3();
            Vector3 vb = new Vector3();
            Vector3 vc = new Vector3();
            Plane plane = new Plane();

            for (int i = 0; i < indices.length; i += 3) {
                int ia = indices[i];
                int ib = indices[i + 1];
                int ic = indices[i + 2];
                va.set(vertices[ia * vertexSize], vertices[ia * vertexSize + 1], vertices[ia * vertexSize + 2]).mul(transform);
                vb.set(vertices[ib * vertexSize], vertices[ib * vertexSize + 1], vertices[ib * vertexSize + 2]).mul(transform);
                vc.set(vertices[ic * vertexSize], vertices[ic * vertexSize + 1], vertices[ic * vertexSize + 2]).mul(transform);
                plane.set(va, vb, vc);
                writer.write(String.format(Locale.US, "facet normal %f %f %f",
                        plane.normal.x,
                        plane.normal.y,
                        plane.normal.z));
                writer.newLine();

                writer.write("\touter loop");
                writer.newLine();

                writer.write(String.format(Locale.US, "\t\tvertex %f %f %f",
                        va.x,
                        va.y,
                        va.z));
                writer.newLine();

                writer.write(String.format(Locale.US, "\t\tvertex %f %f %f",
                        vb.x,
                        vb.y,
                        vb.z));
                writer.newLine();

                writer.write(String.format(Locale.US, "\t\tvertex %f %f %f",
                        vc.x,
                        vc.y,
                        vc.z));
                writer.newLine();

                writer.write("\tendloop");
                writer.newLine();

                writer.write("endfacet");
                writer.newLine();
            }

            writer.write("endsolid");
            writer.newLine();
        } finally {
            writer.flush();
            writer.close();
        }
    }
}
