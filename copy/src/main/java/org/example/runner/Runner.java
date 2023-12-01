package org.example.runner;

import org.example.protocol.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {
    @Autowired
    private Server server;

    @Value("${tcp.tcp-port}")
    private Integer tcpPort;

    @Override
    public void run(String... args) {
        System.out.println(server.createServer(tcpPort));
    }
}
