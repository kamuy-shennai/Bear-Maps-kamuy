package bearmaps.hw4;
import bearmaps.proj2ab.ArrayHeapMinPQ;
import bearmaps.proj2ab.ExtrinsicMinPQ;
import edu.princeton.cs.introcs.Stopwatch;

import java.util.*;

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {
    private final List<Vertex> solution = new ArrayList<>();
    private final HashMap<Vertex, Vertex> edgeTo = new HashMap<>();//道路
    private SolverOutcome outcome;
    private int numStatesExplored = 0;
    private final double timeSpent;
    private double solutionWeight;

    /**
     *A*算法
     * @param input 输入图
     * @param start 起始点
     * @param goal 终点目标点
     * @param timeout 时间
     */
    public AStarSolver(AStarGraph<Vertex>input,Vertex start,Vertex goal,double timeout){
        Stopwatch sw = new Stopwatch();
        ExtrinsicMinPQ<Vertex> PQ = new ArrayHeapMinPQ<>();
        List<WeightedEdge<Vertex>> neighbors;//邻居道路，临时变量
        HashSet<Vertex> visited = new HashSet<>();//访问过的点集合
        Vertex current;
        PQ.add(start, input.estimatedDistanceToGoal(start, goal));
        Map<Vertex, Double> distTo = new HashMap<>();//起点到该点的距离
        distTo.put(start, 0.0);
        do {
            current = PQ.removeSmallest();
            visited.add(current);
            numStatesExplored++;
            neighbors = input.neighbors(current);
            for (WeightedEdge<Vertex> e : neighbors) {
                Vertex begin = e.from();
                Vertex end = e.to();
                double totalDist = distTo.get(begin) + e.weight();
                double priority = totalDist + input.estimatedDistanceToGoal(end, goal);
                if (!distTo.containsKey(end)) {
                    distTo.put(end, totalDist);
                    edgeTo.put(end, begin);
                    PQ.add(end, priority);
                }

                if (totalDist < distTo.get(end)) {
                    distTo.replace(end, totalDist);
                    edgeTo.replace(end, begin);

                    if (PQ.contains(end)) {
                        PQ.changePriority(end, priority);
                    } else {
                        PQ.add(end, priority);
                    }
                }
            }

            if (current.equals(goal) && sw.elapsedTime() < timeout) {
                solutionWeight = distTo.get(goal);
                outcome = SolverOutcome.SOLVED;
                pathBuilder(current);
                Collections.reverse(solution);
                break;
            } else if (sw.elapsedTime() >= timeout) {
                outcome = SolverOutcome.TIMEOUT;
                break;
            } else if (PQ.size() == 0) {
                outcome = SolverOutcome.UNSOLVABLE;
                break;
            }
        } while (PQ.size() > 0);
        timeSpent = sw.elapsedTime();
    }


    private void pathBuilder(Vertex current) {
        while (current != null) {
            solution.add(current);
            current = edgeTo.get(current);
        }
    }
    @Override
    public SolverOutcome outcome() {
        return outcome;
    }

    @Override
    public List<Vertex> solution() {
        return solution;
    }

    /**
     *
     * @return 导航的总距离
     */
    @Override
    public double solutionWeight() {
        if (outcome == SolverOutcome.SOLVED) {
            return solutionWeight;
        } else {
            return 0;
        }
    }

    @Override
    public int numStatesExplored() {
        return numStatesExplored;
    }

    @Override
    public double explorationTime() {
        return timeSpent;
    }
}
