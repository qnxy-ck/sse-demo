package com.example.sse.controller;

import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Qnxy
 */
@RestController
@RequestMapping("/sse2")
public class SSEController2 {

    private static final Map<String, FluxSink<String>> CONNECT_CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");


    /**
     * 建立连接
     *
     * @param request 当前请求
     * @return .
     */
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> connect(ServerHttpRequest request) {
        return Flux.defer(() -> {
            final var connectId = request.getId();
            return this.doConnect(connectId);
        });
    }

    /**
     * 建立连接
     *
     * @param connectId 当前连接分配的Id
     * @return .
     */
    private Flux<String> doConnect(String connectId) {
        return Flux.<String>create(sink -> {
                    sink.next("连接成功");
                    sink.next(String.format("你的专属Id是: [%s]", connectId));

                    CONNECT_CHANNEL_MAP.put(connectId, sink);
                })
                .map(it -> String.format("%s -> %s", dtf.format(LocalDateTime.now()), it))
                .doOnTerminate(() -> this.sinkTermination(connectId))
                .doOnCancel(() -> this.sinkTermination(connectId));
    }

    /**
     * 在完成/取消/异常时执行移除建立连接管道
     */
    private void sinkTermination(String connectId) {
        CONNECT_CHANNEL_MAP.remove(connectId);
    }

    /**
     * 向指定连接发送数据
     *
     * @param connectId 建立连接的Id
     * @param value     实际数据
     * @return 师傅成功
     */
    @GetMapping("/send/{connectId}/{value}")
    public Mono<?> sendTo(@PathVariable String connectId, @PathVariable String value) {
        return Mono.justOrEmpty(CONNECT_CHANNEL_MAP.get(connectId))
                .map(it -> it.next(value))
                .map(it -> Map.of("status", true))
                .switchIfEmpty(Mono.just(Map.of("status", false)));
    }
}
