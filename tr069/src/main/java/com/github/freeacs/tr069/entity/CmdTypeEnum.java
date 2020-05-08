package com.github.freeacs.tr069.entity;

public enum CmdTypeEnum {
    GETPARAM("GetParameterValues", "查询参数"), SETPARAM("SetParameterValues", "设置参数"),
    REBOOT("Reboot", "重启"), PING("Ping", "ping命令");

    private String type;
    private String desc;

    CmdTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static String getDescByType(String type) {
        CmdTypeEnum[] cmdTypeEnums = values();
        for (CmdTypeEnum cmdTypeEnum : cmdTypeEnums) {
            if (cmdTypeEnum.type.equals(type)) {
                return cmdTypeEnum.desc;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
