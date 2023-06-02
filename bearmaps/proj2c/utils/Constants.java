package bearmaps.proj2c.utils;

import bearmaps.proj2c.AugmentedStreetMapGraph;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * A class holding all the constant values used throughout the project
 *一个持有整个项目中使用的所有常量值的类。
 * Created by rahul
 */
public class Constants {

    /**
     * The root upper left/lower right longitudes and latitudes represent the bounding box of
     * the root tile, as the images in the img/ folder are scraped.
     * 根部的左上/右下经度和纬度代表根部瓦片的边界框，因为img/文件夹中的图像被刮走了
     * 注意这里经度是负的
     * Longitude == x-axis; latitude == y-axis.
     */
    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;

    /**
     * The OSM XML file path. Downloaded from <a href="http://download.bbbike.org/osm/">here</a>
     * using custom region selection.
     * 使用自定义区域选择。
     **/
    public static final String OSM_DB_PATH = "../library-sp19/data/proj2c_xml/berkeley-2019.osm.xml";

    /** The tile images are in the IMG_ROOT folder. 瓦片图像在IMG_ROOT文件夹中。*/
    public static final String IMG_ROOT = "../library-sp19/data/proj2c_imgs/";

    /** Route stroke information: Cyan with half transparency.路线笔划信息： 青色，半透明。 */
    public static final Color ROUTE_STROKE_COLOR = new Color(108, 181, 230, 200);

    /** Route stroke information: typically roads are not more than 5px wide.
     * 路线笔划信息：通常情况下，道路的宽度不超过5px。 */
    public static final float ROUTE_STROKE_WIDTH_PX = 5.0f;

    /** Each tile is 256x256 pixels.每块瓷砖是256x256像素 */
    public static final int TILE_SIZE = 256;

    public static AugmentedStreetMapGraph SEMANTIC_STREET_GRAPH;

    /**
     * This is used to maintain a single List of route so that the same instance(object) is accessed
     * from everywhere in the code. Enum is a cleaner way to achieve such a singleton pattern.
     * 这被用来维护一个单一的路线列表，以便从代码中的任何地方访问同一个实例（对象）。Enum是实现这种单子模式的一种更简洁的方式。
     */
    public static final List<Long> ROUTE_LIST = new LinkedList<>();
}
