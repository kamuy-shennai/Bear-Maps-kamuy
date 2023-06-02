package bearmaps.proj2c;

import bearmaps.proj2c.server.handler.APIRouteHandler;
import bearmaps.proj2c.utils.Constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static spark.Spark.*;

/**
 * 地图服务器初始化
 */
public class MapServerInitializer {


    /**
     * Place any initialization statements that will be run before the server main loop here.
     * 将任何将在服务器主循环之前运行的初始化语句放在这里。
     * Do not place it in the main function. Do not place initialization code anywhere else.
     * 不要把它放在主函数中。不要把初始化代码放在其他地方。
     **/
    public static void initializeServer(Map<String, APIRouteHandler> apiHandlers){

        Constants.SEMANTIC_STREET_GRAPH = new AugmentedStreetMapGraph(Constants.OSM_DB_PATH);
        staticFileLocation("/page");
        /* Allow for all origin requests (since this is not an authenticated server, we do not care about CSRF).
        * 允许所有来源的请求（因为这不是一个认证的服务器，我们不关心CSRF的问题  */
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });

        Set<String> paths = new HashSet<>();
        for(Map.Entry<String, APIRouteHandler> apiRoute: apiHandlers.entrySet()){
            if(paths.contains(apiRoute.getKey())){
                throw new RuntimeException("Duplicate API Path found发现重复的API路径");
            }
            get("/"+apiRoute.getKey(), apiRoute.getValue());
            paths.add(apiRoute.getKey());
        }


    }
}
