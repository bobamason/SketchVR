package net.masonapps.sketchvr.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import net.masonapps.sketchvr.Style;

import java.util.function.Consumer;

/**
 * Created by Bob on 8/3/2017.
 */

public class ColorPickerRound extends Group {

    public static final int PADDING = 10;
    private final Color selectedColor = new Color();
    private final float[] hsv = new float[3];
    private final Vector2 tmp = new Vector2();
    @Nullable
    private Consumer<Color> colorListener = null;
    private Slider brightnessSlider;
    private ColorCircle colorCircle;
    private Image selection;

    public ColorPickerRound(Skin skin, int width, int height) {
        super();
        setSize(width, height);
        colorCircle = new ColorCircle(skin.newDrawable(Style.Drawables.white));

        brightnessSlider = new Slider(0f, 1f, 0.01f, true, skin);
        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                colorCircle.setHSV(colorCircle.hue, colorCircle.saturation, brightnessSlider.getValue());
                colorChanged();
            }
        });
        brightnessSlider.setPosition(width - brightnessSlider.getWidth(), 0);
        addActor(brightnessSlider);

        final float s = width - brightnessSlider.getWidth() - PADDING;
        colorCircle.setSize(s, s);
        addActor(colorCircle);

        selection = new Image(skin.newDrawable(Style.Drawables.circle));
        selection.setSize(12, 12);
    }

    private void colorChanged() {
        hsv[0] = colorCircle.hue;
        hsv[1] = colorCircle.saturation;
        hsv[2] = colorCircle.value;
        int c = android.graphics.Color.HSVToColor(hsv);
        selectedColor.set(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f, 1f);
        selection.setColor(selectedColor);
        colorCircle.calculateGlobalSelectedCoordinates(tmp);
        selection.setPosition(tmp.x, tmp.y, Align.center);
        if (colorListener != null)
            colorListener.accept(selectedColor);
    }

    public void setColorListener(@Nullable Consumer<Color> colorListener) {
        this.colorListener = colorListener;
    }

    public void setSelectedColor(Color color) {
        selectedColor.set(color);
        int c = ((int) (color.a * 255f + 0.5f) << 24) |
                ((int) (color.r * 255f + 0.5f) << 16) |
                ((int) (color.g * 255f + 0.5f) << 8) |
                ((int) (color.b * 255f + 0.5f));
        android.graphics.Color.colorToHSV(c, hsv);
        colorCircle.setHSV(hsv[0], hsv[1], hsv[2]);
        brightnessSlider.setValue(hsv[2]);
    }

    private class ColorCircle extends Image {

        private final ShaderProgram shader;
        private final Vector2 selectedPos = new Vector2();
        private float hue = 0f;
        private float saturation = 1f;
        private float value = 1f;

        ColorCircle(Drawable drawable) {
            super(drawable);
            setTouchable(Touchable.enabled);
            shader = new ShaderProgram(SpriteBatch.createDefaultShader().getVertexShaderSource(), Gdx.files.internal("shaders/color_picker.fragment.glsl").readString());
            addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    selectedPos.set(x, y).scl(1f / ColorCircle.this.getWidth(), 1f / ColorCircle.this.getHeight()).sub(0.5f, 0.5f).scl(2f);
                    float r = selectedPos.len();
                    if (r < 1f) {
                        saturation = r;
                        hue = (MathUtils.atan2(selectedPos.y, selectedPos.x) + MathUtils.PI) * MathUtils.radiansToDegrees;
                        colorChanged();
                        return true;
                    }
                    return false;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    selectedPos.set(x, y).scl(1f / ColorCircle.this.getWidth() / 2f, 1f / ColorCircle.this.getHeight() / 2f).sub(1f, 1f);
                    float r = selectedPos.len();
                    if (r < 1f) {
                        saturation = r;
                        hue = (MathUtils.atan2(selectedPos.y, selectedPos.x) + MathUtils.PI) * MathUtils.radiansToDegrees;
                        colorChanged();
                    }
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.setShader(shader);
            super.draw(batch, parentAlpha);
            batch.setShader(null);
        }

        public void setHSV(float hue, float saturation, float value) {
            this.hue = hue;
            this.saturation = saturation;
            setColor(value, value, value, 1f);
            this.value = value;
            selectedPos.set(MathUtils.cosDeg(hue), MathUtils.sinDeg(hue)).scl(saturation);
        }

        public void calculateGlobalSelectedCoordinates(Vector2 coord) {
            coord.set(selectedPos).add(1f, 1f).scl(ColorCircle.this.getWidth() / 2f, ColorCircle.this.getHeight() / 2f);
            ColorCircle.this.localToParentCoordinates(coord);
        }
    }
}
