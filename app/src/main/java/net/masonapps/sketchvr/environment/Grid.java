package net.masonapps.sketchvr.environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;

import org.masonapps.libgdxgooglevr.gfx.Transformable;
import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob on 6/6/2017.
 */

public class Grid extends Transformable implements Disposable {

    private final Shader gridShader;
    private final ModelInstance modelInstance;
    private Model rect;
    private Environment environment;

    public Grid(float sectionSize, TextureRegion textureRegion, Color color) {
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Material material = new Material(TextureAttribute.createDiffuse(textureRegion), ColorAttribute.createDiffuse(color), new BlendingAttribute(true, 0.8f), FloatAttribute.createAlphaTest(0.25f), IntAttribute.createCullFace(0));
        final float radius = sectionSize / 2f;
        modelBuilder.begin();
        final MeshPartBuilder partBuilder = modelBuilder.part("rect", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates, material);
        final int n = 5;
        for (int row = -n; row < n; row++) {
            for (int col = -n; col < n; col++) {
                final float x = col * sectionSize;
                final float z = row * sectionSize;
                partBuilder.rect(
                        x - radius, 0f, z + radius,
                        x + radius, 0f, z + radius,
                        x + radius, 0f, z - radius,
                        x - radius, 0f, z - radius,
                        0f, 1f, 0f
                );
            }
        }
        rect = modelBuilder.end();
        modelInstance = new ModelInstance(rect, 0, -1.2f, 0);
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.Fog, Color.RED));
        final Renderable renderable = modelInstance.getRenderable(new Renderable());
        gridShader = new DefaultShader(renderable);
    }

    public void render(ModelBatch batch) {
        batch.render(modelInstance, environment);
    }

    @Override
    public void dispose() {
        if (rect != null) {
            rect.dispose();
            rect = null;
        }
    }

    private static class GridShader extends DefaultShader {

        private static String vertexShader = null;
        private static String fragmentShader = null;

        public GridShader(Renderable renderable) {
            this(renderable, new Config());
        }

        public GridShader(Renderable renderable, Config config) {
            super(renderable, config, createPrefix(renderable, config), getVertexShader(), getFragmentShader());
            Logger.d("grid shader program log: " + program.getLog());
        }

        public static String getVertexShader() {
            if (vertexShader == null)
                vertexShader = Gdx.files.internal("shaders/grid.vertex.glsl").readString();
            return vertexShader;
        }

        public static String getFragmentShader() {
            if (fragmentShader == null)
                fragmentShader = Gdx.files.internal("shaders/grid.fragment.glsl").readString();
            return fragmentShader;
        }
    }
}
