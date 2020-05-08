package com.github.freeacs.stun;

import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.repository.DeviceRepository;
import de.javawi.jstun.test.demo.StunServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;

@Slf4j
@Component
public class StunServlet {
    public StunServer server;

    private final Properties properties;
    private final DeviceRepository deviceRepository;

    public StunServlet(Properties properties, DeviceRepository deviceRepository) {
        this.properties = properties;
        this.deviceRepository = deviceRepository;
    }

    @PostConstruct
    private void trigger() {
        try {
            if (properties.isStunEnable()) {
                if (server == null) {
                    int pPort = properties.getPrimaryPort();
                    String pIp = properties.getPrimaryIp();
                    int sPort = properties.getSecondaryPort();
                    String sIp = properties.getSecondaryIp();
                    server =
                            new StunServer(pPort, InetAddress.getByName(pIp), sPort, InetAddress.getByName(sIp), deviceRepository);
                }
                if (!StunServer.isStarted()) {
                    log.info("Server startup...");
                    server.start();
                }
            }

        } catch (Throwable t) {
            log.error("An error occurred while starting Stun Server", t);
        }
    }
}
