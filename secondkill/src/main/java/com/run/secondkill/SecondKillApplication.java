package com.run.secondkill;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class SecondKillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecondKillApplication.class,args);
    }

    @GetMapping("/ping")
    public String ping(){
        return "pong";
    }
}
