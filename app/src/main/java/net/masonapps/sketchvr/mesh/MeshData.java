package net.masonapps.sketchvr.mesh;

import android.util.Log;
import android.util.SparseIntArray;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.math.Quaternion;

import net.masonapps.sketchvr.io.PLYAssetLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 7/24/2017.
 */

public class MeshData {
    public final Vertex[] vertices;
    public final Triangle[] triangles;

    public MeshData(Vertex[] vertices, Triangle[] triangles) {
        this.vertices = vertices;
        this.triangles = triangles;
    }

    public MeshData(MeshData other) {
        this(other.vertices, other.triangles);
    }

    public static MeshData fromModelData(ModelData modelData) {
        final ModelMesh modelMesh = modelData.meshes.get(0);
        final int vertexSize = new VertexAttributes(modelMesh.attributes).vertexSize / 4;
        final float[] vertices = modelMesh.vertices;
        final short[] indices = modelMesh.parts[0].indices;
        return createMeshData(vertices, indices, vertexSize);
    }

    public static MeshData createMeshData(float[] vertices, short[] indices, int vertexSize) {
        final List<Vertex> vertexList = new ArrayList<>(vertices.length);
        final List<Triangle> triangles = new ArrayList<>(indices.length / 3);
        final float tolerance = 1e-5f;
        SparseIntArray indexMap = new SparseIntArray();
        int numDuplicates = 0;
        // TODO: 8/24/2017 remove rotation
        final Quaternion rotation = new Quaternion();
        rotation.setFromCross(0f, 0f, -1f, 1f, 0f, 0f);
        for (int i = 0; i < vertices.length; i += vertexSize) {
            boolean isDouble = false;
            final Vertex vertex = new Vertex();
            final int index = i / vertexSize;
            // TODO: 8/24/2017 remove rotation
            vertex.position.set(vertices[i], vertices[i + 1], vertices[i + 2]).mul(rotation);
            vertex.normal.set(vertices[i + 3], vertices[i + 4], vertices[i + 5]).mul(rotation);
//            vertex.position.set(vertices[i], vertices[i + 1], vertices[i + 2]);
//            vertex.normal.set(vertices[i + 3], vertices[i + 4], vertices[i + 5]);
            if (vertexSize > 6) {
                final int offset = 6;
                vertex.uv.set(vertices[i + offset], vertices[i + offset + 1]);
            }
            for (int j = 0; j < vertexList.size(); j++) {
                if (vertexList.get(j).position.dst(vertex.position) <= tolerance) {
                    numDuplicates++;
                    indexMap.put(index, j);
                    isDouble = true;
                    break;
                }
            }
            if (!isDouble) {
                vertex.index = vertexList.size();
                indexMap.put(index, vertexList.size());
                vertexList.add(vertex);
            }
        }

        for (int i = 0; i < indices.length; i += 3) {
            int ia = indices[i];
            ia = indexMap.get(ia, ia);

            int ib = indices[i + 1];
            ib = indexMap.get(ib, ib);

            int ic = indices[i + 2];
            ic = indexMap.get(ic, ic);
            final Triangle triangle = new Triangle(vertexList.get(ia), vertexList.get(ib), vertexList.get(ic));
            triangle.update();
            triangle.index = triangles.size();
            triangles.add(triangle);
        }
//        Log.d(PLYLoader.class.getSimpleName(), "createTriangleList eT: " + df.format(System.currentTimeMillis() - t));
        Log.d(PLYAssetLoader.class.getSimpleName(), numDuplicates + " duplicate vertices removed");
        return new MeshData(vertexList.toArray(new Vertex[vertexList.size()]), triangles.toArray(new Triangle[triangles.size()]));
    }

    public static Triangle[] fromFaces(List<Face> faceList) {
        final List<Triangle> triangleList = new ArrayList<>();
        faceList.forEach(face -> {
            for (int i = 2; i < face.vertices.length; i++) {
                Vertex v1;
                Vertex v2;
                Vertex v3;
                if (i >= 3) {
                    v1 = face.vertices[0];
                    v2 = face.vertices[i - 1];
                    v3 = face.vertices[i];
                } else {
                    v1 = face.vertices[i - 2];
                    v2 = face.vertices[i - 1];
                    v3 = face.vertices[i];
                }
                triangleList.add(new Triangle(v1, v2, v3));
            }
        });
        return triangleList.toArray(new Triangle[triangleList.size()]);
    }

    public Triangle[] getTriangles() {
        return triangles;
    }

    public Vertex getVertex(int i) {
        return vertices[i];
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public int getVertexCount() {
        return vertices.length;
    }

    public int getTriangleCount() {
        return triangles.length;
    }

    public MeshData copy() {
        return new MeshData(this);
    }

    public Triangle getTriangle(int index) {
        return triangles[index];
    }
}
