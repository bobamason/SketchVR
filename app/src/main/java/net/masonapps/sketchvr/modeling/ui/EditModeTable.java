package net.masonapps.sketchvr.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.sketchvr.R;
import net.masonapps.sketchvr.Style;
import net.masonapps.sketchvr.ui.VerticalImageTextButton;

/**
 * Created by Bob Mason on 1/29/2018.
 */

public class EditModeTable extends Table {


    private static final float PADDING = 8f;
    @Nullable
    private OnEditModeChangedListener listener = null;

    public EditModeTable(Skin skin) {
        super(skin);
        final VerticalImageTextButton translateBtn = new VerticalImageTextButton(Style.getStringResource(R.string.translate, "move"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_translate));
        translateBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onEditModeChanged(EditMode.TRANSLATE);
            }
        });
        add(translateBtn).pad(PADDING, PADDING, PADDING, PADDING);

        final VerticalImageTextButton rotateBtn = new VerticalImageTextButton(Style.getStringResource(R.string.rotate, "rotate"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_rotate));
        rotateBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onEditModeChanged(EditMode.ROTATE);
            }
        });
        add(rotateBtn).pad(PADDING, 0, PADDING, PADDING);

        final VerticalImageTextButton scaleBtn = new VerticalImageTextButton(Style.getStringResource(R.string.scale, "scale"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_scale));
        scaleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onEditModeChanged(EditMode.SCALE);
            }
        });
        add(scaleBtn).pad(PADDING, 0, PADDING, PADDING);

        final VerticalImageTextButton colorBtn = new VerticalImageTextButton(Style.getStringResource(R.string.title_color_picker, "color"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_color));
        colorBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onEditModeChanged(EditMode.COLOR);
            }
        });
        add(colorBtn).pad(PADDING, 0, PADDING, PADDING);
    }

    public void setListener(@Nullable OnEditModeChangedListener listener) {
        this.listener = listener;
    }

    public enum EditMode {
        NONE, COLOR, TRANSLATE, ROTATE, SCALE
    }

    public interface OnEditModeChangedListener {
        void onEditModeChanged(EditMode mode);
    }
}
