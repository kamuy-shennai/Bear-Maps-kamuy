package bearmaps.proj2c;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import bearmaps.hw4.*;
/**
 * This class acts as a helper for the RoutingAPIHandler.
 * 该类作为RoutingAPIHandler的一个助手。
 * @author Josh Hug, ______
 */
public class Router {

    /**
     * Overloaded method for shortestPath that has flexibility to specify a solver
     * and returns a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination location.
     * shortestPath 的重载方法可以灵活地指定求解器并返回一个长列表，表示从最接近起始位置的节点到最接近目标位置的节点的最短路径。
     * @param g The graph to use.要使用的图。
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.按照最短路径上访问的顺序排列的节点ID列表。
     */
    public static List<Long> shortestPath(AugmentedStreetMapGraph g, double stlon, double stlat,
                                          double destlon, double destlat) {
        long src = g.closest(stlon, stlat);
        long dest = g.closest(destlon, destlat);
        AStarSolver<Long> hello = new AStarSolver<>(g,src,dest,20);
        System.out.println(hello.outcome());
        return hello.solution();
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     * 创建与图表上的路线对应的方向列表。
     * @param g The graph to use.要使用的图。
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.翻译成方向的路线。每个元素都对应于路线中图形的一个节点。
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.与输入路线对应的 NavigatiionDirection 对象列表。
     */
    public static List<NavigationDirection> routeDirections(AugmentedStreetMapGraph g, List<Long> route) {
        if (route == null || route.size() < 2) {
            return null;
        }

        List<NavigationDirection> results = new ArrayList<>();

        Iterator<Long> routeIter = route.iterator();

        long pre = routeIter.next();
        long cur = routeIter.next();

        NavigationDirection nd = new NavigationDirection();
        nd.direction = NavigationDirection.START;
        nd.distance = -1;  // as a flag, meaning the WeightedEdge is not found.

        for (WeightedEdge<Long> we : g.neighbors(pre)) {
            if (we.to() == cur) {
                nd.way = (we.getName().isEmpty()) ? NavigationDirection.UNKNOWN_ROAD : we.getName();
                nd.distance = (int) (we.weight()*1609.344);
                break;
            }
        }
        if (nd.distance == -1) {
            throw new IllegalArgumentException("Invalid route.");
        }

        if (!routeIter.hasNext()) {
            results.add(nd);
            return results;
        }

        while (routeIter.hasNext()) {
            long next = routeIter.next();

            WeightedEdge<Long> we = null;  // from cur to next
            for (WeightedEdge<Long> weightedEdge : g.neighbors(cur)) {
                if (weightedEdge.to() == next) {
                    we = weightedEdge;
                    break;
                }
            }
            if (we == null) {
                throw new IllegalArgumentException("Invalid route.");
            }

            if ((we.getName().isEmpty() && !nd.way.equals(NavigationDirection.UNKNOWN_ROAD))
                    || (!we.getName().isEmpty() && !we.getName().equals(nd.way))) {
                results.add(nd);

                nd = new NavigationDirection();
                nd.direction = NavigationDirection.getDirection(
                        NavigationDirection.bearing(g.lon(pre), g.lon(cur), g.lat(pre), g.lat(cur)),
                        NavigationDirection.bearing(g.lon(cur), g.lon(next), g.lat(cur), g.lat(next)));
                nd.way = (we.getName().isEmpty()) ? NavigationDirection.UNKNOWN_ROAD : we.getName();
                nd.distance = (int) (we.weight()*1609.344);
            } else {
                nd.distance += (int) (we.weight()*1609.344);
            }

            pre = cur;
            cur = next;
        }

        results.add(nd);
        return results;
    }

    /**
     * 表示导航方向的类，它由 3 个属性组成：前进的方向、方式和行进的距离。
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** 表示方向的整数常量。 */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported.支持的方向数。 */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.整数值到方向的映射。*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way.未知道路的默认名称。 */
        public static final String UNKNOWN_ROAD = "无名路";

        /** 静态初始化器。 */
        static {
            DIRECTIONS[START] = "开始";
            DIRECTIONS[STRAIGHT] = "直行";
            DIRECTIONS[SLIGHT_LEFT] = "稍左转";
            DIRECTIONS[SLIGHT_RIGHT] = "稍右转";
            DIRECTIONS[LEFT] = "左转";
            DIRECTIONS[RIGHT] = "右转";
            DIRECTIONS[SHARP_LEFT] = "急左转";
            DIRECTIONS[SHARP_RIGHT] = "急右转";
        }

        /** The direction a given NavigationDirection represents.给定 NavigationDirection 表示的方向*/
        int direction;
        /** The name of the way I represent. 我代表的方式的名称。*/
        String way;
        /** The distance along this way I represent.长度 */
        int distance;

        /**
         * Create a default, anonymous NavigationDirection.创建默认的匿名 NavigationDirection。
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0;
        }

        public String toString() {
            return String.format("在%s%s后继续行驶%s米",
                    way, DIRECTIONS[direction], distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.获取导航方向的字符串表示形式并将其转换为导航方向对象。
         * @param dirAsString The string representation of the NavigationDirection.NavigationDirection 的字符串表示形式。
         * @return A NavigationDirection object representing the input string.表示输入字符串的 NavigationDirection 对象。
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("开始")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("直行")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("稍左转")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("稍右转")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("有转")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("左转")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("急左转")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("急右转")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = (int) (Double.parseDouble(m.group(3))*1609.344);
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        /** Checks that a value is between the given ranges.检查值是否在给定范围内*/
        private static boolean numInRange(double value, double from, double to) {
            return value >= from && value <= to;
        }

        /**
         * Calculates what direction we are going based on the two bearings, which
         * are the angles from true north. We compare the angles to see whether
         * we are making a left turn or right turn. Then we can just use the absolute value of the
         * difference to give us the degree of turn (straight, sharp, left, or right).
         * 根据两个方位角计算我们前进的方向，这两个方位角是与真北的夹角。 我们比较角度，看看我们是
         * 在左转还是右转。 然后我们可以只使用差异的绝对值来给我们转弯的程度（直的、急的、左的或右的）。
         * @param prevBearing A double in [0, 360.0]
         * @param currBearing A double in [0, 360.0]
         * @return the Navigation Direction type
         */
        private static int getDirection(double prevBearing, double currBearing) {
            double absDiff = Math.abs(currBearing - prevBearing);
            if (numInRange(absDiff, 0.0, 15.0)) {
                return NavigationDirection.STRAIGHT;

            }
            if ((currBearing > prevBearing && absDiff < 180.0)
                    || (currBearing < prevBearing && absDiff > 180.0)) {
                // we're going right
                if (numInRange(absDiff, 15.0, 30.0) || absDiff > 330.0) {
                    // bearmaps.proj2c.example of high abs diff is prev = 355 and curr = 2
                    return NavigationDirection.SLIGHT_RIGHT;
                } else if (numInRange(absDiff, 30.0, 100.0) || absDiff > 260.0) {
                    return NavigationDirection.RIGHT;
                } else {
                    return NavigationDirection.SHARP_RIGHT;
                }
            } else {
                // we're going left
                if (numInRange(absDiff, 15.0, 30.0) || absDiff > 330.0) {
                    return NavigationDirection.SLIGHT_LEFT;
                } else if (numInRange(absDiff, 30.0, 100.0) || absDiff > 260.0) {
                    return NavigationDirection.LEFT;
                } else {
                    return NavigationDirection.SHARP_LEFT;
                }
            }
        }


        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }

        /**
         * Returns the initial bearing (angle) between vertices v and w in degrees.
         * The initial bearing is the angle that, if followed in a straight line
         * along a great-circle arc from the starting point, would take you to the
         * end point.
         * Assumes the lon/lat methods are implemented properly.
         * 以度为单位返回顶点 v 和 w 之间的初始方位角（角度）。 初始方位是一个角度，如果从起点沿着大圆弧沿直线行驶，将带您到达终点。
         * 假设 lon/lat 方法已正确实施。
         * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
         * @param lonV  The longitude of the first vertex.
         * @param latV  The latitude of the first vertex.
         * @param lonW  The longitude of the second vertex.
         * @param latW  The latitude of the second vertex.
         * @return The initial bearing between the vertices.顶点之间的初始方位。
         */
        public static double bearing(double lonV, double lonW, double latV, double latW) {
            double phi1 = Math.toRadians(latV);
            double phi2 = Math.toRadians(latW);
            double lambda1 = Math.toRadians(lonV);
            double lambda2 = Math.toRadians(lonW);

            double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
            double x = Math.cos(phi1) * Math.sin(phi2);
            x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
            return Math.toDegrees(Math.atan2(y, x));
        }
    }
}
