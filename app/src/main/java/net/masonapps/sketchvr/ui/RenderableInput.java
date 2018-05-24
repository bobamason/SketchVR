package net.masonapps.sketchvr.ui;

import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * Created by Bob Mason on 5/22/2018.
 */
public interface RenderableInput {

    void update();

    void render(ModelBatch modelBatch);
}
