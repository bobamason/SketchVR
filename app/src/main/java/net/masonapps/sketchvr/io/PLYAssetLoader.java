package net.masonapps.sketchvr.io;

import android.util.Log;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import net.masonapps.sketchvr.mesh.Face;
import net.masonapps.sketchvr.mesh.MeshData;
import net.masonapps.sketchvr.mesh.MeshUtils;
import net.masonapps.sketchvr.mesh.Vertex;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 7/11/2017.
 */

public class PLYAssetLoader {
//    private static final SimpleDateFormat df = new SimpleDateFormat("mm:ss.SSS");

    public static ModelData parse(File file, boolean flipV) throws IOException {
        return parse(new FileInputStream(file), flipV);
    }

    public static ModelData parse(InputStream inputStream, boolean flipV) throws IOException {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final int vertexSize = parseStream(new BufferedInputStream(inputStream), flipV, vertices, indices);
        if (vertexSize == 8)
            return MeshUtils.createModelData(vertices.toArray(), indices.toArray(), VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        else if (vertexSize == 6)
            return MeshUtils.createModelData(vertices.toArray(), indices.toArray(), VertexAttribute.Position(), VertexAttribute.Normal());
        else if (vertexSize == 3)
            return MeshUtils.createModelData(vertices.toArray(), indices.toArray(), VertexAttribute.Position());
        else
            throw new IOException("file was not exported properly or is corrupt");
    }

    private static int parseStream(InputStream inputStream, boolean flipV, FloatArray vertices, ShortArray indices) throws IOException {
//        long t = System.currentTimeMillis();
        String line;
        String[] tokens;
        int xIndex = -1, yIndex = -1, zIndex = -1, nxIndex = -1, nyIndex = -1, nzIndex = -1, uIndex = -1, vIndex = -1;
        boolean hasNormal = false;
        boolean hasUV = false;
        int propertyIndex = 0;
        int vertexCount = 0, faceCount = 0, currentVertex = 0, currentFace = 0, vertexSize = 3;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            while ((line = reader.readLine()) != null) {
                tokens = line.split("\\s+");
                if (tokens.length < 1) break;

                if (tokens[0].length() == 0) {
                    continue;
                }

                if (tokens[0].equals("element")) {
                    if (tokens[1].equals("vertex")) {
                        vertexCount = Integer.parseInt(tokens[2]);
                    } else if (tokens[1].equals("face")) {
                        faceCount = Integer.parseInt(tokens[2]);
                    }

                } else if (tokens[0].equals("property")) {
                    if (tokens[2].equals("x")) {
                        xIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("y")) {
                        yIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("z")) {
                        zIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("nx")) {
                        nxIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("ny")) {
                        nyIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("nz")) {
                        nzIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("s")) {
                        uIndex = propertyIndex;
                        propertyIndex++;
                        hasUV = true;
                    }
                    if (tokens[2].equals("t")) {
                        vIndex = propertyIndex;
                        propertyIndex++;
                        hasUV = true;
                    }
                } else if (tokens[0].equals("end_header")) {
                    vertexSize = 3;
                    if (hasNormal) vertexSize += 3;
                    if (hasUV) vertexSize += 2;
                    vertices.ensureCapacity(vertexCount * vertexSize);
                    indices.ensureCapacity(faceCount * 3);
                    break;
                }
            }

            while (currentVertex < vertexCount) {
                line = reader.readLine();
                if (line == null) break;
                tokens = line.split("\\s+");
                float x = 0, y = 0, z = 0, nx = 0, ny = 0, nz = 0, u = 0, v = 0;
                try {
                    x = Float.parseFloat(tokens[xIndex]);
                    y = Float.parseFloat(tokens[yIndex]);
                    z = Float.parseFloat(tokens[zIndex]);
                    if (hasNormal) {
                        nx = Float.parseFloat(tokens[nxIndex]);
                        ny = Float.parseFloat(tokens[nyIndex]);
                        nz = Float.parseFloat(tokens[nzIndex]);
                    }
                    if (hasUV) {
                        u = Float.parseFloat(tokens[uIndex]);
                        v = Float.parseFloat(tokens[vIndex]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
                if (hasNormal) {
                    vertices.add(nx);
                    vertices.add(ny);
                    vertices.add(nz);
                }
                if (hasUV) {
                    vertices.add(u);
                    vertices.add(flipV ? 1f - v : v);
                }
                currentVertex++;
            }

            Log.d(PLYAssetLoader.class.getSimpleName(), "vertices " + (vertices.size / vertexSize) + " file count " + vertexCount);

            while (currentFace < faceCount) {
                line = reader.readLine();
                if (line == null) break;
                tokens = line.split("\\s+");
                final int n = Integer.parseInt(tokens[0]);
                for (int i = 0; i < n; i++) {
                    if (i + 1 > tokens.length) break;
                    if (i >= 3) {
                        indices.add(Integer.parseInt(tokens[1]));
                        indices.add(Integer.parseInt(tokens[i]));
                    }
                    indices.add(Integer.parseInt(tokens[i + 1]));
                }
                currentFace++;
            }
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
        Log.d(PLYAssetLoader.class.getSimpleName(), "faces " + (indices.size / 3));
//        Log.d(PLYLoader.class.getSimpleName(), "parseStream eT: " + df.format(System.currentTimeMillis() - t));
        return vertexSize;
    }

    public static void parseFaceList(InputStream inputStream, boolean flipV, List<Vertex> vertices, List<Face> faces) throws IOException {
        String line;
        String[] tokens;
        int xIndex = -1, yIndex = -1, zIndex = -1, nxIndex = -1, nyIndex = -1, nzIndex = -1, uIndex = -1, vIndex = -1;
        boolean hasNormal = false;
        boolean hasUV = false;
        int propertyIndex = 0;
        int vertexCount = 0, faceCount = 0, currentVertex = 0, currentFace = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            while ((line = reader.readLine()) != null) {
                tokens = line.split("\\s+");
                if (tokens.length < 1) break;

                if (tokens[0].length() == 0) {
                    continue;
                }

                if (tokens[0].equals("element")) {
                    if (tokens[1].equals("vertex")) {
                        vertexCount = Integer.parseInt(tokens[2]);
                    } else if (tokens[1].equals("face")) {
                        faceCount = Integer.parseInt(tokens[2]);
                    }

                } else if (tokens[0].equals("property")) {
                    if (tokens[2].equals("x")) {
                        xIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("y")) {
                        yIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("z")) {
                        zIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("nx")) {
                        nxIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("ny")) {
                        nyIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("nz")) {
                        nzIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("s")) {
                        uIndex = propertyIndex;
                        propertyIndex++;
                        hasUV = true;
                    }
                    if (tokens[2].equals("t")) {
                        vIndex = propertyIndex;
                        propertyIndex++;
                        hasUV = true;
                    }
                } else if (tokens[0].equals("end_header")) {
                    break;
                }
            }

            while (currentVertex < vertexCount) {
                line = reader.readLine();
                if (line == null) break;
                tokens = line.split("\\s+");
                float x = 0, y = 0, z = 0, nx = 0, ny = 0, nz = 0, u = 0, v = 0;
                try {
                    x = Float.parseFloat(tokens[xIndex]);
                    y = Float.parseFloat(tokens[yIndex]);
                    z = Float.parseFloat(tokens[zIndex]);
                    if (hasNormal) {
                        nx = Float.parseFloat(tokens[nxIndex]);
                        ny = Float.parseFloat(tokens[nyIndex]);
                        nz = Float.parseFloat(tokens[nzIndex]);
                    }
                    if (hasUV) {
                        u = Float.parseFloat(tokens[uIndex]);
                        v = Float.parseFloat(tokens[vIndex]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final Vertex vertex = new Vertex();
                vertex.position.set(x, y, z);
                if (hasNormal) {
                    vertex.normal.set(nx, ny, nz);
                }
                if (hasUV) {
                    vertex.uv.set(u, flipV ? 1f - v : v);
                }
                vertex.index = currentVertex;
                vertices.add(vertex);
                currentVertex++;
            }

            Log.d(PLYAssetLoader.class.getSimpleName(), "vertices " + vertices.size() + " file count " + vertexCount);

            while (currentFace < faceCount) {
                List<Vertex> tempVerts = new ArrayList<>();
                line = reader.readLine();
                if (line == null) break;
                tokens = line.split("\\s+");
                final int n = Integer.parseInt(tokens[0]);
                for (int i = 0; i < n; i++) {
                    tempVerts.add(vertices.get(Integer.parseInt(tokens[i + 1])));
                }
                final Face face = new Face(tempVerts);
                face.update();
                faces.add(face);
                tempVerts.clear();
                currentFace++;
            }
        } finally {
            if (reader != null)
                reader.close();
        }
        Log.d(PLYAssetLoader.class.getSimpleName(), "faces " + faces.size());
    }

    public static MeshData createMeshData(File file) throws IOException {
        return createMeshData(new FileInputStream(file));
    }

    public static MeshData createMeshData(InputStream inputStream) throws IOException {
//        long t = System.currentTimeMillis();
        final FloatArray vertexArray = new FloatArray();
        final ShortArray indexArray = new ShortArray();
        final int vertexSize = parseStream(inputStream, false, vertexArray, indexArray);

        return MeshData.createMeshData(vertexArray.toArray(), indexArray.toArray(), vertexSize);
    }

    public static class PLYLoaderParameters extends ModelLoader.ModelParameters {
        public boolean flipV = true;

        public PLYLoaderParameters() {
        }

        public PLYLoaderParameters(boolean flipV) {
            this.flipV = flipV;
        }
    }
}
