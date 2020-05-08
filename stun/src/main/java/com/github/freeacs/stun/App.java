package com.github.freeacs.stun;

import static spark.Spark.get;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ExecutorWrapperFactory;
import com.github.freeacs.common.spark.SparkApp;
import com.github.freeacs.common.util.TimestampMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.sql.DataSource;

import de.javawi.jstun.test.demo.StunServer;
import de.javawi.jstun.util.Address;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class App extends SparkApp {

  public static void main(String[] args) {
    final App app = new App();
    Properties properties = new Properties(app.config);
    ExecutorWrapper executorWrapper = ExecutorWrapperFactory.create(2);
    StunServlet stunServlet = new StunServlet(app.datasource, properties, executorWrapper);
    stunServlet.init();
    get(properties.getContextPath() + "/ok", (req, res) -> "FREEACSOK");
    get(properties.getContextPath() + "/reqConn", new Route() {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            try {
                TimestampMap activeDevices = StunServer.getActiveStunClients();
                String uri = activeDevices.newest();
                DatagramSocket socket = StunServer.newestReceiveSocket;
//                int pPort = properties.getPrimaryPort();
//                String pIp = properties.getPrimaryIp();
//                DatagramSocket socket = new DatagramSocket(pPort, InetAddress.getByName(pIp));
                String req = "GET http://" + uri + "?ts=" + System.currentTimeMillis() + "&id=" + System.currentTimeMillis() +
                        "&un=cpe" + "&cn=8837237846066432308" + "&sig=B748ED10822125901DDFB74983EF95AC18DAA299";
//                String req = "GET http://192.168.11.61:47643?ts=1588244850771&id=76658&un=cpe&cn=8837237846066432308&sig=B748ED10822125901DDFB74983EF95AC18DAA299";
                DatagramPacket send = new DatagramPacket(req.getBytes(), req.getBytes().length);
                send.setPort(Integer.parseInt(uri.split(":")[1]));
                Address address = new Address(uri.split(":")[0]);
                send.setAddress(address.getInetAddress());
                socket.send(send);
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
            return "ok";
        }
    });
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Shutdown Hook is running !");
                  StunServlet.destroy();
                  executorWrapper.shutdown();
                }));
  }
}
