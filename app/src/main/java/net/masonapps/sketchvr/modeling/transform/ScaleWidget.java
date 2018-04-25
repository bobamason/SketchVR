package net.masonapps.sketchvr.modeling.transform;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob Mason on 1/23/2018.
 */

public class ScaleWidget extends TransformWidget3D {

    public ScaleWidget(ModelBuilder builder) {
        super();
        final ScaleHandle3D scaleX = new ScaleHandle3D(builder, DragHandle3D.Axis.AXIS_X);
        add(scaleX);
        final ScaleHandle3D scaleY = new ScaleHandle3D(builder, DragHandle3D.Axis.AXIS_Y);
        add(scaleY);
        final ScaleHandle3D scaleZ = new ScaleHandle3D(builder, DragHandle3D.Axis.AXIS_Z);
        add(scaleZ);
        bounds.set(new Vector3(-2.f, -2.f, -2.f), new Vector3(2.f, 2.f, 2.f));
    }
}
