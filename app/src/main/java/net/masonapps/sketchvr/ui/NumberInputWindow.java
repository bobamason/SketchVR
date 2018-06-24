package net.masonapps.sketchvr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.sketchvr.Style;

import org.mariuszgromada.math.mxparser.Expression;
import org.masonapps.libgdxgooglevr.ui.WindowTableVR;
import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public class NumberInputWindow extends WindowTableVR {


    private final float padding = 8f;
    private TextField textField;
    private InputListener textFieldInputListener;

    public NumberInputWindow(Batch batch, Skin skin, WindowVrStyle windowStyle) {
        super(batch, skin, 100, 100, "", windowStyle);

        addTextField(skin);
        addKeys(skin);
        resizeToFitTable();
    }

    protected void addTextField(Skin skin) {
        final TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(skin.getFont(Style.DEFAULT_FONT), Color.WHITE.cpy(), skin.newDrawable(Style.Drawables.white, Color.GRAY), skin.newDrawable(Style.Drawables.white, Color.BLUE), skin.newDrawable(Style.Drawables.white, Color.NAVY));
        textField = new TextField("0.0", textFieldStyle);
        textField.setTextFieldListener((textField, c) -> {
            // TODO: 6/21/2018 add value listener 
            final Expression expression = new Expression(textField.getText());
            if (expression.checkSyntax()) {
                textField.getStyle().fontColor.set(Color.WHITE);
                Logger.d("value = " + expression.calculate());
            } else {
                textField.getStyle().fontColor.set(Color.RED);
            }
        });
        textFieldInputListener = textField.getDefaultInputListener();
        setKeyboardFocus(textField);
        getTable().add(textField).pad(0, 0, 0, 0).colspan(3).row();
    }

    private void addKeys(Skin skin) {
        addKey(skin, '7', false);
        addKey(skin, '8', false);
        addKey(skin, '9', false);
        addKey(skin, '/', true);

        addKey(skin, '4', false);
        addKey(skin, '5', false);
        addKey(skin, '6', false);
        addKey(skin, '*', true);

        addKey(skin, '1', false);
        addKey(skin, '2', false);
        addKey(skin, '3', false);
        addKey(skin, '-', true);

        final TextButton key = new TextButton("DEL", skin);
        key.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                textFieldInputListener.keyTyped(null, (char) 8);
            }
        });
        getTable().add(key).right().pad(padding, padding, padding, padding);
        addKey(skin, '.', false);
        addKey(skin, '0', false);
        addKey(skin, '+', false);
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
