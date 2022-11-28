package com.zihenx.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration // anotação para classe de configuração
public class AppConfig {

    @Bean // anotação para que o spring gerencie a injeção da dependecia desse metodo/componente
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
