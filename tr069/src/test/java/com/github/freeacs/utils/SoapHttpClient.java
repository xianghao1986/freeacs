package com.github.freeacs.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;

public class SoapHttpClient {
    public static void main(String[] args) throws IOException {
//        String soap = getFileAsString("/command/setParam.json");
//        String soap = getFileAsString("/command/getParam.json");
        String soap = getFileAsString("/command/reboot.json");
//        String soap = getFileAsString("/command/ping.json");
////
        soap = soap.replace("123456", "TestCommand-" + System.currentTimeMillis());
        String url = "http://localhost:8085/setting/setParamList";
        String response = SoapHttpClient.request(url, soap, "POST");
        System.err.println(response);
//        connReq();
//
//
//        connReqByStun();
    }
    public static void connReqByStun(){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1000);
        requestFactory.setReadTimeout(1000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        ResponseEntity<String> res = restTemplate.getForEntity("http://localhost:8087/stun/reqConn", String.class);
        System.err.println(res.toString());
    }

    public static void connReq(){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1000);
        requestFactory.setReadTimeout(1000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getForEntity("http://192.168.1.106:7546", null);
    }

    public static String request(String urlPath, String soap, String method) {
        try {
            URL url = new URL(urlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(method);
            httpURLConnection.setConnectTimeout(30000);
            httpURLConnection.setReadTimeout(30000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Content-type", "text/xml;charset=utf-8");
            if (StringUtils.isNotEmpty(soap)) {
                httpURLConnection.setRequestProperty("SOAPAction", "");
            }
            OutputStream os = httpURLConnection.getOutputStream();
            if (StringUtils.isNotEmpty(soap)) {
                os.write(soap.getBytes());
            }
            os.flush();
            if (httpURLConnection.getResponseCode() == 200) {
                //由HttpURLConnection拿到输入流
                InputStream in = httpURLConnection.getInputStream();
                StringBuffer sb = new StringBuffer();
                //根据输入流做一些IO操作
                byte[] buff = new byte[1024];
                int len = -1;
                while ((len = in.read(buff)) != -1) {
                    sb.append(new String(buff, 0, len, "utf-8"));
                }
                in.close();
                os.close();
                httpURLConnection.disconnect();
                System.err.println("收到响应报文：" + sb.toString());
                return sb.toString();
            } else {
                System.err.println("响应码：" + httpURLConnection.getResponseCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
