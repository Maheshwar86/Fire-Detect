package com.vote.digital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class DigitalVoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigitalVoteApplication.class, args);
    }
}
