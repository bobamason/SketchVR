package net.masonapps.sketchvr.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.gfx.Entity;

import java.util.List;

/**
 * Created by Bob Mason on 2/9/2018.
 */

public class SketchProjectEntity extends Entity {

    private AABBTree aabbTree;

    public SketchProjectEntity() {
        this(null);
    }

    public SketchProjectEntity(@Nullable List<EditableNode> nodes) {
        super(new ModelInstance(new Model()));
        aabbTree = new AABBTree();
        if (nodes != null && !nodes.isEmpty()) {
            for (EditableNode node : nodes)
                add(node);
        }
    }


    public void add(EditableNode node) {
        if (modelInstance == null) return;
        node.initMesh();

        modelInstance.nodes.add(node);
        modelInstance.model.nodes.add(node);

        final NodePart nodePart = node.parts.get(0);
        modelInstance.model.meshParts.add(nodePart.meshPart);
        modelInstance.model.meshes.add(nodePart.meshPart.mesh);

        modelInstance.materials.add(nodePart.material);
        modelInstance.model.materials.add(nodePart.material);
        aabbTree.insert(node);

        getBounds().set(aabbTree.root.bb);
        getBounds().getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }


    public void remove(EditableNode node) {
        if (modelInstance != null) {

            modelInstance.nodes.removeValue(node, true);
            modelInstance.model.nodes.removeValue(node, true);

            final NodePart nodePart = node.parts.get(0);
            modelInstance.model.meshParts.removeValue(nodePart.meshPart, true);
            modelInstance.model.meshes.removeValue(nodePart.meshPart.mesh, true);

            modelInstance.model.materials.removeValue(nodePart.material, true);
            modelInstance.materials.removeValue(nodePart.material, true);
        }
        aabbTree.remove(node);
    }

    public void update() {
        if (modelInstance == null) return;
        for (int i = 0; i < modelInstance.nodes.size; i++) {
            final Node node = modelInstance.nodes.get(i);
            if (node instanceof EditableNode)
                ((EditableNode) node).validate();
        }
        validate();
    }

    @Override
    public boolean isInCameraFrustum(Camera camera) {
        return true;
    }

    @Nullable
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        final Ray tmpRay = Pools.obtain(Ray.class);
        final Matrix4 tmpMat = Pools.obtain(Matrix4.class);

        tmpRay.set(ray).mul(inverseTransform);
        ray.direction.nor();
        intersection.object = null;
        final boolean rayTest = aabbTree.rayTest(tmpRay, intersection);
        if (rayTest) {
            intersection.hitPoint.mul(transform);
            intersection.normal.rot(transform);
        }
        Pools.free(tmpRay);
        Pools.free(tmpMat);
        return rayTest;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (modelInstance != null) {
            modelInstance.model.dispose();
            modelInstance = null;
        }
    }

    public AABBTree getAABBTree() {
        return aabbTree;
    }
}
