package com.zhuaer.learning.sentinel.handler;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;

import reactor.core.publisher.Mono;
/**
 * @ClassName JsonSentinelGatewayBlockExceptionHandler
 * @Description TODO
 * @Author zhua
 * @Date 2020/9/5 15:44
 * @Version 1.0
 */

public class JsonSentinelGatewayBlockExceptionHandler implements WebExceptionHandler {

    private List<ViewResolver> viewResolvers;
    private List<HttpMessageWriter<?>> messageWriters;

    public JsonSentinelGatewayBlockExceptionHandler(
            List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolvers;
        this.messageWriters = serverCodecConfigurer.getWriters();
    }
    /**
     * 自定义返回
     * @param response
     * @param exchange
     * @return
     */
    private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange) {
        ServerHttpResponse resp = exchange.getResponse();
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String json = "{\"code\": -1, \"data\": null, \"msg\": \"系统限流\"}";
        DataBuffer buffer = resp.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Mono.just(buffer));
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }
        return handleBlockedRequest(exchange, ex)
                .flatMap(response -> writeResponse(response, exchange));
    }
    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }
    private final Supplier<ServerResponse.Context> contextSupplier = () -> new ServerResponse.Context() {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return JsonSentinelGatewayBlockExceptionHandler.this.messageWriters;
        }
        @Override
        public List<ViewResolver> viewResolvers() {
            return JsonSentinelGatewayBlockExceptionHandler.this.viewResolvers;
        }
    };

}
