package io.vertx.starter;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    VertxOptions options = new VertxOptions().
      setWorkerPoolSize(40);
    Vertx vertx = Vertx.vertx();
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    WebClient client;
    WebClientOptions webclientoptions = new WebClientOptions()
      .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36");
    webclientoptions.setKeepAlive(false);
    client = WebClient.create(vertx,webclientoptions);



    /*测试用例*/
    Route testroute = router.route(HttpMethod.POST, "/test");
    /*文章抓取*/
    Route articlespider = router.route(HttpMethod.POST, "/articlespider");
    /*获取html*/
    Route sendurl = router.route(HttpMethod.GET, "/sendurl");

    testroute.handler(BodyHandler.create())
      .handler(routingContext -> {
        HttpServerResponse response = routingContext.response();
        System.out.println(routingContext.getBodyAsJson());
        response.putHeader("content-type", "text/json");
        response.end("123");
      });

    articlespider
      .handler(BodyHandler.create())
      .handler(routingContext -> {
        HttpServerResponse response = routingContext.response();
        System.out.println(routingContext.getBodyAsJson().getString("name"));
        response.putHeader("content-type", "text/json");
        response.end();
      });

    sendurl
      .handler(BodyHandler.create())
      .handler(routingContext -> {
        HttpServerResponse routresponse = routingContext.response();
        JsonObject jo = routingContext.getBodyAsJson();

        client.get("www.baidu.com","/").send(ar -> {
          if (ar.succeeded()) {
            // 获取响应
            HttpResponse<Buffer> response = ar.result();
            routresponse.end(response.body());
            System.out.println("Received response with status code" + response.statusCode());
          } else {
            System.out.println("Something went wrong " + ar.cause().getMessage());
          }
        });
      });


    server.requestHandler(router::accept).listen(8080);
  }
}
