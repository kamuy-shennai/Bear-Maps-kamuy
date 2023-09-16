package bearmaps.proj2ab;

import java.util.HashSet;
import java.util.List;

public class KDTree implements PointSet {
    private final Node firstNode;
    private static class Node {
        private final Point point;
        private Node left;
        private Node right;
        private int dimensional;
        public Node (Point p) {
            point = p;
            left = null;
            right = null;
            dimensional = 0;
        }
    }

    /**
     * 构造函数，构建k-d tree
     * @param points 点集
     */
    public KDTree(List<Point> points) {
        firstNode = new Node(points.get(0));
        for(int i = 1; i < points.size(); i++){
            Node node = new Node(points.get(i));
            Node tempNode = firstNode;
            while (true)
            {
                if(tempNode.dimensional==0) {
                    if(tempNode.point.getX()>node.point.getX()) {
                        if(tempNode.left!=null) {
                            tempNode=tempNode.left;
                        }else {
                            node.dimensional = 1;
                            tempNode.left=node;
                            break;
                        }
                    }else {
                        if(tempNode.right!=null) {
                            tempNode=tempNode.right;
                        }else {
                            node.dimensional = 1;
                            tempNode.right=node;
                            break;
                        }
                    }
                }else {
                    if(tempNode.point.getY()>node.point.getY()) {
                        if(tempNode.left!=null) {
                            tempNode=tempNode.left;
                        }else {
                            node.dimensional = 0;
                            tempNode.left=node;
                            break;
                        }
                    }else {
                        if(tempNode.right!=null) {
                            tempNode=tempNode.right;
                        }else {
                            node.dimensional = 0;
                            tempNode.right=node;
                            break;
                        }
                    }
                }
            }
        }
    }
    public Point nearest(double x, double y){
        Point goal = new Point(x,y);
        Node best = nearestHelper(goal,firstNode,firstNode);
      return best.point;
    }

    private Node nearestHelper(Point goal, Node nowBest, Node n)
    {
        Node Best = nowBest;
        if(n==null)return nowBest;
        //System.out.println(""+Best.point.getX()+","+n.point.getY());
        if(Point.distance(n.point,goal)<Point.distance(Best.point,goal)){
            Best=n;
        }
        Node goodSide;
        Node badSide;
        if(n.dimensional==0)
        {
            if(goal.getX()<n.point.getX()){
                goodSide = n.left;
                badSide = n.right;
            }else {
                goodSide = n.right;
                badSide = n.left;
            }
        }else {
            if(goal.getY()<n.point.getY()){
                goodSide = n.left;
                badSide = n.right;
            }else {
                goodSide = n.right;
                badSide = n.left;
            }
        }

        Best = nearestHelper(goal,Best,goodSide);
        if(badSideHaveGood(goal,Best,badSide)){
            Best = nearestHelper(goal,Best,badSide);
        }
        return Best;
    }
    private boolean badSideHaveGood(Point goal,Node nowBest,Node badSide){
        if(badSide == null){
            return false;
        }
        if(badSide.dimensional==0)
        {
            return Point.distance(goal, new Point(goal.getX(), badSide.point.getY())) < Point.distance(goal, nowBest.point);
        }else {
            return Point.distance(goal, new Point(badSide.point.getX(), goal.getY())) < Point.distance(goal, nowBest.point);
        }
    }
}
