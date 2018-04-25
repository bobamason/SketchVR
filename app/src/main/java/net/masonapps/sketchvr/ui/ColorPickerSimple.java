package net.masonapps.sketchvr.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.masonapps.sketchvr.Style;

import java.util.function.Consumer;

/**
 * Created by Bob on 8/3/2017.
 */

public class ColorPickerSimple extends Table {

    public static final int PADDING = 10;
    @Nullable
    private Consumer<Color> colorListener = null;
    private Texture colorGridTexture;
    private Image highlightImage;
    private int[][] colors;
    private Image colorGridImage;

    public ColorPickerSimple(Skin skin, int width, int height) {
        super(skin);
        init(skin, width, height);
    }

    private void init(Skin skin, int tableWidth, int tableHeight) {
        colors = new int[16][16];
        Pixmap pixmap = new Pixmap(tableWidth, tableHeight, Pixmap.Format.RGBA8888);
        final int w = pixmap.getWidth() / 16;
        final int h = pixmap.getHeight() / 16;
        final float[] hsv = new float[]{0, 0, 0};
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                hsv[0] = col == 0 ? 0 : 360 / 15f * (col - 1);
                hsv[1] = col == 0 ? 0 : (row < 5 ? (row + 1f) / 5f : 1);
                hsv[2] = col == 0 ? (15 - row) / 15f : (row < 5 ? 1 : (14f - (row - 4f)) / 14f);
                final int c = android.graphics.Color.HSVToColor(hsv);
                colors[col][15 - row] = c;  
                pixmap.setColor((c >> 16 & 0xff) / 255f, (c >> 8 & 0xff) / 255f, (c & 0xff) / 255f, 1f);
                pixmap.fillRectangle(col * w, row * h, w, h);
            }
        }
        colorGridTexture = new Texture(pixmap);
        colorGridImage = new Image(colorGridTexture);
        highlightImage = new Image(skin.newDrawable(Style.Drawables.ic_color_selector));
        add(colorGridImage).size(tableWidth, tableHeight).pad(PADDING);
        colorGridImage.setTouchable(Touchable.enabled);

        highlightImage.setSize(w, h);
        highlightImage.setPosition(colorGridImage.getX(), colorGridImage.getY(), Align.bottomLeft);
        
        colorGridImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final float w = colorGridImage.getWidth() / 16f;
                final float h = colorGridImage.getHeight() / 16f;
                int col = Math.round(x / w - 0.5f);
                int row = Math.round(y / h - 0.5f);
                int c = colors[col][row];
                final Color color = new Color((c >> 16 & 0xff) / 255f, (c >> 8 & 0xff) / 255f, (c & 0xff) / 255f, 1f);
                highlightImage.setPosition(col * w + colorGridImage.getX(), row * h + colorGridImage.getY(), Align.bottomLeft);
                if (colorListener != null)
                    colorListener.accept(color);
            }
        });

        addActor(highlightImage);
    }

    public void setColorListener(@Nullable Consumer<Color> colorListener) {
        this.colorListener = colorListener;
    }

    public void dispose() {
        colorGridTexture.dispose();
    }

    public void setColor(Color color) {
        final float w = colorGridImage.getWidth() / 16f;
        final float h = colorGridImage.getHeight() / 16f;
        float minDst = Float.MAX_VALUE;
        for (int col = 0; col < colors.length; col++) {
            for (int row = 0; row < colors[col].length; row++) {
                int c = colors[col][row];
                final float r = (c >> 16 & 0xff) / 255f;
                final float g = (c >> 8 & 0xff) / 255f;
                final float b = (c & 0xff) / 255f;
                final float d = Vector3.dst2(r, g, b, color.r, color.g, color.b);
                if (d < minDst) {
                    minDst = d;
                    highlightImage.setPosition(col * w + colorGridImage.getX(), row * h + colorGridImage.getY(), Align.bottomLeft);
                }
            }
        }
    }
}
