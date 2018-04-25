package net.masonapps.sketchvr.modeling.transform;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob Mason on 1/23/2018.
 */

public class RotateWidget extends TransformWidget3D {

    public RotateWidget(ModelBuilder builder) {
        super();
        final RotateHandle3D rotX = new RotateHandle3D(builder, DragHandle3D.Axis.AXIS_X);
        add(rotX);
        final RotateHandle3D rotY = new RotateHandle3D(builder, DragHandle3D.Axis.AXIS_Y);
        add(rotY);
        final RotateHandle3D rotZ = new RotateHandle3D(builder, DragHandle3D.Axis.AXIS_Z);
        add(rotZ);
        bounds.set(new Vector3(-2.f, -2.f, -2.f), new Vector3(2.f, 2.f, 2.f));
        }
}
