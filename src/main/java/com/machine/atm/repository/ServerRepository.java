package com.machine.atm.repository;

import com.machine.atm.model.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerRepository {

    @Bean
    public Server getServer(){
        return new Server();
    }

}