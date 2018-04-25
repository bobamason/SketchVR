package net.masonapps.sketchvr.mesh;

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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Bob on 1/3/2018.
 */

public class MeshUtils {

    public static ModelData createModelData(float[] vertices, short[] indices, VertexAttribute... attributes) {
        final ModelData data = new ModelData();
        ModelNode node = new ModelNode();
        final String nodeId = "node";
        final String meshId = "mesh";
        final String partId = "part";
        final String materialName = "mat0";
        node.id = nodeId;
        node.meshId = meshId;
        node.scale = new Vector3(1, 1, 1);
        node.translation = new Vector3();
        node.rotation = new Quaternion();
        ModelNodePart pm = new ModelNodePart();
        pm.meshPartId = partId;
        pm.materialId = materialName;
        node.parts = new ModelNodePart[]{pm};
        ModelMeshPart part = new ModelMeshPart();
        part.id = partId;
        part.indices = indices;
        part.primitiveType = GL20.GL_TRIANGLES;
        ModelMesh mesh = new ModelMesh();
        mesh.id = meshId;
        mesh.attributes = attributes;
        mesh.vertices = vertices;
        mesh.parts = new ModelMeshPart[]{part};
        data.nodes.add(node);
        data.meshes.add(mesh);
        ModelMaterial mat = new ModelMaterial();
        mat.id = "mat0";
        mat.ambient = new Color(Color.WHITE);
        mat.diffuse = new Color(Color.WHITE);
        mat.opacity = 1f;
        data.materials.add(mat);
        return data;
    }

    public static ModelData createModelData(Vertex[] vertexArray, Triangle[] triangles, long usage) {
        return createModelData(toVertices(vertexArray, usage), toIndices(triangles), VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
    }

    public static float[] toVertices(Vertex[] vertexArray, long usage) {
        final FloatArray vertices = new FloatArray();
        for (Vertex vertex : vertexArray) {
            vertices.add(vertex.position.x);
            vertices.add(vertex.position.y);
            vertices.add(vertex.position.z);
            if ((usage & VertexAttributes.Usage.Normal) == VertexAttributes.Usage.Normal) {
                vertices.add(vertex.normal.x);
                vertices.add(vertex.normal.y);
                vertices.add(vertex.normal.z);
            }
            if ((usage & VertexAttributes.Usage.TextureCoordinates) == VertexAttributes.Usage.TextureCoordinates) {
                vertices.add(vertex.uv.x);
                vertices.add(vertex.uv.y);
            }
        }
        return vertices.toArray();
    }

    public static short[] toIndices(Triangle[] triangles) {
        final ShortArray indices = new ShortArray();
        for (Triangle triangle : triangles) {
            indices.add(triangle.v1.index);
            indices.add(triangle.v2.index);
            indices.add(triangle.v3.index);
        }
        return indices.toArray();
    }
}
