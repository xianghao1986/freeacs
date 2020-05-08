package com.github.freeacs.config;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 请求上报处理器
 */
public class RequestReportHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestReportHandler.class);
    private static RestTemplate restTemplate;
    private static HttpHeaders headers;

    static {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1000);
        requestFactory.setReadTimeout(1000);
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate = new RestTemplate(requestFactory);
    }

    public static void asyncReport(String json, String url) {
        AsyncHandler.execute(new Runnable() {
            @Override
            public void run() {
                HttpEntity httpEntity = new HttpEntity(json, headers);
                String res = restTemplate.postForObject(url, httpEntity, String.class);
                logger.info("数据上报处理结果：" + res);
            }
        });

    }

}
