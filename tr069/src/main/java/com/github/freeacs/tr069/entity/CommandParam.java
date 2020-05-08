package com.github.freeacs.tr069.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_command_param")
public class CommandParam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;
    @Column(name = "type")
    private String type;
    @Column(name = "value")
    private String value;
    @Column(name = "command_id")
    private Integer commandId;
    //是否为诊断参数，如果ping命令下发，收到终端的诊断完成inform后，下发的查询诊断结果的参数
    @Column(name = "diagnostic")
    private boolean diagnostic;


}
