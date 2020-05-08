package com.github.freeacs.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.freeacs.config.AsyncHandler;
import com.github.freeacs.tr069.CommandService;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.entity.CmdTypeEnum;
import com.github.freeacs.tr069.entity.CommandParam;
import com.github.freeacs.tr069.entity.Device;
import com.github.freeacs.tr069.repository.DeviceRepository;
import de.javawi.jstun.test.demo.StunServer;
import de.javawi.jstun.util.Address;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.SQLException;
import java.util.List;

import static java.lang.System.currentTimeMillis;

/**
 * 接收业务系统下发的命令
 */
@Slf4j
@RestController
public class CommandController {

    private final CommandService commandService;
    private final DeviceRepository deviceRepository;
    private final Properties properties;

    public CommandController(CommandService commandService, DeviceRepository deviceRepository, Properties properties) {
        this.commandService = commandService;
        this.deviceRepository = deviceRepository;
        this.properties = properties;
    }

    @PostMapping(value = "/setting/setParamList")
    public String submitCommand(@RequestBody String reqBody) throws SQLException {
        JSONObject jsonObject = JSON.parseObject(reqBody);
        String deviceId = jsonObject.getString("deviceId");
        String cmdType = jsonObject.getString("command");
        if (CmdTypeEnum.getDescByType(cmdType) == null) {
            String msg = "不支持命令:" + cmdType;
            log.error(msg);
            return msg;
        }
        String cmdSeq = jsonObject.getString("sessionId");
        if (StringUtils.isEmpty(cmdSeq)) {
            String msg = "sessionId为空！";
            log.error(msg);
            return msg;
        }
        JSONArray parameterListJson = jsonObject.getJSONArray("dataList");
        List<CommandParam> params = null;
        if (parameterListJson != null && parameterListJson.size() > 0) {
            params = parameterListJson.toJavaList(CommandParam.class);
        }
        JSONArray retParameterListJson = jsonObject.getJSONArray("retDataList");
        List<CommandParam> retParams = null;
        if (retParameterListJson != null && retParameterListJson.size() > 0) {
            retParams = retParameterListJson.toJavaList(CommandParam.class);
        }

        String result = commandService.addCommand(deviceId, cmdType, cmdSeq, params, retParams);
        if (!result.equals("00")) {
            return result;
        }
        AsyncHandler.execute(new Runnable() {
            @Override
            public void run() {
                requestClient(deviceId);
            }
        });
        return "00";
    }

    private void requestClient(String deviceId) {
        Device device = deviceRepository.findFirstBySerialNumber(deviceId);
        if (device != null) {
            if (properties.isStunEnable() && device.isNatDetected()) {
                log.info("发送udp连接请求");
                try {
                    String uri = device.getUdpConnectionRequestAddress();
                    if (StringUtils.isNotEmpty(uri)) {
                        DatagramSocket socket = StunServer.newestReceiveSocket;
                        String req = new StringBuilder().append("GET http://").append(uri)
                                .append("?ts=").append(currentTimeMillis())
                                .append("&id=").append(currentTimeMillis())
                                .append("&un=cpe").append("&cn=8837237846066432308")
                                .append("&sig=B748ED10822125901DDFB74983EF95AC18DAA299").toString();
                        DatagramPacket send = new DatagramPacket(req.getBytes(), req.getBytes().length);
                        send.setPort(Integer.parseInt(uri.split(":")[1]));
                        Address address = new Address(uri.split(":")[0]);
                        send.setAddress(address.getInetAddress());
                        socket.send(send);
                    }
                } catch (Exception e) {
                    log.error("发送udp请求失败", e);
                }
            } else {//
                log.info("发送http连接请求");
                if (StringUtils.isNotEmpty(device.getConnectionRequestUrl())) {
                    try {
                        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                        requestFactory.setConnectTimeout(1000);
                        requestFactory.setReadTimeout(1000);
                        RestTemplate restTemplate = new RestTemplate(requestFactory);
                        restTemplate.getForEntity(device.getConnectionRequestUrl(), null);
                    } catch (RestClientException e) {
                        log.error("ManagementServer.ConnectionRequestURL请求异常", e);
                    }
                }

            }
        }
    }
}
