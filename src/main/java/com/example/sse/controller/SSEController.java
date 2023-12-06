package com.example.sse.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author Qnxy
 */
@RestController
@RequestMapping("/sse")
public class SSEController {


    /**
     * 服务器每隔参数 sec 秒推送一次
     *
     * @param sec 推送间隔(秒)
     * @return .
     */
    @GetMapping(value = "/interval/{sec}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> interval(@PathVariable Integer sec) {
        return Flux.interval(Duration.ofSeconds(sec));
    }

    
   

}
