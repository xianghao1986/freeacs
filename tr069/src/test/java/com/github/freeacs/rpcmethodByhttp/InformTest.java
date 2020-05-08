package com.github.freeacs.rpcmethodByhttp;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class InformTest {
    public static void main(String[] args) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);// 设置超时
        requestFactory.setReadTimeout(5000);
        HttpHeaders headers = new HttpHeaders();
    }

}
