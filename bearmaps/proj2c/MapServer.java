package bearmaps.proj2c;


import bearmaps.proj2c.server.handler.APIRouteHandlerFactory;

/**
 * 这段代码使用的是BearMaps骨架代码4.0版。
 */
public class MapServer {


    /**
     * 这里是启动MapServer的地方。
     *
     */
    public static void main(String[] args) {

        MapServerInitializer.initializeServer(APIRouteHandlerFactory.handlerMap);

    }

}
