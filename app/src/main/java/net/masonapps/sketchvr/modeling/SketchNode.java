package net.masonapps.sketchvr.modeling;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.sketchvr.actions.TransformAction;
import net.masonapps.sketchvr.io.Base64Utils;
import net.masonapps.sketchvr.io.JsonUtils;
import net.masonapps.sketchvr.mesh.MeshInfo;
import net.masonapps.sketchvr.mesh.Triangle;
import net.masonapps.sketchvr.mesh.Vertex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 2/9/2018.
 */

public class SketchNode extends Node implements AABBTree.AABBObject {

    public static final IntSet usedIndices = new IntSet();
    public static final String KEY_PRIMITIVE = "primitive";
    public static final String KEY_MESH = "mesh";
    public static final String KEY_GROUP = "group";
    public static final String KEY_CHILDREN = "children";
    public static final String KEY_VERTEX_COUNT = "numVertices";
    public static final String KEY_VERTICES = "vertices";
    public static final String KEY_INDEX_COUNT = "numIndices";
    public static final String KEY_INDICES = "indices";
    public static final String KEY_POSITION = "position";
    public static final String KEY_ROTATION = "rotation";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_AMBIENT = "ambient";
    public static final String KEY_DIFFUSE = "diffuse";
    public static final String KEY_SPECULAR = "specular";
    public static final String KEY_SHININESS = "shininess";
    protected final Matrix4 inverseTransform = new Matrix4();
    private final Ray transformedRay = new Ray();
    private final boolean isGroup;
    protected BoundingBox bounds = new BoundingBox();
    private boolean updated = false;
    @Nullable
    private AABBTree.Node node = null;
    private BoundingBox aabb = new BoundingBox();
    private Color ambientColor = new Color(Color.GRAY);
    private Color diffuseColor = new Color(Color.GRAY);
    private Color specularColor = new Color(0x3f3f3fff);
    private float shininess = 8f;
    private AABBTree meshAABBTree = new AABBTree();

    public SketchNode() {
        super();
        isGroup = true;
    }

    public SketchNode(@NonNull MeshPart meshPart) {
        this(meshPart, Color.GRAY);
    }

    public SketchNode(@NonNull MeshPart meshPart, Color color) {
        super();
        parts.add(new NodePart(meshPart, createDefaultMaterial(color)));
        recenter(meshPart);
        buildMeshAABBTree(meshPart);
        invalidate();
        isGroup = false;
    }

//    private void recenter(MeshPart meshPart) {
//        usedIndices.clear();
//        usedIndices.ensureCapacity(meshPart.size);
//        final Vector3 center = meshPart.center;
//        final int stride = meshPart.mesh.getVertexSize() / Float.BYTES;
//        final FloatBuffer verticesBuffer = meshPart.mesh.getVerticesBuffer();
//        final ShortBuffer indicesBuffer = meshPart.mesh.getIndicesBuffer();
//        verticesBuffer.position(0);
//        indicesBuffer.position(0);
//        for (int i = meshPart.offset; i < meshPart.size + meshPart.offset; i++) {
//            final short index = indicesBuffer.get(i);
//            if (usedIndices.contains(index))
//                continue;
//            final int iv = index * stride;
//            float x = verticesBuffer.get(iv);
//            verticesBuffer.put(iv, x - center.x);
//            float y = verticesBuffer.get(iv + 1);
//            verticesBuffer.put(iv + 1, y - center.y);
//            float z = verticesBuffer.get(iv + 2);
//            verticesBuffer.put(iv + 2, z - center.z);
//            usedIndices.add(index);
//        }
////        Logger.d("meshPart " + meshPart.id + " indices = " + usedIndices.toString());
//        usedIndices.clear();
//        setPosition(meshPart.center);
//        meshPart.center.set(0, 0, 0);
//        meshPart.mesh.updateVertices(0, new float[0]);
//    }

    private void buildMeshAABBTree(MeshPart meshPart) {
//        usedIndices.clear();
//        usedIndices.ensureCapacity(meshPart.size);
        final int stride = meshPart.mesh.getVertexSize() / Float.BYTES;
        final FloatBuffer verticesBuffer = meshPart.mesh.getVerticesBuffer();
        final ShortBuffer indicesBuffer = meshPart.mesh.getIndicesBuffer();
        verticesBuffer.position(0);
        indicesBuffer.position(0);
        final List<Vertex> vertexList = new ArrayList<>();
        for (int i = meshPart.offset; i < meshPart.size + meshPart.offset; i++) {
            final short index = indicesBuffer.get(i);
            final int iv = index * stride;
            if (usedIndices.contains(index))
                continue;
            final Vertex v = new Vertex();
            v.position.x = verticesBuffer.get(iv);
            v.position.y = verticesBuffer.get(iv + 1);
            v.position.z = verticesBuffer.get(iv + 2);
            vertexList.add(v);
        }

        indicesBuffer.position(0);
        for (int i = meshPart.offset; i < meshPart.size + meshPart.offset; i += 3) {
            final short i1 = indicesBuffer.get(i);
            final short i2 = indicesBuffer.get(i + 1);
            final short i3 = indicesBuffer.get(i + 2);
            meshAABBTree.insert(new Triangle(vertexList.get(i1), vertexList.get(i2), vertexList.get(i3)));
        }
        usedIndices.clear();
    }

    /**
     * only works when there is one mesh per node
     *
     * @param meshPart with meshPart.offset = 0 meshPart.size = mesh.getNumIndices()
     */
    private void recenter(MeshPart meshPart) {
        setPosition(meshPart.center);
        meshPart.mesh.transform(new Matrix4().translate(-meshPart.center.x, -meshPart.center.y, -meshPart.center.z));
        meshPart.center.set(0, 0, 0);
    }

    public static SketchNode fromJSONObject(JSONObject jsonObject) throws JSONException {
        // TODO: 4/30/2018 fix load mesh 
        final String primitiveKey = jsonObject.optString(KEY_PRIMITIVE, KEY_GROUP);
        SketchNode sketchNode = null;

        if (primitiveKey.equals(KEY_GROUP)) {
            sketchNode = new SketchNode();
            final JSONArray children = jsonObject.getJSONArray(KEY_CHILDREN);
            if (children != null) {
                for (int i = 0; i < children.length(); i++) {
                    sketchNode.addChild(fromJSONObject(children.getJSONObject(i)));
                }
            }
        } else if (primitiveKey.equals(KEY_MESH) && jsonObject.has(KEY_MESH)) {
            final MeshInfo meshInfo = parseMesh(jsonObject.getJSONObject(KEY_MESH));
            // TODO: 4/30/2018  
//            sketchNode = new SketchNode(part);
        } else {
            return null;
        }
        final Color ambient = Color.valueOf(jsonObject.optString(KEY_AMBIENT, "000000FF"));
        final Color diffuse = Color.valueOf(jsonObject.optString(KEY_DIFFUSE, "7F7F7FFF"));
        final Color specular = Color.valueOf(jsonObject.optString(KEY_SPECULAR, "000000FF"));
        final float shininess = (float) jsonObject.optDouble(KEY_SHININESS, 8.);
        sketchNode.setAmbientColor(ambient);
        sketchNode.setDiffuseColor(diffuse);
        sketchNode.setSpecularColor(specular);
        sketchNode.setShininess(shininess);
        sketchNode.translation.fromString(jsonObject.optString(KEY_POSITION, "(0.0,0.0,0.0)"));
        final String rotationString = jsonObject.optString(KEY_ROTATION, "(0.0,0.0,0.0,1.0)");
        sketchNode.rotation.set(JsonUtils.quaternionFromString(rotationString));
        sketchNode.scale.fromString(jsonObject.optString(KEY_SCALE, "(1.0,1.0,1.0)"));
        sketchNode.calculateTransforms(true);
        return sketchNode;
    }

    private static MeshInfo parseMesh(JSONObject jsonObject) throws JSONException {
        final MeshInfo mesh = new MeshInfo();
        mesh.numVertices = jsonObject.getInt(KEY_VERTEX_COUNT);
        mesh.numIndices = jsonObject.getInt(KEY_INDEX_COUNT);
        mesh.vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));

        final String vertexString = jsonObject.getString(KEY_VERTICES);
        mesh.vertices = new float[mesh.vertexAttributes.vertexSize / Float.BYTES * mesh.numVertices];
        Base64Utils.decodeFloatArray(vertexString, mesh.vertices);

        final String indexString = jsonObject.getString(KEY_INDICES);
        mesh.indices = new short[mesh.numIndices];
        Base64Utils.decodeShortArray(indexString, mesh.indices);
        return mesh;
    }

    public boolean isGroup() {
        return isGroup;
    }

    protected Material createDefaultMaterial(Color color) {
        ambientColor.set(color);
        diffuseColor.set(color);
        return new Material(ColorAttribute.createAmbient(ambientColor),
                ColorAttribute.createDiffuse(diffuseColor),
                ColorAttribute.createSpecular(specularColor),
                FloatAttribute.createShininess(shininess));
    }

    public void updateBounds() {
        if (parts.size == 0) {
            bounds.clr();
            return;
        }
        bounds.inf();
        for (NodePart part : parts) {
            final MeshPart meshPart = part.meshPart;
            final Vector3 halfExtents = meshPart.halfExtents;
            final Vector3 center = meshPart.center;
            bounds.ext(center.x + halfExtents.x, center.y + halfExtents.y, center.z + halfExtents.z);
            bounds.ext(center.x - halfExtents.x, center.y - halfExtents.y, center.z - halfExtents.z);
        }
    }

    @Nullable
    @Override
    public AABBTree.Node getNode() {
        return node;
    }

    @Override
    public void setNode(@Nullable AABBTree.Node node) {
        this.node = node;
    }

    @Override
    public BoundingBox getAABB() {
        validate();
        return aabb;
    }

    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        validate();
        // TODO: 4/27/2018 ray test shape or path
        boolean rayTest;
        transformedRay.set(ray).mul(inverseTransform);
        if (isGroup || meshAABBTree == null) {
            rayTest = Intersector.intersectRayBounds(transformedRay, bounds, intersection.hitPoint);
            intersection.object = this;
        } else
            rayTest = meshAABBTree.rayTest(transformedRay, intersection);
        if (rayTest) {
            intersection.hitPoint.mul(getTransform());
            Logger.d("node ray test hitPoint = " + intersection.hitPoint);
            if (intersection.object instanceof Triangle)
                Logger.d("node ray test normal = " + ((Triangle) intersection.object).plane.normal);
            intersection.t = ray.origin.dst(intersection.hitPoint);
        }
        return rayTest;
    }

    public void validate() {
        if (!updated)
            calculateTransforms(true);
    }

    public void invalidate() {
        updated = false;
    }

    @Override
    public void calculateTransforms(boolean recursive) {
        super.calculateTransforms(recursive);
        try {
//            inverseTransform.set(-translation.x, -translation.y, -translation.z, -rotation.x, -rotation.y, -rotation.z, rotation.w, 1f / scale.x, 1f / scale.y, 1f / scale.z);
            inverseTransform.set(localTransform).inv();
        } catch (Exception ignored) {
        }
        if (isGroup && getChildCount() > 0) {
            bounds.inf();
            final Iterable<Node> children = getChildren();
            for (Node child : children) {
                if (child instanceof SketchNode)
                    bounds.ext(((SketchNode) child).getAABB());
            }
        }
        aabb.set(bounds).mul(localTransform);
        updated = true;
    }

    @Override
    public SketchNode copy() {
        validate();
        final SketchNode node;
        if (isGroup)
            node = new SketchNode();
        else if (parts.size > 0)
            node = new SketchNode(parts.get(0).meshPart);
        else
            return null;
        node.translation.set(translation);
        node.rotation.set(rotation);
        node.scale.set(scale);
        node.localTransform.set(localTransform);
        node.globalTransform.set(globalTransform);
        if (isGroup) {
            final Iterable<Node> children = getChildren();
            for (Node child : children) {
                if (child instanceof SketchNode)
                    node.addChild(((SketchNode) child).copy());
            }
        }
        node.ambientColor.set(ambientColor);
        node.diffuseColor.set(diffuseColor);
        node.specularColor.set(specularColor);
        node.shininess = shininess;
        return node;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(Color color) {
        ambientColor.set(color);
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final ColorAttribute ambient = (ColorAttribute) material.get(ColorAttribute.Ambient);
        if (ambient != null)
            ambient.color.set(color);
        else
            material.set(ColorAttribute.createAmbient(color));
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Color color) {
        diffuseColor.set(color);
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final ColorAttribute diffuse = (ColorAttribute) material.get(ColorAttribute.Diffuse);
        if (diffuse != null)
            diffuse.color.set(color);
        else
            material.set(ColorAttribute.createDiffuse(color));
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Color color) {
        specularColor.set(color);
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final ColorAttribute specular = (ColorAttribute) material.get(ColorAttribute.Specular);
        if (specular != null)
            specular.color.set(color);
        else
            material.set(ColorAttribute.createSpecular(color));
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float value) {
        this.shininess = value;
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final FloatAttribute shininess = (FloatAttribute) material.get(FloatAttribute.Shininess);
        if (shininess != null)
            shininess.value = value;
        else
            material.set(FloatAttribute.createShininess(value));
    }

    public JSONObject toJSONObject() throws JSONException {
        validate();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_PRIMITIVE, isGroup ? KEY_GROUP : KEY_MESH);
        if (isGroup) {
            final Iterable<Node> children = getChildren();
            JSONArray jsonArray = new JSONArray();
            for (Node child : children) {
                if (child instanceof SketchNode)
                    jsonArray.put(((SketchNode) child).toJSONObject());
            }
            jsonObject.put(KEY_CHILDREN, jsonArray);
        } else {
            jsonObject.put(KEY_MESH, meshPartToJsonObject(parts.get(0).meshPart));
        }
        jsonObject.put(KEY_AMBIENT, getAmbientColor().toString());
        jsonObject.put(KEY_DIFFUSE, getDiffuseColor().toString());
        jsonObject.put(KEY_SPECULAR, getSpecularColor().toString());
        jsonObject.put(KEY_SHININESS, getShininess());
        jsonObject.put(KEY_POSITION, translation.toString());
        jsonObject.put(KEY_ROTATION, JsonUtils.quaternionToString(rotation));
        jsonObject.put(KEY_SCALE, scale.toString());
        return jsonObject;
    }

    private JSONObject meshPartToJsonObject(MeshPart meshPart) {
        // TODO: 4/30/2018 implement 
        return new JSONObject();
    }

    private JSONObject meshToJsonObject(Mesh mesh) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        final int numVertices = mesh.getNumVertices();
        final int numIndices = mesh.getNumIndices();
        final float[] vertices = new float[mesh.getVertexSize() / Float.BYTES * numVertices];
        mesh.getVertices(vertices);
        final short[] indices = new short[numIndices];
        mesh.getIndices(indices);
        jsonObject.put(KEY_VERTEX_COUNT, numVertices);
        jsonObject.put(KEY_INDEX_COUNT, numIndices);
        jsonObject.put(KEY_VERTICES, Base64Utils.encode(vertices));
        jsonObject.put(KEY_INDICES, Base64Utils.encode(indices));
        // TODO: 4/30/2018 move to SketchMeshBuilder
        return jsonObject;
    }

    /**
     * Methods needed to make it compatible with other code written earlier
     * |
     * |
     * |
     * |
     * \   |   /
     * \  |  /
     * \ | /
     * \|/
     */

    public Matrix4 getTransform(Matrix4 out) {
        validate();
        return out.set(localTransform);
    }

    public TransformAction.Transform getTransform(TransformAction.Transform out) {
        validate();
        out.position.set(getPosition());
        out.rotation.set(getRotation());
        out.scale.set(getScale());
        return out;
    }

    public Matrix4 getTransform() {
        validate();
        return localTransform;
    }

    public void setTransform(TransformAction.Transform transform) {
        setPosition(transform.position);
        setRotation(transform.rotation);
        final Vector3 s = transform.scale;
        setScale(s.x, s.y, s.z);
    }

    public SketchNode setScale(float x, float y, float z) {
        scale.set(x, y, z);
        invalidate();
        return this;
    }

    public SketchNode scaleX(float x) {
        scale.x *= x;
        invalidate();
        return this;
    }

    public SketchNode scaleY(float y) {
        scale.y *= y;
        invalidate();
        return this;
    }

    public SketchNode scaleZ(float z) {
        scale.z *= z;
        invalidate();
        return this;
    }

    public SketchNode scale(float s) {
        scale.scl(s, s, s);
        invalidate();
        return this;
    }

    public SketchNode scale(float x, float y, float z) {
        scale.scl(x, y, z);
        invalidate();
        return this;
    }

    public float getScaleX() {
        return this.scale.x;
    }

    public SketchNode setScaleX(float x) {
        scale.x = x;
        invalidate();
        return this;
    }

    public float getScaleY() {
        return this.scale.y;
    }

    public SketchNode setScaleY(float y) {
        scale.y = y;
        invalidate();
        return this;
    }

    public float getScaleZ() {
        return this.scale.z;
    }

    public SketchNode setScaleZ(float z) {
        scale.z = z;
        invalidate();
        return this;
    }

    public SketchNode setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
        return this;
    }

    public SketchNode setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
        return this;
    }

    public SketchNode setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
        return this;
    }

    public SketchNode rotateX(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public SketchNode rotateY(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public SketchNode rotateZ(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public SketchNode setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
        return this;
    }

    public SketchNode setRotation(Vector3 dir, Vector3 up) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
        Pools.free(tmp);
        Pools.free(tmp2);
        return this;
    }

    public SketchNode lookAt(Vector3 position, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(position).sub(this.translation).nor();
        setRotation(dir, up);
        Pools.free(dir);
        return this;
    }

    public Quaternion getRotation() {
        validate();
        return rotation;
    }

    public SketchNode setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
        return this;
    }

    public SketchNode translateX(float units) {
        this.translation.x += units;
        invalidate();
        return this;
    }

    public float getX() {
        return this.translation.x;
    }

    public SketchNode setX(float x) {
        this.translation.x = x;
        invalidate();
        return this;
    }

    public SketchNode translateY(float units) {
        this.translation.y += units;
        invalidate();
        return this;
    }

    public float getY() {
        return this.translation.y;
    }

    public SketchNode setY(float y) {
        this.translation.y = y;
        invalidate();
        return this;
    }

    public SketchNode translateZ(float units) {
        this.translation.z += units;
        invalidate();
        return this;
    }

    public float getZ() {
        return this.translation.z;
    }

    public SketchNode setZ(float z) {
        this.translation.z = z;
        invalidate();
        return this;
    }

    public SketchNode translate(float x, float y, float z) {
        this.translation.add(x, y, z);
        invalidate();
        return this;
    }

    public SketchNode translate(Vector3 translate) {
        this.translation.add(translate);
        invalidate();
        return this;
    }

    public SketchNode setPosition(float x, float y, float z) {
        this.translation.set(x, y, z);
        invalidate();
        return this;
    }

    public Vector3 getPosition() {
        validate();
        return translation;
    }

    public SketchNode setPosition(Vector3 pos) {
        this.translation.set(pos);
        invalidate();
        return this;
    }

    public Matrix4 getInverseTransform(Matrix4 out) {
        return out.set(inverseTransform);
    }

    public Matrix4 getInverseTransform() {
        return inverseTransform;
    }

    public boolean isUpdated() {
        return updated;
    }

    public Vector3 getScale() {
        validate();
        return scale;
    }

    public SketchNode setScale(float scale) {
        this.scale.set(scale, scale, scale);
        invalidate();
        return this;
    }

    public BoundingBox getBounds() {
        return bounds;
    }
}
