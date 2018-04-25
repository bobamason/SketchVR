package net.masonapps.sketchvr.modeling.transform;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob Mason on 1/23/2018.
 */

public class TranslateWidget extends TransformWidget3D {

    public TranslateWidget(ModelBuilder builder) {
        super();
        final TranslateHandle3D transX = new TranslateHandle3D(builder, DragHandle3D.Axis.AXIS_X);
        add(transX);
        final TranslateHandle3D transY = new TranslateHandle3D(builder, DragHandle3D.Axis.AXIS_Y);
        add(transY);
        final TranslateHandle3D transZ = new TranslateHandle3D(builder, DragHandle3D.Axis.AXIS_Z);
        add(transZ);
        bounds.set(new Vector3(-2f, -2f, -2f), new Vector3(2f, 2f, 2f));
    }
}
