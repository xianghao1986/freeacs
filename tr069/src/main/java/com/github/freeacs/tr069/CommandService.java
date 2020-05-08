package com.github.freeacs.tr069;

import com.github.freeacs.tr069.entity.CmdTypeEnum;
import com.github.freeacs.tr069.entity.Command;
import com.github.freeacs.tr069.entity.CommandParam;
import com.github.freeacs.tr069.repository.CommandParamRepository;
import com.github.freeacs.tr069.repository.CommandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
@Transactional
public class CommandService {
    public final static String SUCCESS = "00";

    private final CommandRepository commandRepository;

    private final CommandParamRepository commandParamRepository;

    public CommandService(CommandRepository commandRepository, CommandParamRepository commandParamRepository) {
        this.commandRepository = commandRepository;
        this.commandParamRepository = commandParamRepository;
    }

    public void save(Command command) {
        commandRepository.save(command);
    }


    public String addCommand(String deviceId, String cmdType, String cmdSeq, List<CommandParam> params, List<CommandParam> retParams) {
        if (null != commandRepository.findFirstByDeviceIdAndCmdSeq(deviceId, cmdSeq)) {
            String msg = "已经存在deviceId：" + deviceId + "，cmdSeq:" + cmdSeq + "，的任务，不能重复添加";
            log.error(msg);
            return msg;
        }
        Command command = new Command();
        command.setDeviceId(deviceId);
        command.setCmdSeq(cmdSeq);
        command.setCmdType(cmdType);
        command.setCmdStatus(Command.STATUS_CREATED);
        commandRepository.save(command);

        if (params != null && params.size() > 0) {
            for (CommandParam param : params) {
                param.setCommandId(command.getId());
//                commandParamRepository.save(param);
            }
            commandParamRepository.saveAll(params);
        } else if (cmdType.equals(CmdTypeEnum.SETPARAM.getType()) || cmdType.equals(CmdTypeEnum.GETPARAM.getType())
                || cmdType.equals(CmdTypeEnum.PING.getType())) {
            String msg = CmdTypeEnum.getDescByType(cmdType) + "命令，需要携带参数列表！";
            log.error(msg);
            return msg;
        }
        if (retParams != null && retParams.size() > 0){//Ping和TraceRoute 命令有该参数
            for (CommandParam param : retParams) {
                param.setCommandId(command.getId());
                param.setDiagnostic(true);
            }
            commandParamRepository.saveAll(retParams);
        }
        log.info("deviceId:" + deviceId + ",cmdSeq:" + cmdSeq + ",添加[" + CmdTypeEnum.getDescByType(cmdType) + "]命令成功");
        return SUCCESS;
    }

    public Command selectOneOfWaitingCommand(String deviceId) {
        Command command = commandRepository.findFirstByDeviceIdAndCmdStatus(deviceId, Command.STATUS_CREATED);
        if (command == null) return null;
        List<CommandParam> params = commandParamRepository.findByCommandIdAndDiagnostic(command.getId(),false);
        if (params != null && params.size() > 0) {
            command.setParams(params);
        }
        List<CommandParam> diagnosticParams = commandParamRepository.findByCommandIdAndDiagnostic(command.getId(),true);
        if (diagnosticParams != null && diagnosticParams.size() > 0) {
            command.setDiagnosticParams(diagnosticParams);
        }

        return command;
    }

    public Command findProcessingRebootCommand(String deviceId) {
        return commandRepository.findFirstByDeviceIdAndCmdStatusAndCmdTypeOrderByIdDesc
                (deviceId, Command.STATUS_PROCESSING, CmdTypeEnum.REBOOT.getType());
    }

    public Command findProcessingPingCommand(String deviceId) {
        return commandRepository.findFirstByDeviceIdAndCmdStatusAndCmdTypeOrderByIdDesc
                (deviceId, Command.STATUS_PROCESSING, CmdTypeEnum.PING.getType());
    }

//    public void stop(Command command) {
//
//    }
}
