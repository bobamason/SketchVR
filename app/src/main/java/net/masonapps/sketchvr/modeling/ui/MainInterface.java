package net.masonapps.sketchvr.modeling.ui;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.sketchvr.R;
import net.masonapps.sketchvr.Style;
import net.masonapps.sketchvr.modeling.SketchNode;
import net.masonapps.sketchvr.ui.ColorPickerSimple;
import net.masonapps.sketchvr.ui.ConfirmDialog;
import net.masonapps.sketchvr.ui.VerticalImageTextButton;

import org.masonapps.libgdxgooglevr.math.CylindricalCoordinate;
import org.masonapps.libgdxgooglevr.ui.CylindricalWindowUiContainer;
import org.masonapps.libgdxgooglevr.ui.WindowTableVR;

import java.util.function.Consumer;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class MainInterface extends CylindricalWindowUiContainer {

    public static final String WINDOW_MAIN = "winMain";
    public static final String WINDOW_VIEW_CONTROLS = "winViewControls";
    private static final float PADDING = 8f;
    private final Skin skin;
    private final UiEventListener eventListener;
    private final WindowTableVR mainTable;
    private final ColorPickerSimple colorPicker;
    private final ConfirmDialog confirmDialog;
    private final ViewControls viewControls;
    private final Container<Table> container;
    private final Table emptyTable;
    private EditModeTable editModeTable;
    @Nullable
    private SketchNode entity = null;
    private EditModeTable.EditMode currentEditMode = EditModeTable.EditMode.NONE;

    public MainInterface(Batch spriteBatch, Skin skin, UiEventListener listener) {
        super(2f, 4f);
        this.skin = skin;
        this.eventListener = listener;
//        final WindowVR.WindowVrStyle windowStyleWithClose = Style.createWindowVrStyle(skin);
//        windowStyleWithClose.closeDrawable = skin.newDrawable(Style.Drawables.ic_close);
        container = new Container<>();
        emptyTable = new Table();
        container.setActor(emptyTable);
        mainTable = new WindowTableVR(spriteBatch, skin, 200, 200, Style.createWindowVrStyle(skin));
        colorPicker = new ColorPickerSimple(skin, 448, 448);
        confirmDialog = new ConfirmDialog(spriteBatch, skin);
        viewControls = new ViewControls(spriteBatch, skin, Style.createWindowVrStyle(skin));
        editModeTable = new EditModeTable(skin);
        editModeTable.setListener(this::editModeChanged);
        initMainTable();
        initConfirmDialog();
        initViewControls();
    }

    private void initMainTable() {
        final Table buttonBarTable = new Table(skin);

        final VerticalImageTextButton undoBtn = new VerticalImageTextButton(Style.getStringResource(R.string.undo, "undo"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_undo));
        undoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onUndoClicked();
            }
        });
        buttonBarTable.add(undoBtn).padTop(PADDING).padLeft(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton redoBtn = new VerticalImageTextButton(Style.getStringResource(R.string.redo, "redo"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_redo));
        redoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onRedoClicked();
            }
        });
        buttonBarTable.add(redoBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING);

//        final VerticalImageTextButton viewBtn = new VerticalImageTextButton(Style.getStringResource(R.string.view, "view"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_rotate));
//        viewBtn.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                viewControls.setVisible(!viewControls.isVisible());
//            }
//        });
//        buttonBarTable.add(viewBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton exportBtn = new VerticalImageTextButton(Style.getStringResource(R.string.export, "export"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_export));
        exportBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onExportClicked();
            }
        });
        buttonBarTable.add(exportBtn).padTop(PADDING).padBottom(PADDING).padRight(PADDING).row();

        final VerticalImageTextButton addBtn = new VerticalImageTextButton(Style.getStringResource(R.string.add, "add"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_add));
        addBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //todo add work plane
            }
        });
        buttonBarTable.add(addBtn).padTop(PADDING).padLeft(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton dupBtn = new VerticalImageTextButton(Style.getStringResource(R.string.duplicate, "duplicate"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_duplicate));
        dupBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onDuplicateClicked();
            }
        });
        buttonBarTable.add(dupBtn).padTop(PADDING).padLeft(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton deleteBtn = new VerticalImageTextButton(Style.getStringResource(R.string.delete, "delete"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_delete));
        deleteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onDeleteClicked();
            }
        });
        buttonBarTable.add(deleteBtn).padTop(PADDING).padLeft(PADDING).padBottom(PADDING).padRight(PADDING).row();

        final VerticalImageTextButton groupBtn = new VerticalImageTextButton(Style.getStringResource(R.string.group, "group"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_close));
        groupBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onGroupClicked();
            }
        });
        buttonBarTable.add(groupBtn).padTop(PADDING).padLeft(PADDING).padBottom(PADDING).padRight(PADDING);

        final VerticalImageTextButton ungroupBtn = new VerticalImageTextButton(Style.getStringResource(R.string.ungroup, "ungroup"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_close));
        ungroupBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventListener.onUnGroupClicked();
            }
        });
        buttonBarTable.add(ungroupBtn).padTop(PADDING).padLeft(PADDING).padBottom(PADDING).padRight(PADDING);

        final Container<Table> optionContainer = new Container<>();
        final Table optionsTable = new Table(skin);
        optionsTable.add(buttonBarTable).left().expandX().row();
        optionsTable.add(editModeTable).left().expandX();
        optionContainer.setActor(optionsTable);
        mainTable.getTable().add(optionContainer).left().expand();
        mainTable.getTable().add(container).expand();

        final CylindricalCoordinate coordinate = new CylindricalCoordinate(getRadius(), 50f, 0.35f, CylindricalCoordinate.AngleMode.degrees);
        mainTable.setPosition(coordinate.toCartesian());
        mainTable.lookAt(new Vector3(0, coordinate.vertical, 0), Vector3.Y);
        colorPicker.setColorListener(eventListener::onColorChanged);
        mainTable.resizeToFitTable();
        addProcessor(mainTable);
    }

    private void editModeChanged(EditModeTable.EditMode editMode) {
        setEditMode(editMode);
        eventListener.onEditModeChanged(editMode);
    }

    public void setEditMode(EditModeTable.EditMode editMode) {
        currentEditMode = editMode;
        switch (currentEditMode) {
            case NONE:
                container.setActor(emptyTable);
                break;
            case TRANSLATE:
                container.setActor(emptyTable);
                break;
            case ROTATE:
                container.setActor(emptyTable);
                break;
            case SCALE:
                container.setActor(emptyTable);
                break;
            case COLOR:
                container.setActor(colorPicker);
                break;
        }
        mainTable.resizeToFitTable();
    }

    public EditModeTable.EditMode getCurrentEditMode() {
        return currentEditMode;
    }

    private void initConfirmDialog() {
        confirmDialog.setVisible(false);
        confirmDialog.setBackground(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        confirmDialog.setPosition(new CylindricalCoordinate(getRadius(), 90f, 0f, CylindricalCoordinate.AngleMode.degrees).toCartesian());
        addProcessor(confirmDialog);
    }

    private void showConfirmDialog(String msg, Consumer<Boolean> consumer) {
        confirmDialog.setMessage(msg);
        confirmDialog.setListener(consumer);
        confirmDialog.show();
    }

    private void initViewControls() {
        final CylindricalCoordinate coordinate = new CylindricalCoordinate(getRadius(), 40f, -0.35f, CylindricalCoordinate.AngleMode.degrees);
        viewControls.setPosition(coordinate.toCartesian());
        viewControls.lookAt(new Vector3(0, coordinate.vertical, 0), Vector3.Y);
//        viewControls.setVisible(false);
        addProcessor(viewControls);
    }

    @Override
    public void act() {
        super.act();
    }

    public void loadWindowPositions(SharedPreferences sharedPreferences) {
        //todo uncomment
//        final Vector3 tmp = Pools.obtain(Vector3.class);
//
//        tmp.fromString(sharedPreferences.getString(WINDOW_MAIN, mainTable.getPosition().toString()));
//        mainTable.setPosition(tmp);
//        snapDragTableToCylinder(mainTable);
//
//        tmp.fromString(sharedPreferences.getString(WINDOW_VIEW_CONTROLS, viewControls.getPosition().toString()));
//        viewControls.setPosition(tmp);
//        snapDragTableToCylinder(viewControls);
//
//        Pools.free(tmp);
    }

    public ColorPickerSimple getColorPicker() {
        return colorPicker;
    }

    public void saveWindowPositions(SharedPreferences.Editor editor) {
        editor.putString(WINDOW_MAIN, mainTable.getPosition().toString());
        editor.putString(WINDOW_VIEW_CONTROLS, viewControls.getPosition().toString());
    }

    public boolean onControllerBackButtonClicked() {
        if (confirmDialog.isVisible()) {
            confirmDialog.dismiss();
            return true;
        }
        return false;
    }

    public ViewControls getViewControls() {
        return viewControls;
    }

    public void setEntity(@Nullable SketchNode entity) {
        this.entity = entity;
        if (entity == null)
            editModeChanged(EditModeTable.EditMode.NONE);
        else
            editModeChanged(currentEditMode);
    }

    public interface UiEventListener {
        void onAddClicked(String key);

        void onDeleteClicked();

        void onDuplicateClicked();

        void onColorChanged(Color color);

        void onEditModeChanged(EditModeTable.EditMode mode);
        
        void onUndoClicked();

        void onRedoClicked();

        void onExportClicked();

        void onGroupClicked();

        void onUnGroupClicked();
    }
}
