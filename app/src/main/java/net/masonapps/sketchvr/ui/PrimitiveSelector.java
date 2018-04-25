package net.masonapps.sketchvr.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.sketchvr.Style;

import java.util.List;

/**
 * Created by Bob Mason on 1/25/2018.
 */

public class PrimitiveSelector extends DialogVR {
    @Nullable
    private OnPrimitiveItemClickedListener listener = null;

    public PrimitiveSelector(Batch batch, Skin skin, List<PrimitiveItem> items) {
        super(batch, 100, 100);
        final Table table = getTable();
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            final PrimitiveItem primitiveItem = items.get(index);
            final VerticalImageTextButton button = new VerticalImageTextButton(primitiveItem.text, Style.createImageTextButtonStyle(skin, primitiveItem.drawableName));
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (listener != null)
                        listener.onItemClicked(primitiveItem);
                    dismiss();
                }
            });
            final Cell<VerticalImageTextButton> cell = table.add(button);
            cell.pad(8f, i == 0 ? 8f : 0f, 8f, 8f);
        }
        resizeToFitTable();
    }

    public void setListener(@Nullable OnPrimitiveItemClickedListener listener) {
        this.listener = listener;
    }

    public interface OnPrimitiveItemClickedListener {
        void onItemClicked(PrimitiveItem item);
    }

    public static class PrimitiveItem {
        public final String primitiveKey;
        public final String text;
        public final String drawableName;

        public PrimitiveItem(String primitiveKey, String text, String drawableName) {
            this.primitiveKey = primitiveKey;
            this.text = text;
            this.drawableName = drawableName;
        }
    }
}
