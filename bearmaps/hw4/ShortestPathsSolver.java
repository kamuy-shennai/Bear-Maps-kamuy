package bearmaps.hw4;

import java.util.List;

/**
 * Interface for shortest path solvers.
 * 最短路径求解器的接口。
 * Created by hug.
 */
public interface ShortestPathsSolver<Vertex> {
    SolverOutcome outcome();
    List<Vertex> solution();
    double solutionWeight();
    int numStatesExplored();
    double explorationTime();
}
