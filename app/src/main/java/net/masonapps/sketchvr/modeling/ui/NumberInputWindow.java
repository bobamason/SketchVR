package net.masonapps.sketchvr.modeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.vrsolidmodeling.Style;

import org.masonapps.libgdxgooglevr.ui.WindowTableVR;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public class NumberInputWindow extends WindowTableVR {


    private final float padding = 8f;
    private TextField textField;
    private InputListener textFieldInputListener;

    public NumberInputWindow(Batch batch, Skin skin, int virtualPixelWidth, int virtualPixelHeight, WindowVrStyle windowStyle) {
        super(batch, skin, virtualPixelWidth, virtualPixelHeight, "", windowStyle);

        addTextField(skin);
        addKeys(skin);
    }

    protected void addTextField(Skin skin) {
        final TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(skin.getFont(Style.DEFAULT_FONT), Color.WHITE, skin.newDrawable(Style.Drawables.white, Color.GRAY), skin.newDrawable(Style.Drawables.white, Color.BLUE), skin.newDrawable(Style.Drawables.white, Color.NAVY));
        textField = new TextField("0.0", textFieldStyle);
        textField.setTextFieldListener((textField, c) -> {

        });
        textField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        textFieldInputListener = textField.getDefaultInputListener();
        getTable().add(textField).pad(0, 0, 0, 0).colspan(3).row();
    }

    private void addKeys(Skin skin) {
        addKey(skin, '7', false);
        addKey(skin, '8', false);
        addKey(skin, '9', true);

        addKey(skin, '4', false);
        addKey(skin, '5', false);
        addKey(skin, '6', true);

        addKey(skin, '1', false);
        addKey(skin, '2', false);
        addKey(skin, '3', true);

        addKey(skin, '.', false);
        addKey(skin, '0', false);
        addKey(skin, '-', true);
    }

    private void addKey(Skin skin, final char c, boolean row) {
        final TextButton key = new TextButton(String.valueOf(c), skin);
        key.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                textFieldInputListener.keyTyped(null, c);
            }
        });
        final Cell<TextButton> cell = getTable().add(key).pad(padding, padding, padding, padding);
        if (row) cell.row();
    }
}
