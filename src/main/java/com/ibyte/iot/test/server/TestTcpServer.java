package com.ibyte.iot.test.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestTcpServer {

    /**
     * 程序启动 Server
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(TestTcpServer.class, args);
        //ApplicationContext context = new FileSystemXmlApplicationContext(new String[]{"classpath:spring-config.xml"});
        //context.getApplicationName();
    }

}
