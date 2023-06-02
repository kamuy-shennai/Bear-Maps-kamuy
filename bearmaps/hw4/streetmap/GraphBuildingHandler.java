package bearmaps.hw4.streetmap;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 *  Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for pathfinding, under some constraints.
 *   使用XML SAX解析器解析OSM XML文件。在一些约束条件下，用于构建寻路的道路图。
 *  See OSM documentation on
 *  <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 *  <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 *  and the java
 *  <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 *
 *  You may find the CSCourseGraphDB and CSCourseGraphDBHandler examples useful.
 *  你可能会发现CSCourseGraphDB和CSCourseGraphDBHandler的例子很有用。
 *
 *  The idea here is that some external library is going to walk through the XML
 *  file, and your override method tells Java what to do every time it gets to the next
 *  element in the file. This is a very common but strange-when-you-first-see it pattern.
 *  It is similar to the Visitor pattern we discussed for graphs.
 * 这里的想法是，一些外部库将浏览XML文件，而你的覆盖方法告诉Java在每次到达文件中的下一个元素时该做什么。
 * 这是一个非常常见但又很奇怪的模式，当你第一次看到它时。 它类似于我们讨论的图形的Visitor模式。
 *  @author Alan Yao, Maurice Lee, with minor modifications by Lucas Pan for HW4, Spring 2019
 */
public class GraphBuildingHandler extends DefaultHandler {
    /**
     * Only allow for non-service roads; this prevents going on pedestrian streets as much as
     * possible. Note that in Berkeley, many of the campus roads are tagged as motor vehicle
     * roads, but in practice we walk all over them with such impunity that we forget cars can
     * actually drive on them.
     *
     * 只允许在非服务性道路上行驶；这样可以尽量避免走人行道。请注意，在伯克利，许多校园道路都被标记为机动车道，
     * 但实际上我们在上面行走时，却没有受到任何惩罚，以至于我们忘记了汽车实际上可以在上面行驶。
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private String activeState = "";
    private Node activeNode = null;
    private boolean validWay = false;
    private List<Long> nodePath = new ArrayList<>();
    private final StreetMapGraph g;

    private String wayName = "";

    public GraphBuildingHandler(StreetMapGraph g) {
        this.g = g;
    }

    /**
     * Called at the beginning of an element. Typically, you will want to handle each element in here,
     * and you may want to track the parent element.
     * 在一个元素的开头被调用。通常情况下，你会想在这里处理每个元素，你可能想跟踪父元素。
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if
     *           Namespace processing is not being performed.
     * 命名空间URI，如果该元素没有命名空间URI或不执行命名空间处理，则为空字符串。
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
     *                  本地名称（无前缀），如果不执行命名空间处理，则为空字符串。
     * @param qName The qualified name (with prefix), or the empty string if qualified names
     *             are not available. This tells us which element we're looking at.
     *              合格的名称（含前缀），如果没有合格的名称，则为空字符串。这告诉我们正在寻找哪个元素。
     * @param attributes The attributes attached to the element. If there are no attributes,
     *                   it shall be an empty Attributes object.
     *                   附加到该元素的属性。如果没有属性，它应该是一个空的Attributes对象。
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     *          任何SAX异常，可能包裹着另一个异常。
     * @see Attributes
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("node")) {
            /* We encountered a new <node...> tag. */
            activeState = "node";
//            System.out.println("Node id: " + attributes.getValue("id"));
//            System.out.println("Node lon: " + attributes.getValue("lon"));
//            System.out.println("Node lat: " + attributes.getValue("lat"));

            activeNode = Node.of(Long.parseLong(attributes.getValue("id")),
                    Double.parseDouble(attributes.getValue("lat")),
                    Double.parseDouble(attributes.getValue("lon")));
        } else if (qName.equals("way")) {
            /* We encountered a new <way...> tag. */
            activeState = "way";
//            System.out.println("Beginning a way...");
        } else if (activeState.equals("way") && qName.equals("nd")) {
            /* While looking at a way, we found a <nd...> tag. */
            //System.out.println("Id of a node in this way: " + attributes.getValue("ref"));
            nodePath.add(Long.parseLong(attributes.getValue("ref")));
        } else if (activeState.equals("way") && qName.equals("tag")) {
            /* While looking at a way, we found a <tag...> tag. */
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                //System.out.println("Highway type: " + v);
                validWay = ALLOWED_HIGHWAY_TYPES.contains(v);
            } else if (k.equals("name")) {
                //System.out.println("Way Name: " + v);
                wayName = v;
            }
//            System.out.println("Tag with k=" + k + ", v=" + v + ".");
        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
//            System.out.println("Node's name: " + attributes.getValue("v"));
            activeNode.setName(attributes.getValue("v"));
        }
    }

    /**
     * Receive notification of the end of an element. You may want to take specific terminating
     * actions here, like finalizing vertices or edges found.
     * 接收一个元素结束的通知。你可能想在这里采取特定的终止行动，比如最终确定发现的顶点或边。
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
     *命名空间URI，如果该元素没有命名空间URI或不执行命名空间处理，则为空字符串。
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
     *                  本地名称（无前缀），如果不执行命名空间处理，则为空字符串。
     * @param qName The qualified name (with prefix), or the empty string if qualified names are
     *              not available.合格的名称（含前缀），如果没有合格的名称，则为空字符串。
     * @throws SAXException  Any SAX exception, possibly wrapping another exception.任何SAX异常，可能包裹着另一个异常。
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            /* We are done looking at a way. (We finished looking at the nodes, speeds, etc...)*/
            if (validWay) {
                for (int i = 0; i < nodePath.size() - 1; i++) {
                    long fromID = nodePath.get(i);
                    long toID = nodePath.get(i + 1);
                    g.addWeightedEdge(fromID, toID, wayName);
                    g.addWeightedEdge(toID, fromID, wayName);
                }
            }
            clearStates();
        } else if (qName.equals("node")) {
            if (activeNode != null) {
                g.addNode(activeNode);
            }
            clearStates();
        }
    }

    private void clearStates() {
        activeState = "";
        activeNode = null;
        validWay = false;
        nodePath = new ArrayList<>();
        wayName = "";
    }
}
