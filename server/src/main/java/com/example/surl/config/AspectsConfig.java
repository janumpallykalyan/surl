package com.example.surl.config;

import com.example.surl.aspect.DuplicateKeyExceptionAspect;
import com.example.surl.aspect.LoggingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan
public class AspectsConfig {

    @Bean
    DuplicateKeyExceptionAspect duplicateKeyExceptionAspect() {
        return new DuplicateKeyExceptionAspect();
    }

    @Bean
    LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
}
