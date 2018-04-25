package net.masonapps.sketchvr.modeling;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.masonapps.sketchvr.mesh.Stroke;

/**
 * Created by Bob Mason on 3/20/2018.
 */

public class CatmullRomSplineNode extends DynamicNode {

    private final CatmullRomSpline<Vector3> spline;
    private Array<Vector3> controlPoints = new Array<>();
    private boolean continuous = false;

    public CatmullRomSplineNode() {
        super();
        spline = new CatmullRomSpline<>();
    }

    public void setControlPoints(Stroke stroke) {
        controlPoints.clear();
        for (Vector3 point : stroke.points) {
            controlPoints.add(point);
        }
        updateSpline();
    }

    private void updateSpline() {
        spline.set(controlPoints.toArray(), continuous);
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }
}
