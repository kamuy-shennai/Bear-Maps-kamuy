package bearmaps.hw4;

import java.util.List;

/**
 * Represents a graph of vertices.
 * 表示顶点图。
 */

public interface AStarGraph<Vertex> {
    List<WeightedEdge<Vertex>> neighbors(Vertex v);
    /**
     * 返回S和GOAL之间的大圆距离。如果S和GOAL存在于这个图中。
     */
    double estimatedDistanceToGoal(Vertex s, Vertex goal);
}
