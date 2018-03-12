package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableOAuth2Sso
public class Application  extends WebMvcConfigurerAdapter {

    @Autowired
    private SecurityInterceptor securityInterceptor;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

        @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor);
    }
}
