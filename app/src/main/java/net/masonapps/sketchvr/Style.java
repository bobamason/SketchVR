package net.masonapps.sketchvr;

import android.content.Context;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.sketchvr.environment.GradientSphere;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.ui.WindowVR;

/**
 * Created by Bob on 2/11/2017.
 */

public class Style {

    public static final String FONT_REGION = "Roboto-hdpi";
    public static final String FONT_FILE = "skin/Roboto-hdpi.fnt";
    public static final String ATLAS_FILE = "skin/drawables.pack";
    public static final Color COLOR_PRIMARY = new Color(0x0288d1ff);
    public static final Color COLOR_PRIMARY_LIGHT = new Color(0x5eb8ffff);
    public static final Color COLOR_PRIMARY_DARK = new Color(0x005b9fff);
    public static final Color COLOR_ACCENT = new Color(0xffc107ff);
    public static final Color COLOR_UP = new Color(0x00000000);
    public static final Color COLOR_DOWN = new Color(0xcccccc99);
    public static final Color COLOR_OVER = new Color(0xcccccc66);
    public static final Color COLOR_UP_2 = new Color(Color.WHITE);
    public static final Color COLOR_DOWN_2 = new Color(Color.LIGHT_GRAY);
    public static final Color COLOR_OVER_2 = new Color(Color.GRAY);
    public static final Color COLOR_DISABLED = new Color(Color.GRAY);
    //    public static final Color COLOR_WINDOW = new Color(Color.BLACK);
    public static final String DEFAULT = "default";
    public static final String DEFAULT_FONT = "default-font";
    public static final String DEFAULT_HORIZONTAL = "default-horizontal";
    public static final String TOGGLE = "toggle";
    public static final String LIST_ITEM = "list_item";
    public static final Color FONT_COLOR = new Color(Color.WHITE);
    public static final Color TITLE_FONT_COLOR = new Color(FONT_COLOR);
    public static final Color SLIDER_BG_COLOR = new Color(Color.GRAY);
    public static final Color COLOR_WINDOW = new Color(0x000000aa);
    private static final Color COLOR_GRADIENT_BOTTOM = new Color(Color.BLACK);
    private static final Color COLOR_GRADIENT_TOP = new Color(Color.SKY);
    private static final Color COLOR_TITLE_BAR = new Color(COLOR_PRIMARY);

    public static ImageButton.ImageButtonStyle createImageButtonStyle(Skin skin, String name) {
        final ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle(skin.get(DEFAULT, ImageButton.ImageButtonStyle.class));
        imageButtonStyle.imageUp = skin.newDrawable(name);
        imageButtonStyle.imageDisabled = skin.newDrawable(name, Color.GRAY);
        return imageButtonStyle;
    }

    public static ImageButton.ImageButtonStyle createImageButtonStyleNoBg(Skin skin, String name) {
        final ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.imageUp = skin.newDrawable(name);
        imageButtonStyle.imageDown = skin.newDrawable(name, Color.LIGHT_GRAY);
        imageButtonStyle.imageDisabled = skin.newDrawable(name, Color.GRAY);
        return imageButtonStyle;
    }

    public static ImageTextButton.ImageTextButtonStyle createImageTextButtonStyle(Skin skin, String name) {
        final ImageTextButton.ImageTextButtonStyle imageTextButtonStyle = new ImageTextButton.ImageTextButtonStyle(skin.get(DEFAULT, ImageTextButton.ImageTextButtonStyle.class));
        imageTextButtonStyle.imageUp = skin.newDrawable(name);
        return imageTextButtonStyle;
    }

    public static String getStringResource(int res, String defaultValue) {
        final Context context = GdxVr.app.getActivityWeakReference().get();
        if (context != null) {
            try {
                return context.getString(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public static Image newBackgroundImage(Skin skin) {
        return newBackgroundImage(skin, Color.WHITE);
    }

    public static Image newBackgroundImage(Skin skin, Color color) {
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, color));
        bg.setFillParent(true);
        bg.setTouchable(Touchable.disabled);
        bg.setZIndex(0);
        return bg;
    }

    public static Entity newGradientBackground(float r) {
        return GradientSphere.newInstance(r, 32, 16, Style.COLOR_GRADIENT_BOTTOM, Style.COLOR_GRADIENT_TOP);
    }

    public static WindowVR.WindowVrStyle createWindowVrStyle(Skin skin) {
        return new WindowVR.WindowVrStyle(skin.newDrawable(Drawables.window, COLOR_WINDOW), skin.newDrawable(Drawables.drag_bar, COLOR_TITLE_BAR), skin.getFont(DEFAULT_FONT), TITLE_FONT_COLOR);
    }

    public static class Drawables {
        public static final String loading_spinner = "loading_spinner";
        public static final String circle = "circle";
        public static final String dash_circle = "dash_circle";
        public static final String touch_pad_background = "touch_pad_background";
        public static final String touch_pad_button_down = "touch_pad_button_down";
        public static final String left_arrow = "left_arrow";
        public static final String right_arrow = "right_arrow";
        public static final String ic_shape_cube = "ic_shape_cube";
        public static final String ic_shape_sphere = "ic_shape_sphere";
        public static final String ic_shape_cylinder = "ic_shape_cylinder";
        public static final String ic_shape_cone = "ic_shape_cone";
        public static final String ic_shape_torus = "ic_shape_torus";
        public static final String ic_export = "ic_export";
        public static final String ic_folder = "ic_folder";
        public static final String ic_pan = "ic_pan";
        public static final String ic_redo = "ic_redo";
        public static final String ic_undo = "ic_undo";
        public static final String ic_translate = "ic_translate";
        public static final String ic_rotate = "ic_rotate";
        public static final String ic_scale = "ic_scale";
        public static final String ic_rotate_handle = "ic_rotate_handle";
        public static final String ic_zoom = "ic_zoom";
        public static final String ic_add = "ic_add";
        public static final String ic_more_vert = "ic_more_vert";
        public static final String ic_close = "ic_close";
        public static final String ic_color_selector = "ic_color_selector";
        public static final String ic_color = "ic_color";
        public static final String ic_drag_left = "ic_drag_left";
        public static final String ic_drag_up = "ic_drag_up";
        public static final String window = "window";
        public static final String button = "button";
        public static final String white = "white";
        public static final String grid = "grid";
        public static final String round_button = "round_button";
        public static final String slider = "slider";
        public static final String slider_knob = "slider_knob";
        public static final String new_project = "new_project";
        public static final String open_project = "open_project";
        public static final String checkbox_on = "checkbox_on";
        public static final String checkbox_off = "checkbox_off";
        public static final String drag_bar = "drag_bar";
        public static final String ic_open = "ic_open";
        public static final String ic_copy = "ic_copy";
        public static final String ic_duplicate = "ic_duplicate";
        public static final String ic_delete = "ic_delete";
        public static final String ic_dropper = "ic_dropper";
    }
}
