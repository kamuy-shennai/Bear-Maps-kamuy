package bearmaps.hw4;

/**
 * 用来表示边的类。
 */
public class WeightedEdge<Vertex> {
    private Vertex begin;
    private Vertex end;
    private double weight;

    private String name;

    public WeightedEdge(Vertex begin, Vertex end, double weight) {
        this.begin = begin;
        this.end = end;
        this.weight = weight;
    }

    /**
     * 返回起点
     * @return 起点
     */
    public Vertex from() {
        return begin;
    }

    /**
     * 返回目的地点终点
     * @return 终点
     */
    public Vertex to() {
        return end;
    }
    public double weight() {
        return weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
