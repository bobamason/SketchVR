package net.masonapps.sketchvr.screens;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.sketchvr.SolidModelingGame;
import net.masonapps.sketchvr.io.ProjectFileIO;
import net.masonapps.sketchvr.ui.FileButtonBar;
import net.masonapps.sketchvr.ui.ProjectList;

import org.json.JSONException;
import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.ui.VrUiContainer;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Bob on 8/30/2017.
 */

public class OpenProjectListScreen extends RoomScreen implements ProjectList.OnProjectSelectedListener, FileButtonBar.OnFileButtonClicked<File> {

    protected final ProjectList<File> ui;
    private final List<File> list;
    private final Consumer<File> consumer;
    private final FileButtonBar<File> fileButtonBar;
    private final VrUiContainer container = new VrUiContainer();
    @Nullable
    private ProjectList.ProjectItem selectedItem = null;
    private Vector3 tmp = new Vector3();

    public OpenProjectListScreen(SolidModelingGame game, List<File> list, Consumer<File> consumer) {
        super(game);
        this.list = list;
        this.consumer = consumer;
//        getWorld().add(Style.newGradientBackground(getVrCamera().far - 1f));
//        getWorld().add(Grid.newInstance(20f, 0.5f, 0.02f, Color.WHITE, Color.DARK_GRAY)).setPosition(0, -1.3f, 0);

        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        ui = new ProjectList<File>(list, this) {
            @Override
            protected ModelData loadProject(File file, BoundingBox bounds) throws IOException, JSONException {
                return new ProjectFileIO.ModelDataLoader(file, bounds).loadModelData();
            }

            @Override
            protected void onLoadFailed(File file, Throwable e) {
                Logger.e(file.getName(), e);
            }
        };
        manageDisposable(ui);

        fileButtonBar = new FileButtonBar<>(spriteBatch, getSolidModelingGame().getSkin(), this);
        fileButtonBar.setPosition(-1.25f, 0.75f, -2);
        fileButtonBar.lookAt(tmp.set(0, 1f, 0f), Vector3.Y);

        container.addProcessor(ui);
        container.addProcessor(fileButtonBar);
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        lights.add(light);
    }

    @Override
    protected World createWorld() {
        return new World() {

            @Override
            public void update() {
                super.update();
                ui.update();
            }

            @Override
            public void render(ModelBatch batch, Environment environment) {
                final ModelInstance roomInstance = getSolidModelingGame().getRoomInstance();
                if (roomInstance != null)
                    batch.render(roomInstance);
                super.render(batch, environment);
                ui.render(batch, environment);
                if (selectedItem != null && selectedItem.modelInstance != null) {
                    selectedItem.validate();
                    batch.render(selectedItem.modelInstance, environment);
                }
            }
        };
    }

    @Override
    public void show() {
        super.show();
        GdxVr.input.setInputProcessor(container);
    }

    @Override
    public void hide() {
        super.hide();
        GdxVr.input.setInputProcessor(null);
    }

    @Override
    public void update() {
        super.update();
        container.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        ui.debug(camera);
        container.draw(camera);
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
    }

    @Override
    public void onControllerBackButtonClicked() {
        getSolidModelingGame().switchToStartupScreen();
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        ui.onControllerTouchPadEvent(event);
    }

    @Override
    public void onProjectSelected(ProjectList.ProjectItem item) {
        selectedItem = item.copy();
        if (selectedItem != null) {
            fileButtonBar.setT(list.get(item.index));
            selectedItem.setPosition(0, 1, -3.5f);
            final float scale = 3f / selectedItem.getBounds().getDimensions(tmp).len();
            selectedItem.setScale(scale);
        } else {
            fileButtonBar.setT(null);
        }
//        consumer.accept(list.get(item.index));
    }

    @Override
    public void onOpenClicked(File file) {
        if (selectedItem != null)
            consumer.accept(file);
    }

    @Override
    public void onCopyClicked(File file) {

    }

    @Override
    public void onDeleteClicked(File file) {

    }
}
