package net.masonapps.sketchvr.mesh;

/**
 * Created by Bob Mason on 7/12/2017.
 */

public class Edge {
    public final Vertex v1;
    public final Vertex v2;

    public Edge(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
        v1.addEdge(this);
        v2.addEdge(this);
    }
}
