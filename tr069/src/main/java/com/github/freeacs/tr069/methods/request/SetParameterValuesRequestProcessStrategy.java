package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.util.SyslogClient;
import com.github.freeacs.tr069.entity.CmdTypeEnum;
import com.github.freeacs.tr069.entity.Command;
import com.github.freeacs.tr069.http.HTTPRequestResponseData;
import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.methods.ProvisioningMethod;
import com.github.freeacs.tr069.xml.ParameterList;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import com.github.freeacs.tr069.xml.Parser;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class SetParameterValuesRequestProcessStrategy implements RequestProcessStrategy {

    private final DBI dbi;

    SetParameterValuesRequestProcessStrategy(DBI dbi) {
        this.dbi = dbi;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void process(HTTPRequestResponseData reqRes) throws Exception {
        reqRes.getRequestData().setMethod(ProvisioningMethod.SetParameterValues.name());
        Parser parser = new Parser(reqRes.getRequestData().getXml());
        if (parser.getHeader().getNoMoreRequests() != null
                && parser.getHeader().getNoMoreRequests().getNoMoreRequestFlag()) {
            reqRes.getSessionData().setNoMoreRequests(true);
        }
        SessionData sessionData = reqRes.getSessionData();
        Command command = reqRes.getSessionData().getCommand();
        if(command != null && CmdTypeEnum.SETPARAM.getType().equals(command.getCmdType())){
            log.info("有设置参数命令正在处理中，需要处理结果");
            command.setCmdStatus(Command.STATUS_FINISHED);
            command.setEndTime(new Date());
            command.setResult(Command.RESULT_SUCCESS);

        };
        ParameterList paramList = sessionData.getToCPE();
        for (ParameterValueStruct pvs : paramList.getParameterValueList()) {
            log.debug(pvs.getName() + " : " + pvs.getValue());
            String user = dbi.getSyslog().getIdentity().getUser().getUsername();
            SyslogClient.notice(
                    sessionData.getUnitId(),
                    "ProvMsg: Written to CPE: " + pvs.getName() + " = " + pvs.getValue(),
                    SyslogConstants.FACILITY_TR069,
                    "latest",
                    user);
        }
    }
}
