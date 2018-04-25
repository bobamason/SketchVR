package net.masonapps.sketchvr.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

import net.masonapps.sketchvr.bvh.BVH;
import net.masonapps.sketchvr.mesh.MeshInfo;

/**
 * Created by Bob Mason on 3/20/2018.
 */

public class DynamicNode extends EditableNode {

    public DynamicNode() {
        super(new MeshInfo(), new BVH(new BVH.Group()));
    }

    @Override
    public void initMesh() {
        if (parts.size > 0 || meshInfo == null) return;
        final Mesh mesh = meshInfo.createDynamicMesh();
        final MeshPart meshPart = new MeshPart();
        meshPart.id = "part";
        meshPart.primitiveType = GL20.GL_TRIANGLES;
        meshPart.mesh = mesh;
        meshPart.offset = 0;
        meshPart.size = mesh.getNumIndices();
        parts.add(new NodePart(meshPart, createDefaultMaterial()));
        updateBounds();
        invalidate();
    }

    @Nullable
    public Mesh getMesh() {
        if (parts.size < 1) return null;
        return parts.get(0).meshPart.mesh;
    }
}
