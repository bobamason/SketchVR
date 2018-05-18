package net.masonapps.sketchvr.modeling;

import android.support.annotation.NonNull;

import com.badlogic.gdx.math.Vector2;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Bob Mason on 5/18/2018.
 */
public class PolygonUtils {

    @NonNull
    public static ArrayList<List<Vector2>> getLoops(Collection polygons) {
        final ArrayList<List<Vector2>> loops = new ArrayList<>();
        // TODO: 5/17/2018 handle holes 
        for (Object obj : polygons) {
            if (obj instanceof Polygon) {
                final Polygon poly = (Polygon) obj;
                final Coordinate[] coordinates = poly.getExteriorRing().getCoordinates();
                if (coordinates.length >= 3) {
                    final ArrayList<Vector2> loop = new ArrayList<>();
                    for (Coordinate coordinate : coordinates) {
                        loop.add(new Vector2((float) coordinate.x, (float) coordinate.y));
                    }
                    loops.add(loop);
                }
            }
        }
        return loops;
    }
}
