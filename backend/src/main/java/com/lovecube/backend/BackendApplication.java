package com.lovecube.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories({
        "com.lovecube.backend.repository",
        "com.lovecube.backend.growth.repository"
})
public class BackendApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(BackendApplication.class, args);
    }

}
