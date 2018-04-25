package net.masonapps.sketchvr.io;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.masonapps.sketchvr.mesh.MeshInfo;
import net.masonapps.sketchvr.modeling.EditableNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 1/15/2018.
 */

public class ProjectFileIO {

    public static JSONArray toJSONArray(List<EditableNode> objects) throws JSONException {
        final JSONArray jsonArray = new JSONArray();
        for (EditableNode object : objects) {
            jsonArray.put(object.toJSONObject());
        }
        return jsonArray;
    }

    public static List<EditableNode> fromJSONArray(JSONArray jsonArray) throws JSONException {
        final ArrayList<EditableNode> objects = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            objects.add(EditableNode.fromJSONObject(jsonArray.getJSONObject(i)));
        }
        return objects;
    }

    public static void saveFile(File file, List<EditableNode> objects) throws IOException, JSONException {
        BufferedWriter writer = null;
        try {
            final JSONArray jsonArray = toJSONArray(objects);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(jsonArray.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static List<EditableNode> loadFile(File file) throws IOException, JSONException {
        BufferedReader reader = null;
        final ArrayList<EditableNode> objects = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            final StringBuilder sb = new StringBuilder();
            final char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, count);
            }
            objects.addAll(fromJSONArray(new JSONArray(sb.toString())));
        } finally {
            if (reader != null)
                reader.close();
        }
        return objects;
    }

    public static class ModelDataLoader {

        private final File file;
        private final BoundingBox outBounds;
        private int index = 0;

        public ModelDataLoader(File file, BoundingBox outBounds) {
            this.file = file;
            this.outBounds = outBounds;
        }

        private static MeshInfo parseMesh(JSONObject jsonObject) throws JSONException {
            final MeshInfo mesh = new MeshInfo();
            mesh.numVertices = jsonObject.getInt(EditableNode.KEY_VERTEX_COUNT);
            mesh.numIndices = jsonObject.getInt(EditableNode.KEY_INDEX_COUNT);
            mesh.vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));

            final String vertexString = jsonObject.getString(EditableNode.KEY_VERTICES);
            mesh.vertices = new float[mesh.vertexAttributes.vertexSize / Float.BYTES * mesh.numVertices];
            Base64Utils.decodeFloatArray(vertexString, mesh.vertices);

            final String indexString = jsonObject.getString(EditableNode.KEY_INDICES);
            mesh.indices = new short[mesh.numIndices];
            Base64Utils.decodeShortArray(indexString, mesh.indices);
            return mesh;
        }

        public ModelData loadModelData() throws IOException, JSONException {
            BufferedReader reader = null;
            final ModelData data = new ModelData();
            try {
                reader = new BufferedReader(new FileReader(file));
                final StringBuilder sb = new StringBuilder();
                final char[] buffer = new char[1024];
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, count);
                }

                final JSONArray jsonArray = new JSONArray(sb.toString());
                if (outBounds != null) {
                    if (jsonArray.length() == 0)
                        outBounds.set(new Vector3(-0.5f, -0.5f, -0.5f), new Vector3(0.5f, 0.5f, 0.5f));
                    else
                        outBounds.inf();

                }
                parseJSONArray(data, jsonArray, outBounds);

            } finally {
                if (reader != null)
                    reader.close();
            }
            return data;
        }

        private void parseJSONArray(ModelData data, JSONArray jsonArray, @Nullable BoundingBox outBounds) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);
                final String primitiveKey = jsonObject.optString(EditableNode.KEY_PRIMITIVE, EditableNode.KEY_GROUP);
                if (primitiveKey.equals(EditableNode.KEY_GROUP)) {
                    if (jsonObject.has(EditableNode.KEY_CHILDREN)) {
                        final JSONArray children = jsonObject.optJSONArray(EditableNode.KEY_CHILDREN);
                        if (children != null)
                            parseJSONArray(data, children, outBounds);
                    }
                } else if(primitiveKey.equals(EditableNode.KEY_MESH)){
                    addModelNode(data, jsonObject, outBounds);
                }
            }
        }

        private void addModelNode(ModelData data, JSONObject jsonObject, @Nullable BoundingBox outBounds) throws JSONException {
            if (!jsonObject.has(EditableNode.KEY_MESH)) 
                return;
            
            index++;
            ModelNode node = new ModelNode();
            final String nodeId = "node" + index;
            final String meshId = "mesh" + index;
            node.id = nodeId;
            node.meshId = meshId;
            node.translation = new Vector3().fromString(jsonObject.optString(EditableNode.KEY_POSITION, "(0.0,0.0,0.0)"));
            final String rotationString = jsonObject.optString(EditableNode.KEY_ROTATION, "(0.0,0.0,0.0,1.0)");
            node.rotation = new Quaternion(JsonUtils.quaternionFromString(rotationString));
            node.scale = new Vector3().fromString(jsonObject.optString(EditableNode.KEY_SCALE, "(1.0,1.0,1.0)"));

            ModelNodePart pm = new ModelNodePart();
            final String partId = "part" + index;
            final String materialName = "mat" + index;
            pm.meshPartId = partId;
            pm.materialId = materialName;
            node.parts = new ModelNodePart[]{pm};
            data.nodes.add(node);

            final MeshInfo meshInfo;
                    meshInfo = parseMesh(jsonObject.getJSONObject(EditableNode.KEY_MESH));

            final float[] vertices = new float[meshInfo.vertices.length];
            System.arraycopy(meshInfo.vertices, 0, vertices, 0, meshInfo.vertices.length);
            final short[] indices = new short[meshInfo.indices.length];
            System.arraycopy(meshInfo.indices, 0, indices, 0, meshInfo.indices.length);
            final int n = meshInfo.vertexAttributes.size();
            final VertexAttribute[] attributes = new VertexAttribute[n];
            for (int i = 0; i < n; i++) {
                attributes[i] = meshInfo.vertexAttributes.get(i);
            }

            if (outBounds != null) {
                final Vector3 tmp = new Vector3();
                final Matrix4 transform = new Matrix4();
                transform.set(node.translation, node.rotation, node.scale);
                final int vertexSize = meshInfo.vertexAttributes.vertexSize / Float.BYTES;
                for (int i = 0; i < vertices.length; i += vertexSize) {
                    outBounds.ext(tmp.set(vertices[i], vertices[i + 1], vertices[i + 2]).mul(transform));
                }
            }

            ModelMeshPart part = new ModelMeshPart();
            part.id = partId;
            part.indices = indices;
            part.primitiveType = GL20.GL_TRIANGLES;

            ModelMesh mesh = new ModelMesh();
            mesh.id = meshId;
            mesh.attributes = attributes;
            mesh.vertices = vertices;
            mesh.parts = new ModelMeshPart[]{part};
            data.meshes.add(mesh);

            ModelMaterial mat = new ModelMaterial();
            mat.id = materialName;
            mat.ambient = Color.valueOf(jsonObject.optString(EditableNode.KEY_AMBIENT, "000000FF"));
            mat.diffuse = Color.valueOf(jsonObject.optString(EditableNode.KEY_DIFFUSE, "7F7F7FFF"));
            mat.specular = Color.valueOf(jsonObject.optString(EditableNode.KEY_SPECULAR, "000000FF"));
            mat.shininess = (float) jsonObject.optDouble(EditableNode.KEY_SHININESS, 8.);
            mat.opacity = 1f;
            data.materials.add(mat);
        }
    }
} 
