package bearmaps.proj2c.server.handler;

import bearmaps.proj2c.server.handler.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines different paths available for our Application/Service,
 * along with the handler for each path. The handler defines the action that
 * needs to be taken in case of each path.
 * 这个类定义了我们的应用程序/服务的不同路径，以及每个路径的处理程序。处理程序定义了在每个路径下需要采取的行动。
 *
 * Created by rahul
 */
public class APIRouteHandlerFactory {

    public static final Map<String, APIRouteHandler> handlerMap;

    static {
        handlerMap = new HashMap<>();
        handlerMap.put("raster", new RasterAPIHandler());
        handlerMap.put("route", new RoutingAPIHandler());
        handlerMap.put("clear_route", new ClearRouteAPIHandler());
        handlerMap.put("search", new SearchAPIHandler());
        handlerMap.put("", new RedirectAPIHandler());
    }


}
