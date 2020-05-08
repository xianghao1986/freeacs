package com.github.freeacs.tr069.entity;

import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "tb_command")
public class Command {

    public final static String RESULT_SUCCESS = "0";

    public final static String STATUS_CREATED = "0";
    public final static String STATUS_PROCESSING = "1";
    public final static String STATUS_FINISHED = "2";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "cmd_seq")
    private String cmdSeq;

    @Column(name = "cmd_type")
    private String cmdType;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "cmd_status")
    private String cmdStatus;

    @Column(name = "result")
    private String result;

//    @Column(name = "create_time")
//    private Date createTime;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "fault")
    private String fault;

    @Column(name = "msg_id")
    private String msgID;

    @Column(name = "cmd_key")
    private String commandKey;

    @Transient
    private List<CommandParam> params;

    @Transient
    private List<CommandParam> diagnosticParams;

    @Transient
    private List<ParameterValueStruct> reportParams;
}
