package net.masonapps.sketchvr.modeling;

import net.masonapps.sketchvr.jcsg.Polygon;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;

import java.util.List;

/**
 * Created by Bob Mason on 6/20/2018.
 */
public class PolygonAABBTree extends AABBTree {

    public PolygonAABBTree(List<Polygon> polygons) {
        for (Polygon polygon : polygons) {
            insert(new PolygonAABBObject(polygon));
        }
    }
}
