package bearmaps.proj2c;

import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;
import bearmaps.proj2ab.KDTree;
import bearmaps.proj2ab.Point;

import java.util.*;

/**
 *
 *  一个比StreetMapGraph更强大的增强图。
 *  具体来说，它支持以下额外操作：
 *  1.寻找最近有效点
 *  2.前缀搜索
 *  3.完整名称地点搜索
 */

public class AugmentedStreetMapGraph extends StreetMapGraph {

    //KD树，用来寻找最近有效点
    private final KDTree kdTree ;
    private final HashMap<Point,Long> pToId=new HashMap<>();
    private final HashMap<String, HashSet<String>> cleanToFull;
    //存放所有完整的地点名称
    private final HashMap<String, HashSet<Node>> cleanLocations;
    //字典树，用来进行前缀搜索
    private final TrieSet trieSet;
    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        List<Node> nodes = this.getNodes();
        List<Point> point = new LinkedList<>();
        cleanToFull = new HashMap<>();
        cleanLocations = new HashMap<>();
        trieSet = new TrieSet();
        for (Node node : nodes) {
            point.add(new Point(node.lon(), node.lat()));
            pToId.put(new Point(node.lon(), node.lat()), node.id());
            String fullName = node.name();
            if (fullName != null) {
                String cleanedName = cleanString(fullName);
                trieSet.add(cleanedName);

                if (!cleanToFull.containsKey(cleanedName)) {
                    HashSet<String> full = new HashSet<>();
                    full.add(fullName);
                    cleanToFull.put(cleanedName, full);
                } else {
                    HashSet<String> full = cleanToFull.get(cleanedName);
                    full.add(fullName);
                }
                if (!cleanLocations.containsKey(cleanedName)) {
                    HashSet<Node> loc = new HashSet<>();
                    loc.add(node);
                    cleanLocations.put(cleanedName, loc);
                } else {
                    HashSet<Node> loc = cleanLocations.get(cleanedName);
                    loc.add(node);
                }
            }
        }
        kdTree = new KDTree(point);
    }


    /**
     * 返回最接近给定经度和纬度的顶点。
     * @param lon 原始点经度
     * @param lat 原始点纬度
     * @return 图中最接近目标的节点的ID。
     */
    public long closest(double lon, double lat) {
        System.out.println("原始点："+lon+","+lat);
        Point n = kdTree.nearest(lon,lat);
        System.out.println("最近点："+n.getX()+","+n.getY());
        return pToId.get(n);
    }


    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *  在线性时间内，收集所有与查询字符串前缀匹配的OSM地点名称。
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        List<String> found = new LinkedList<>();
        List<String> keys = trieSet.keysWithPrefix(cleanString(prefix));
        // List of all keys matching prefix
        for (String k : keys) {
            HashSet<String> full = cleanToFull.get(k);
            if (full != null) {
                found.addAll(full);
            }
        }
        return found;
    }

    /**
     * Collect all locations that match a cleaned locationName, and return
     * information about each node that matches.
     * 收集与清理后的 locationName 匹配的所有位置，并返回有关每个匹配节点的信息。
     * @param locationName A full name of a location searched for.搜索位置的全名。
     * @return A list of locations whose cleaned name matches the
     * cleaned locationName, and each location is a map of parameters for the Json
     * response as specified: 清理后的名称与清理后的 locationName 匹配的位置列表，每个位置都是指定的 Json 响应的参数映射：
     * "lat" -> Number, The 纬度of the node.
     * "lon" -> Number, The 经度 of the node.
     * "name" -> String, The actual name of the node.
     * "id" -> Number, The id of the node.
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        HashSet<Node> nodes = cleanLocations.get(cleanString(locationName));
        ArrayList<Map<String, Object>> loc = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            Map<String, Object> l = new HashMap<>(6);
            l.put("lat", n.lat());
            l.put("lon", n.lon());
            l.put("name", n.name());
            l.put("id", n.id());
            loc.add(l);
        }
        return loc;
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * 对字符串进行clean处理，去掉标点符号和大写字母。
     * @param s 输入字符串
     * @return clean处理后的字符串
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
