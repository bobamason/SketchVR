package net.masonapps.sketchvr.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import net.masonapps.sketchvr.SolidModelingGame;
import net.masonapps.sketchvr.Style;
import net.masonapps.sketchvr.environment.Grid;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.gfx.World;

/**
 * Created by Bob on 12/28/2016.
 */
public abstract class RoomScreen extends VrWorldScreen implements SolidModelingGame.OnControllerBackPressedListener {
    private final SolidModelingGame solidModelingGame;
    private final Entity gradientBackground;
    private final Grid gridFloor;

    public RoomScreen(VrGame game) {
        super(game);
        if (!(game instanceof SolidModelingGame))
            throw new IllegalArgumentException("game must be SculptingVrGame");
        solidModelingGame = (SolidModelingGame) game;
        setBackgroundColor(Color.BLACK);
        gradientBackground = Style.newGradientBackground(getVrCamera().far - 1f);
        gradientBackground.invalidate();
        getWorld().add(gradientBackground);
        gridFloor = new Grid(2, solidModelingGame.getSkin().getRegion(Style.Drawables.grid), Color.LIGHT_GRAY);
    }

    @Override
    protected Environment createEnvironment() {
        final Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.DARK_GRAY));
        return environment;
    }

    @Override
    public void show() {
        super.show();
        getVrCamera().position.set(0, 0, 0);
        getVrCamera().up.set(0, 1, 0);
        getVrCamera().lookAt(0, 0, -1);
    }

    @Override
    protected World createWorld() {
        return new World() {
            @Override
            public void render(ModelBatch batch, Environment environment) {
//                final ModelInstance roomInstance = solidModelingGame.getRoomInstance();
//                if (roomInstance != null)
//                    batch.render(roomInstance);
                gridFloor.render(batch);
                super.render(batch, environment);
            }
        };
    }

    public SolidModelingGame getSolidModelingGame() {
        return solidModelingGame;
    }
}
