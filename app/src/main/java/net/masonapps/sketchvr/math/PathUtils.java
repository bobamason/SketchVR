package net.masonapps.sketchvr.math;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob Mason on 3/23/2018.
 */

public class PathUtils {
    private static final Vector3 p1 = new Vector3();
    private static final Vector3 p2 = new Vector3();

    public static void drawPath(ShapeRenderer shapeRenderer, Path<Vector3> path, int segments) {
        final float step = 1f / segments;
        for (int i = 0; i < segments - 1; i++) {
            path.valueAt(p1, i * step);
            path.valueAt(p2, (i + 1) * step);
            shapeRenderer.line(p1, p2);
        }
    }
}
