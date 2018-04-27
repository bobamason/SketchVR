package net.masonapps.sketchvr.io;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.sketchvr.modeling.SketchNode;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Bob on 9/1/2017.
 */

public class OBJWriter {


    public static void writeToZip(File zipFile, List<SketchNode> nodes, Matrix4 transform) throws IOException {
        final int index = zipFile.getName().lastIndexOf('.');
        final String name = zipFile.getName().substring(0, index > 0 ? index : zipFile.getName().length());

        final ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

        zipOutputStream.putNextEntry(new ZipEntry(name + ".obj"));
        final ByteArrayOutputStream objOutputStream = new ByteArrayOutputStream();
        writeObjToOutputStream(objOutputStream, nodes, name.substring(0, index > 0 ? index : name.length()), transform);
        zipOutputStream.write(objOutputStream.toByteArray());

        zipOutputStream.putNextEntry(new ZipEntry(name + ".mtl"));
        final ByteArrayOutputStream mtlOutputStream = new ByteArrayOutputStream();
        writeMtlToOutputStream(mtlOutputStream, nodes);
        zipOutputStream.write(mtlOutputStream.toByteArray());
    }

    public static void writeToFiles(File objFile, File mtlFile, List<SketchNode> nodes, Matrix4 transform) throws IOException {

        final String name = mtlFile.getName();
        final int index = name.lastIndexOf('.');
        writeObjToOutputStream(new FileOutputStream(objFile), nodes, name.substring(0, index > 0 ? index : name.length()), transform);

        writeMtlToOutputStream(new FileOutputStream(mtlFile), nodes);
    }

    @SuppressLint("DefaultLocale")
    private static void writeObjToOutputStream(OutputStream outputStream, List<SketchNode> nodes, String mtlfilename, Matrix4 transform) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        try {
            writer.write("mtllib ./" + mtlfilename);
            writer.newLine();
            for (int i = 0; i < nodes.size(); i++) {
                writeObject(writer, nodes.get(i), "obj" + i, getMaterialName(i), transform);
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }

    private static void writeObject(BufferedWriter writer, SketchNode node, String groupName, String materialName, Matrix4 globalTransform) throws IOException {
        writer.write("g " + groupName);
        writer.newLine();
        writer.write("usemtl " + materialName);
        writer.newLine();
        final Vector3 pos = new Vector3();
        final Vector3 nor = new Vector3();

        final Mesh mesh = node.parts.get(0).meshPart.mesh;

        final float[] vertices = new float[mesh.getNumVertices()];
        mesh.getVertices(vertices);
        final short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);
        final int vertexSize = mesh.getVertexSize() / Float.BYTES;

        final Matrix4 transform = new Matrix4(globalTransform).mul(node.localTransform);

        for (int i = 0; i < vertices.length; i += vertexSize) {
            pos.set(vertices[i], vertices[i + 1], vertices[i + 2]).mul(transform);
            writePosition(writer, pos.x, pos.y, pos.z);
        }

        for (int i = 0; i < vertices.length; i += vertexSize) {
            nor.set(vertices[i + 3], vertices[i + 4], vertices[i + 5]).rot(transform).nor();
            writeNormal(writer, nor.x, nor.y, nor.z);
        }

        if ((mesh.getVertexAttributes().getMask() & VertexAttributes.Usage.TextureCoordinates) == VertexAttributes.Usage.TextureCoordinates) {
            for (int i = 0; i < vertices.length; i += vertexSize) {
                writeTextureCoordinate(writer, vertices[i + 6], vertices[i + 7]);
            }
        }

        for (int i = 0; i < indices.length; i += 3) {
            writeFace(writer, indices[i], indices[i + 1], indices[i + 2]);
        }
    }

    private static void writePosition(BufferedWriter writer, float x, float y, float z) throws IOException {
        writer.write(String.format(Locale.US, "v %f %f %f", x, y, z));
        writer.newLine();
    }

    private static void writeNormal(BufferedWriter writer, float x, float y, float z) throws IOException {
        writer.write(String.format(Locale.US, "vn %f %f %f", x, y, z));
        writer.newLine();
    }

    private static void writeTextureCoordinate(BufferedWriter writer, float u, float v) throws IOException {
        writer.write(String.format(Locale.US, "vt %f %f", u, v));
        writer.newLine();
    }

    private static void writeFace(BufferedWriter writer, int a, int b, int c) throws IOException {
        writer.write(String.format(Locale.US, "f %d %d %d", a, b, c));
        writer.newLine();
    }

    private static void writeMtlToOutputStream(OutputStream outputStream, List<SketchNode> nodes) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        try {
            for (int i = 0; i < nodes.size(); i++) {
                writeMaterial(writer, nodes.get(i), getMaterialName(i));
            }
        } finally {
            writer.flush();
            writer.close();
        }
    }

    @NonNull
    private static String getMaterialName(int index) {
        return "mat" + index;
    }

    private static void writeMaterial(BufferedWriter writer, SketchNode node, String materialName) throws IOException {
        writer.write("newmtl " + materialName);
        writer.newLine();
        Color ambient = node.getAmbientColor();
        writer.write(String.format(Locale.US, "Ka %f %f %f", ambient.r, ambient.g, ambient.b));
        writer.newLine();
        Color diffuse = node.getAmbientColor();
        writer.write(String.format(Locale.US, "Kd %f %f %f", diffuse.r, diffuse.g, diffuse.b));
        writer.newLine();
        Color specular = node.getAmbientColor();
        writer.write(String.format(Locale.US, "Ks %f %f %f", specular.r, specular.g, specular.b));
        writer.newLine();
        writer.write("illum 2");
        writer.newLine();
    }

//    @SuppressWarnings("NumericOverflow")
//    private static void drawVertexColorsToCanvas(Canvas canvas, float[] vertices, short[] indices, int vertexSize, boolean flipV) {
//        if (vertexSize != 9 || vertices.length % 9 != 0)
//            throw new IllegalArgumentException("vertexSize must be 9, [px, py, pz, nx, ny, nz, u, v, color]");
//        float w = canvas.getWidth();
//        float h = canvas.getHeight();
//        final Vector2 tc = new Vector2();
//        final Color c = new Color();
//        final float[] vertices2D = new float[vertices.length / vertexSize * 2];
//        final int[] colors = new int[vertices.length / vertexSize];
//
//        for (int i = 0; i < vertices2D.length; i += 2) {
//            final int iv = i / 2 * vertexSize;
//            tc.set(vertices[iv + 6], vertices[iv + 7]);
//            vertices2D[i] = tc.x * w;
//            vertices2D[i] = (flipV ? 1f - tc.y : tc.y) * h;
//            Color.argb8888ToColor(c, NumberUtils.floatToIntColor(vertices[iv + 8]));
//            final int red = Math.round(c.r * 255f);
//            final int green = Math.round(c.g * 255f);
//            final int blue = Math.round(c.b * 255f);
//            colors[i / 2] = android.graphics.Color.rgb(red, green, blue);
//        }
//
//        for (int i = 0; i < indices.length; i += 3) {
//            canvas.drawVertices(Canvas.VertexMode.TRIANGLES, vertices2D.length, vertices2D, 0, null, 0, colors, 0, indices, 0, indices.length, new Paint(Paint.ANTI_ALIAS_FLAG));
//        }
//    }
}
