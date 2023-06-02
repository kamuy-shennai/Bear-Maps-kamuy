package bearmaps.proj2c.server.handler;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Set;

import static spark.Spark.halt;

/**
 * This is the base class that defines the procedure for handling an API request
 * 这是一个基类，定义了处理API请求的程序
 * The process is defined as such that first the request parameters are read, then request is process based on those parameters and finally the response is built.
 *这个过程是这样定义的：首先读取请求参数，然后根据这些参数对请求进行处理，最后建立响应。
 * Created by rahul
 */
public abstract class APIRouteHandler<Req, Res> implements Route {

    /** HTTP failed response. HTTP响应失败。 */
    private static final int HALT_RESPONSE = 403;

    private Gson gson;

    public APIRouteHandler() {
        gson = new Gson();
    }

    @Override
    public Object handle(Request request, Response response) {
        Req requestParams = parseRequestParams(request);
        Res result = processRequest(requestParams, response);
        return buildJsonResponse(result);
    }

    /**
     * Defines how to parse and extract the request parameters from request
     * 定义了如何从请求中解析和提取请求参数。
     * @param request   the request object received
     * @return  extracted request parameters
     */
    protected abstract Req parseRequestParams(Request request);

    /**
     * Process the request using the given parameters
     * 使用给定的参数处理该请求
     * @param requestParams request parameters
     * @param response  response object
     * @return  the result computed after processing request
     */
    protected abstract Res processRequest(Req requestParams, Response response);

    /**
     * Builds a JSON response to return from the result object
     * 构建一个JSON响应，从结果对象中返回。
     * @param result
     * @return
     */
    protected  Object buildJsonResponse(Res result){
        return gson.toJson(result);
    }

    /**
     * Validate & return a parameter map of the required request parameters.
     * 验证并返回所需请求参数的参数图。
     * Requires that all input parameters are doubles.
     * 要求所有输入参数都是成对的。
     * @param req HTTP 请求.
     * @param requiredParams 验证的模板.
     * @return A populated map of input parameter to its numerical value. 输入参数到其数值的填充图。
     *
     */
    protected  HashMap<String, Double> getRequestParams(
            spark.Request req, String[] requiredParams) {
        Set<String> reqParams = req.queryParams();
        HashMap<String, Double> params = new HashMap<>();
        for (String param : requiredParams) {
            if (!reqParams.contains(param)) {
                halt(HALT_RESPONSE, "Request failed - parameters missing.");
            } else {
                try {
                    params.put(param, Double.parseDouble(req.queryParams(param)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    halt(HALT_RESPONSE, "Incorrect parameters - provide numbers.");
                }
            }
        }
        return params;
    }
}
