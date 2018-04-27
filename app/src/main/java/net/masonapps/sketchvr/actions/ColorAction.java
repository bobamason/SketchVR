package net.masonapps.sketchvr.actions;

import com.badlogic.gdx.graphics.Color;

import net.masonapps.sketchvr.modeling.SketchNode;

import java.util.function.Consumer;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class ColorAction extends Action {

    private final Color oldColor;
    private final Color newColor;
    private final Consumer<Color> consumer;

    public ColorAction(SketchNode node, Color oldColor, Color newColor, Consumer<Color> consumer) {
        super(node);
        this.oldColor = oldColor;
        this.newColor = newColor;
        this.consumer = consumer;
    }

    @Override
    public void redoAction() {
        getNode().setAmbientColor(newColor);
        getNode().setDiffuseColor(newColor);
        consumer.accept(newColor);
    }

    @Override
    public void undoAction() {
        getNode().setAmbientColor(oldColor);
        getNode().setDiffuseColor(oldColor);
        consumer.accept(oldColor);
    }
}
