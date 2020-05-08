package com.github.freeacs.tr069.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "tb_device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "oui")
    private String oui;

    @Column(name = "product_class")
    private String productClass;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "stun_enable")
    private boolean stunEnable;

    @Column(name = "nat_detected")
    private boolean natDetected;

    @Column(name = "connection_request_password")
    private String connectionRequestPassword;

    @Column(name = "connection_request_username")
    private String connectionRequestUsername;

    @Column(name = "periodic_inform_interval")
    private Integer periodicInformInterval;

    @Column(name = "udp_connection_request_address")
    private String udpConnectionRequestAddress;

    @Column(name = "connection_request_url")
    private String connectionRequestUrl;

    @Column(name = "last_udp_request_time")
    private Date lastUdpRequestTime;

}
