package net.masonapps.sketchvr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.sketchvr.R;
import net.masonapps.sketchvr.Style;

/**
 * Created by Bob Mason on 3/6/2018.
 */

public class GroupCompleteDialog extends DialogVR {

    public GroupCompleteDialog(Batch batch, Skin skin, final GroupCompleteDialog.GroupDialogListener listener) {
        super(batch, skin, 100, 100);
        table.add(Style.getStringResource(R.string.group_objects, "Group Objects")).colspan(2).expandX().center().pad(10f).row();
        final TextButton cancel = new TextButton(Style.getStringResource(R.string.cancel, "cancel"), skin);
        cancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCancelClicked();
                dismiss();
            }
        });
        table.add(cancel).pad(0, 10, 10, 10);
        final TextButton done = new TextButton(Style.getStringResource(R.string.done, "done"), skin);
        done.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onDoneClicked();
                dismiss();
            }
        });
        table.add(done).pad(0, 10, 10, 10);
        resizeToFitTable();
        setVisible(false);
    }

    public interface GroupDialogListener {
        void onCancelClicked();

        void onDoneClicked();
    }
}
