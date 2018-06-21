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
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.util.List;

/**
 * Created by Bob Mason on 2/9/2018.
 */

public class SketchProjectEntity extends Entity {

    private final AABBTree aabbTree;

    public SketchProjectEntity() {
        this(null);
    }

    @SuppressWarnings("ConstantConditions")
    public SketchProjectEntity(@Nullable List<SketchNode> nodes) {
        super(new ModelInstance(new Model()));
        aabbTree = new AABBTree();
        if (nodes != null && !nodes.isEmpty()) {
            for (SketchNode node : nodes) {
                add(node, true);
            }
        }
    }


    public void add(SketchNode node, boolean insertIntoTree) {
        if (modelInstance == null) return;

        modelInstance.nodes.add(node);
        modelInstance.model.nodes.add(node);

        final NodePart nodePart = node.parts.get(0);
        modelInstance.model.meshParts.add(nodePart.meshPart);
        modelInstance.model.meshes.add(nodePart.meshPart.mesh);

        modelInstance.materials.add(nodePart.material);
        modelInstance.model.materials.add(nodePart.material);

        if (insertIntoTree)
            insertIntoAABBTree(node);
    }

    public void insertIntoAABBTree(SketchNode node) {
        node.updateBounds();
        node.validate();
        aabbTree.insert(node);
        getBounds().set(aabbTree.root.bb);
        updateDimensions();
    }

    public void remove(SketchNode node) {
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
            if (node instanceof SketchNode)
                ((SketchNode) node).validate();
        }
        validate();
    }

    @Override
    public boolean isInCameraFrustum(Camera camera) {
        return true;
    }

    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        final Ray tmpRay = Pools.obtain(Ray.class);
        final Matrix4 tmpMat = Pools.obtain(Matrix4.class);

        tmpRay.set(ray).mul(inverseTransform);
        ray.direction.nor();
        intersection.object = null;
        final boolean rayTest = aabbTree.rayTest(tmpRay, intersection);
        if (rayTest) {
            intersection.hitPoint.mul(transform);
            Logger.d(intersection.toString());
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
