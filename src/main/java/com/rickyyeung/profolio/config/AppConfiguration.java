package com.rickyyeung.profolio.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AppConfiguration {

    @Value("${app.backend.domain}")
    public String backendDomain;
}
