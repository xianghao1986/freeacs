package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.tr069.entity.Command;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.Parser;

import java.util.Date;

public class FaultRequestProcessStrategy implements RequestProcessStrategy {
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(ProvisioningMethod.Fault.name());
        Parser parser = new Parser(reqRes.getRequestData().getXml());
        reqRes.getRequestData().setFault(parser.getFault());
        //上报异常
        Command command = reqRes.getSessionData().getCommand();
        if (command != null) {
            log.info("有设置参数命令正在处理中，需要处理结果");
            command.setCmdStatus(Command.STATUS_FINISHED);
            command.setEndTime(new Date());
            command.setResult(parser.getFault().getFaultCode());
            command.setFault(parser.getFault().toString());

        }
    }
}
