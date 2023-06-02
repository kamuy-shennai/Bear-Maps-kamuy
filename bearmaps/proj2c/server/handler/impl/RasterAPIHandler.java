package bearmaps.proj2c.server.handler.impl;

import bearmaps.proj2c.AugmentedStreetMapGraph;
import bearmaps.proj2c.server.handler.APIRouteHandler;
import spark.Request;
import spark.Response;
import bearmaps.proj2c.utils.Constants;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bearmaps.proj2c.utils.Constants.SEMANTIC_STREET_GRAPH;
import static bearmaps.proj2c.utils.Constants.ROUTE_LIST;

/**
 * Handles requests from the web browser for map images. These images will be rastered into one large image to be
 * displayed to the user.
 *处理来自网络浏览器对地图图像的请求。这些图像将被光栅化成一个大的图像，显示给用户。
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {
    private static final double ROOT_BASE_X = Constants.ROOT_LRLON - Constants.ROOT_ULLON;
    private static final double ROOT_BASE_Y = Constants.ROOT_ULLAT - Constants.ROOT_LRLAT;

    private final List<Double> lonDPP = new ArrayList<>();
    private final List<Double> xWidthLon = new ArrayList<>();
    private final List<Double> yHeightLat = new ArrayList<>();

    /**
     * 初始化一些东西。
     */
    public RasterAPIHandler() {
        double baseLon = Math.abs(Constants.ROOT_LRLON - Constants.ROOT_ULLON);
        double baseLat = Math.abs(Constants.ROOT_ULLAT - Constants.ROOT_LRLAT);
        for (int i = 0; i < 8; i++) {
            double pow = Math.pow(2, i);
            double units = Constants.TILE_SIZE * pow;
            lonDPP.add(i, baseLon / units);
            xWidthLon.add(i, baseLon / pow);
            yHeightLat.add(i, baseLat / pow);
        }
    }
    /**
     * Each raster request to the server will have the following parameters as keys in the params map accessible by,
     * 每个向服务器发出的光栅请求都会有以下参数，作为params地图中的键，可以通过以下方式访问、
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest().
     * ullat : upper left corner latitude,  ullon : upper left corner longitude,
     * lrlat : lower right corner latitude, lrlon : lower right corner longitude
     * w : user viewport window width in pixels, h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

    /**
     * The result of rastering must be a map containing all of the fields listed in the comments
     * for RasterAPIHandler.processRequest.
     * 结果必须是一张包含RasterAPIHandler.processRequest注释中所列所有字段的地图。
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};


    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *接受用户的查询，并找到最符合查询的图片网格。这些图像将被前端合并成一张大的图像。
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     图像的网格必须遵守以下属性，其中网格中的图像被称为 "瓦片"。
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size.
     *         所收集的瓦片必须尽可能地覆盖每像素的纵向距离（LonDPP），同时仍然覆盖小于
     *         或等于用户视口尺寸的查询框中每像素的纵向距离量。在用户视口尺寸的查询框中的每像素纵向距离。</li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.包含所有满足上述条件的与查询边界框相交的瓷砖。</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.
     *         瓷砖必须按顺序排列，以重建完整的图像。</li>
     *     </ul>
     *
     * @param requestParams Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height. HTTP GET请求的查询参数的地图--查询框和用户视口的宽度和高度。
     *
     * @param response  Not used by this function. You may ignore.本函数不使用。你可以忽略不计。
     * @return A map of results for the front end as specified指定的前端的结果地图:
     * "render_grid"   : String[][], the files to display.
     * "raster_ul_lon" : Number，是指光栅图像的左上经度的边界。
     * "raster_ul_lat" : Number，是指光栅图像的左上角纬度的边界。
     * "raster_lr_lon" : Number，是指光栅图像的右下经度的边界。
     * "raster_lr_lat" : Number，即光栅图像的边界右下角纬度。
     * "depth"         : Number，即光栅图像的节点深度；也可以解释为图像字符串中数字的长度。
     * "query_success" : Boolean, 查询是否能够成功完成；不要忘记在成功时将其设置为true！
     */
    @Override
    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
        //处理请求
        Map<String, Object> results = new HashMap<>();
        final double reqLeft = requestParams.get("ullon");
        final double reqRight = requestParams.get("lrlon");
        final double reqUp = requestParams.get("ullat");
        final double reqDown = requestParams.get("lrlat");

        if (reqRight > Constants.ROOT_ULLON && reqLeft < Constants.ROOT_LRLON &&
                reqDown < Constants.ROOT_ULLAT && reqUp > Constants.ROOT_LRLAT) {//如果完全在地图外面
            double queryLonDPP = (reqRight - reqLeft) / requestParams.get("w");
            // 匹配深度
            int depth = getDepth(queryLonDPP);
            // 计算得出最左边的第一个图片x坐标//计算左边第一个完整的图片的x坐标
            double edgeLeft = (reqLeft - Constants.ROOT_ULLON) / xWidthLon.get(depth);
            int xleft;
            if(edgeLeft<0)
            {
                xleft = 0;
            }else {
                xleft = (int) Math.floor(edgeLeft);
            }
            // 计算得出最右边的图片的x坐标//计算右边第一个完整的图片的x坐标
            int xright;
            if(reqRight>Constants.ROOT_LRLON){
                xright = (int) Math.pow(2,depth)-1;
            }else {
                double edgeRight = (reqRight - Constants.ROOT_ULLON) / xWidthLon.get(depth);
                xright = (int) Math.ceil(edgeRight);
            }

            // Upper Top
            double edgeUp = (Constants.ROOT_ULLAT - reqUp) / yHeightLat.get(depth);
            int yUp;
            if(edgeUp<0)
            {
                yUp = 0;
            }else {
                yUp = (int) Math.floor(edgeUp);
            }
            // Lower Bot
            double edgeDown = (Constants.ROOT_ULLAT - reqDown) / yHeightLat.get(depth);
            int yDown;
            if(reqDown<Constants.ROOT_LRLAT){
                yDown = (int) Math.pow(2,depth)-1;
            }else {
                yDown = (int) Math.floor(edgeDown);
            }

            //int yDown = Math.min((int) ((Constants.ROOT_ULLAT - reqDown) / yHeightLat.get(depth)),
            //        (int) (ROOT_BASE_Y / yHeightLat.get(depth)) - 1);
            System.out.println("UP: " + yUp + " DOWN: " + yDown);
            System.out.println("Left: " + xleft + " Right: " + xright);

            // 传入
            String[][] renderGrid = new String[yDown - yUp + 1][xright - xleft + 1];
            int row = 0;
            int col = 0;
            for (int y = yUp; y <= yDown; y++) {
                for (int x = xleft; x <= xright; x++) {
                    renderGrid[row][col] = "d" + depth + "_x" + x + "_y" + y + ".png";
                    col++;
                }
                row++;
                col = 0; // reset col
            }
            results.put("render_grid", renderGrid);
            results.put("raster_ul_lon", Constants.ROOT_ULLON + xleft * xWidthLon.get(depth));
            results.put("raster_ul_lat", Constants.ROOT_ULLAT - yUp * yHeightLat.get(depth));
            results.put("raster_lr_lon", Constants.ROOT_ULLON + (1 + xright) * xWidthLon.get(depth));
            results.put("raster_lr_lat", Constants.ROOT_ULLAT - (1 + yDown) * yHeightLat.get(depth));
            results.put("depth", depth);
            results.put("query_success", true);
        } else {
            results = queryFail();
        }
        if(!validateRasteredImgParams(results))results=queryFail();
        return results;
    }

    /**
     *
     * @param queryLonDPP 计算得出的LonDPP
     * @return 返回整数等级
     */
    private int getDepth(double queryLonDPP) {
        int depth = -1;
        for (int i = 0; i < lonDPP.size(); i++) {
            if (lonDPP.get(i) < queryLonDPP || i == 7) {
                depth = i;
                break;
            }
        }
        return depth;
    }

    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }

    /**
     * 当结果错误/失败，执行函数，对结果进行修改而不至于屏幕上出现莫名其妙的内容。
     * @return 修改后的result
     */
    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        String[][] renderGrid = new String[][]{{"d1_x0_y0.png", "d1_x1_y0.png"},
                                        {"d1_x0_y1.png","d1_x1_y1.png"}};
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", Constants.ROOT_ULLON);
        results.put("raster_ul_lat", Constants.ROOT_ULLAT);
        results.put("raster_lr_lon", Constants.ROOT_LRLON);
        results.put("raster_lr_lat", Constants.ROOT_LRLAT);
        results.put("depth", 3);
        results.put("query_success", true);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     * 验证Rasterer已经返回一个可以渲染的结果。
     * @param rip : Parameters provided by the rasterer 提供的参数
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("你的光栅处理结果缺少 " + p);

                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success被报告为失败");
                return false;
            }
        }
        return true;
    }

    /**
     * 将rasteredImgParams对应的图像写到输出流。
     *
     */
    private  void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                                  ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;
        //拼图
        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (String[] strings : renderGrid) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                //System.out.println(Constants.IMG_ROOT + strings[c]);
                graphic.drawImage(getImage(Constants.IMG_ROOT + strings[c]), x, y, null);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it.
        *  如果有一条路线，就画出来。*/
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (!route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
                File in = new File(imgPath);
                tileImg = ImageIO.read(in);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }
}
