package com.github.freeacs.tr069.methods;

import com.alibaba.fastjson.JSONObject;
import com.github.freeacs.config.RequestReportHandler;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.tr069.CommandService;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.base.Log;
import com.github.freeacs.tr069.entity.CmdTypeEnum;
import com.github.freeacs.tr069.entity.Command;
import com.github.freeacs.tr069.entity.CommandParam;
import com.github.freeacs.tr069.entity.Device;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.decision.DecisionStrategy;
import com.github.freeacs.tr069.methods.request.RequestProcessStrategy;
import com.github.freeacs.tr069.methods.response.ResponseCreateStrategy;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.repository.DeviceRepository;
import com.github.freeacs.tr069.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.freeacs.tr069.CwmpVersion.extractVersionFrom;
import static com.github.freeacs.tr069.methods.ProvisioningMethod.extractMethodFrom;

@Slf4j
public abstract class ProvisioningStrategy {

    public abstract void process(HTTPRequestResponseData reqRes) throws Exception;

    public static ProvisioningStrategy getStrategy(Properties properties, DBI dbi, CommandService commandService, DeviceRepository deviceRepository) {
        return new NormalProvisioningStrategy(properties, dbi, commandService, deviceRepository);
    }

    private static class NormalProvisioningStrategy extends ProvisioningStrategy {

        private final Properties properties;
        private final DBI dbi;
        private final CommandService commandService;
        private final DeviceRepository deviceRepository;

        private NormalProvisioningStrategy(Properties properties, DBI dbi, CommandService commandService, DeviceRepository deviceRepository) {
            this.properties = properties;
            this.dbi = dbi;
            this.commandService = commandService;
            this.deviceRepository = deviceRepository;
        }

        @Override
        public void process(HTTPRequestResponseData reqRes) throws Exception {
            // 0. Pre-processing
            String xml = reqRes.getRequestData().getXml();
            ProvisioningMethod requestMethod = extractMethodFrom(xml);
            reqRes.getRequestData().setXml(XMLFormatterUtils.filterInvalidCharacters(xml));
            reqRes.getRequestData().setMethod(requestMethod.name());
            reqRes.getSessionData().setCwmpVersionNumber(extractVersionFrom(xml));

            // 1. process the request
            logWillProcessRequest(reqRes);
            RequestProcessStrategy.getStrategy(requestMethod, properties, dbi).process(reqRes);
            if (Log.isConversationLogEnabled()) {
                logConversationRequest(reqRes);
            }
            //
            SessionData sessionData = reqRes.getSessionData();
            Command cacheCommand = sessionData.getCommand();
            if (cacheCommand != null &&
                    Command.STATUS_FINISHED.equals(cacheCommand.getCmdStatus())) {
                commandService.save(cacheCommand);
                reqRes.getSessionData().setCommand(null);
                if (CmdTypeEnum.GETPARAM.getType().equals(cacheCommand.getCmdType())){
                    log.info("上报查询参数命令结果");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", CmdTypeEnum.GETPARAM.getType());
                    jsonObject.put("deviceId", sessionData.getSerialNumber());
                    jsonObject.put("model", sessionData.getDevice().getProductClass());
                    jsonObject.put("protocol", "TR069");
                    jsonObject.put("result", cacheCommand.getResult());
                    if (Command.RESULT_SUCCESS.equals(cacheCommand.getResult())) {
                        jsonObject.put("dataList", cacheCommand.getReportParams());
                    }else{
                        jsonObject.put("fault", cacheCommand.getFault());
                    }
                    String monitorUrl = properties.getValueByName("report.monitor.url", "http://127.0.0.1:8085/report");
                    RequestReportHandler.asyncReport(jsonObject.toString(), monitorUrl);
                }
                else if (CmdTypeEnum.PING.getType().equals(cacheCommand.getCmdType())){
                    log.info("上报Ping命令结果");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", CmdTypeEnum.PING.getType());
                    jsonObject.put("deviceId", sessionData.getSerialNumber());
                    jsonObject.put("model", sessionData.getDevice().getProductClass());
                    jsonObject.put("protocol", "TR069");
                    jsonObject.put("result", cacheCommand.getResult());
                    if (Command.RESULT_SUCCESS.equals(cacheCommand.getResult())) {
                        jsonObject.put("dataList", cacheCommand.getReportParams());
                    }else{
                        jsonObject.put("fault", cacheCommand.getFault());
                    }
                    String monitorUrl = properties.getValueByName("report.monitor.url", "http://127.0.0.1:8085/report");
                    RequestReportHandler.asyncReport(jsonObject.toString(), monitorUrl);
                }else if (CmdTypeEnum.SETPARAM.getType().equals(cacheCommand.getCmdType())){
                    log.info("上报设置参数命令结果");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", CmdTypeEnum.SETPARAM.getType());
                    jsonObject.put("deviceId", sessionData.getSerialNumber());
                    jsonObject.put("model", sessionData.getDevice().getProductClass());
                    jsonObject.put("protocol", "TR069");
                    jsonObject.put("result", cacheCommand.getResult());
                    if (!Command.RESULT_SUCCESS.equals(cacheCommand.getResult())) {
                        jsonObject.put("fault", cacheCommand.getFault());
                    }
                    String monitorUrl = properties.getValueByName("report.monitor.url", "http://127.0.0.1:8085/report");
                    RequestReportHandler.asyncReport(jsonObject.toString(), monitorUrl);
                }

            } //Inform请求携带的任务执行结果信息，需要在这里处理
            if (requestMethod == ProvisioningMethod.GetParameterValues) {//更新device信息
                List<ParameterValueStruct> values = sessionData.getValuesFromCPE();
                Device device = sessionData.getDevice();
                for (ParameterValueStruct struct : values) {
                    if (struct.getName().equals("Device.ManagementServer.PeriodicInformInterval")) {
                        device.setPeriodicInformInterval(Integer.parseInt(struct.getValue()));
                    } else if (struct.getName().equals("Device.ManagementServer.ConnectionRequestURL")) {
                        device.setConnectionRequestUrl(struct.getValue());
                    } else if (struct.getName().equals("Device.ManagementServer.ConnectionRequestPassword")) {
                        device.setConnectionRequestPassword(struct.getValue());
                    } else if (struct.getName().equals("Device.ManagementServer.ConnectionRequestUsername")) {
                        device.setConnectionRequestUsername(struct.getValue());
                    } else if (struct.getName().equals("Device.ManagementServer.STUNEnable")) {
                        device.setStunEnable("1".equals(struct.getValue()));
                    } else if (struct.getName().equals("Device.ManagementServer.NATDetected")) {
                        device.setNatDetected("true".equalsIgnoreCase(struct.getValue()));
                    } else if (struct.getName().equals("Device.ManagementServer.UDPConnectionRequestAddress")) {
                        device.setUdpConnectionRequestAddress(struct.getValue());
                    }
                }
                deviceRepository.save(device);
            }
            if (requestMethod == ProvisioningMethod.Inform) {
                String serialNumber = sessionData.getSerialNumber();
                Parser parser = sessionData.getParser();
                if (StringUtils.isNotEmpty(serialNumber)) {
                    Device device = deviceRepository.findFirstBySerialNumber(serialNumber);
                    if (device == null) {
                        device = new Device();
                        device.setSerialNumber(serialNumber);
                        if (parser != null) {
                            device.setManufacturer(parser.getDeviceIdStruct().getManufacturer());
                            device.setProductClass(parser.getDeviceIdStruct().getProductClass());
                            device.setOui(parser.getDeviceIdStruct().getOui());
                        }
                        device = deviceRepository.save(device);
                    }
                    sessionData.setDevice(device);
                }
                if (sessionData.isPeriodic()) {//周期上报
                    List<ParameterValueStruct> parameterValues = parser.getParameterList().getParameterValueList();
                    if (parameterValues.size() > 0) {
                        log.info("上报inform参数");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("command", "Inform");
                        jsonObject.put("deviceId", sessionData.getSerialNumber());
                        jsonObject.put("model", sessionData.getDevice().getProductClass());
                        jsonObject.put("protocol", "TR069");
                        jsonObject.put("dataList", parameterValues);
                        String monitorUrl = properties.getValueByName("report.monitor.url", "http://127.0.0.1:8085/report");
                        RequestReportHandler.asyncReport(jsonObject.toString(), monitorUrl);
                    }
                }
                if (sessionData.isBooted()) {
                    Command command = commandService.findProcessingRebootCommand(reqRes.getSessionData().getSerialNumber());
                    if (command != null) {
                        log.info("有重启的任务，需要处理结果");
                        command.setResult(Command.RESULT_SUCCESS);
                        command.setCmdStatus(Command.STATUS_FINISHED);
                        command.setEndTime(new Date());
                        commandService.save(command);
                        log.info("上报重启成功结果");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("command", CmdTypeEnum.REBOOT.getType());
                        jsonObject.put("deviceId", sessionData.getSerialNumber());
                        jsonObject.put("model", sessionData.getDevice().getProductClass());
                        jsonObject.put("protocol", "TR069");
                        jsonObject.put("result", "0");
                        String monitorUrl = properties.getValueByName("report.monitor.url", "http://127.0.0.1:8085/report");
                        RequestReportHandler.asyncReport(jsonObject.toString(), monitorUrl);
                    }
                }
                if (sessionData.isDiagnosticsComplete()) {
                    Command command = commandService.findProcessingPingCommand(reqRes.getSessionData().getSerialNumber());
                    if (command != null) {
                        log.info("收到诊断完成Inform通知，下发参数查询诊断结果");
                        reqRes.getResponseData().setMethod(ProvisioningMethod.GetParameterValues.name());
                        List<CommandParam> paramList = command.getDiagnosticParams();
                        List<ParameterValueStruct> requestedCPE = new ArrayList<>();
                        for (CommandParam cp : paramList) {
                            ParameterValueStruct pvs = new ParameterValueStruct(cp.getName(), null);
                            requestedCPE.add(pvs);
                        }
                        reqRes.getSessionData().setRequestedCPE(requestedCPE);
                        reqRes.getSessionData().setCommand(command);
                    }
                }
            }

            // 2. decide what to do next
            DecisionStrategy.getStrategy(requestMethod, properties, dbi).makeDecision(reqRes);
            //by xianghao
            Command command = null;

            if (ProvisioningMethod.GetParameterValues.name().equals(reqRes.getResponseData().getMethod())) {
                log.info("检查是否有任务需要执行");
                command = commandService.selectOneOfWaitingCommand(reqRes.getSessionData().getSerialNumber());
                if (command != null) {
                    if (command.getCmdType().equals(CmdTypeEnum.SETPARAM.getType())) {
                        log.info("有[设置参数]任务需要执行");
                        reqRes.getResponseData().setMethod(ProvisioningMethod.SetParameterValues.name());
                        List<CommandParam> paramList = command.getParams();
                        ParameterList toCPE = new ParameterList();
                        for (CommandParam cp : paramList) {
                            ParameterValueStruct pvs = new ParameterValueStruct(cp.getName(), cp.getValue(), "xsd:" + cp.getType());
                            toCPE.addParameterValueStruct(pvs);
                        }
                        reqRes.getSessionData().setToCPE(toCPE);
                        reqRes.getSessionData().setCommand(command);
                    } else if (command.getCmdType().equals(CmdTypeEnum.GETPARAM.getType())) {
                        log.info("有[参数查询]任务需要执行");
                        List<CommandParam> paramList = command.getParams();
                        List<ParameterValueStruct> requestedCPE = new ArrayList<>();
                        for (CommandParam cp : paramList) {
                            ParameterValueStruct pvs = new ParameterValueStruct(cp.getName(), null);
                            requestedCPE.add(pvs);
                        }
                        reqRes.getSessionData().setRequestedCPE(requestedCPE);
                        reqRes.getSessionData().setCommand(command);
                    } else if(command.getCmdType().equals(CmdTypeEnum.REBOOT.getType())){
                        log.info("有["+CmdTypeEnum.REBOOT.getDesc()+"]需要执行");
                        reqRes.getResponseData().setMethod(ProvisioningMethod.Reboot.name());
                        reqRes.getSessionData().setCommand(command);
                    } else if (command.getCmdType().equals(CmdTypeEnum.PING.getType())){
                        log.info("有["+CmdTypeEnum.PING.getDesc()+"]需要执行，下发设置诊断参数");
                        reqRes.getResponseData().setMethod(ProvisioningMethod.SetParameterValues.name());
                        List<CommandParam> paramList = command.getParams();
                        ParameterList toCPE = new ParameterList();
                        for (CommandParam cp : paramList) {
                            ParameterValueStruct pvs = new ParameterValueStruct(cp.getName(), cp.getValue(), "xsd:" + cp.getType());
                            toCPE.addParameterValueStruct(pvs);
                        }
                        reqRes.getSessionData().setToCPE(toCPE);
                        reqRes.getSessionData().setCommand(command);
                    }
                    else {
                        log.info("暂不支持该命令:" + command.getCmdType());
                        command = null;
                    }
                }

            }

            // 3. Create and set response
            ProvisioningMethod responseMethod = getResponseMethod(reqRes);
            Response response = ResponseCreateStrategy.getStrategy(responseMethod, properties).getResponse(reqRes);
            String responseStr = response.toXml();
            if (Log.isConversationLogEnabled()) {
                logConversationResponse(reqRes, responseStr);
            }
            reqRes.getResponseData().setXml(responseStr);

            //by xinaghao
            if(command != null){
                command.setMsgID(reqRes.getTR069TransactionID().getId());
                command.setCmdStatus(Command.STATUS_PROCESSING);
                command.setStartTime(new Date());
//                command.setCommandKey(reqRes.getSessionData().getCommandKey().getServerKey());
               // command.setCommandKey("reboot"); 用来解决重启的问题或者会话中断的问题
                commandService.save(command);
            }

        }

        /**
         * Log that we have got a request.
         */
        private void logWillProcessRequest(HTTPRequestResponseData reqRes) {
            String method = reqRes.getRequestData().getMethod();
            log.debug("Will process method " + method + " (incoming request/response from CPE)");
        }

        /**
         * Log the xml payload. Pretty print it if pretty print quirk is enabled.
         */
        private void logConversationRequest(HTTPRequestResponseData reqRes) {
            String unitId = Optional.ofNullable(reqRes.getSessionData().getUnitId()).orElse("Unknown");
            String xml = reqRes.getRequestData().getXml();
            if (properties.isPrettyPrintQuirk(reqRes.getSessionData())) {
                xml = XMLFormatterUtils.prettyPrintXmlString(reqRes.getRequestData().getXml());
            }
            Log.conversation(reqRes.getSessionData(), "============== FROM CPE ( " + unitId + " ) TO ACS ===============\n" + xml);
        }

        /**
         * Log the xml response.
         */
        private void logConversationResponse(HTTPRequestResponseData reqRes, String responseStr) {
            String unitId = Optional.ofNullable(reqRes.getSessionData().getUnitId()).orElse("Unknown");
            Log.conversation(reqRes.getSessionData(), "=============== FROM ACS TO ( " + unitId + " ) ============\n" + responseStr + "\n");
        }

        /**
         * The request method will never change, so this method is just here to wrap the operation
         * of converting null method to Empty
         */
        private ProvisioningMethod getRequestMethod(HTTPRequestResponseData reqRes) {
            try {
                return ProvisioningMethod.valueOf(reqRes.getRequestData().getMethod());
            } catch (Exception e) {
                return ProvisioningMethod.Empty;
            }
        }

        /**
         * The response method will be mutated by the DecisionStrategy,
         * so this method can and will return something else than the method getRequestMethod.
         */
        private ProvisioningMethod getResponseMethod(HTTPRequestResponseData reqRes) {
            try {
                return ProvisioningMethod.valueOf(reqRes.getResponseData().getMethod());
            } catch (Exception e) {
                return ProvisioningMethod.Empty;
            }
        }
    }
}
