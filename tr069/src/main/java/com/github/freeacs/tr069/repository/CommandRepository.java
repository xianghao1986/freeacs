package com.github.freeacs.tr069.repository;

import com.github.freeacs.tr069.entity.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandRepository extends JpaRepository<Command, Integer> {

    Command findFirstByDeviceIdAndCmdStatus(String deviceId, String cmdStatus);

    Command findFirstByDeviceIdAndCmdStatusAndCmdTypeOrderByIdDesc(String deviceId, String cmdStatus, String cmdType);

    Command findFirstByDeviceIdAndCmdSeq(String deviceId, String cmdSeq);

    Command findFirstByDeviceIdAndMsgID(String deviceId, String msgId);




}