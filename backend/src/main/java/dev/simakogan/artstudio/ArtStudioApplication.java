package dev.simakogan.artstudio;

import dev.simakogan.artstudio.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class ArtStudioApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArtStudioApplication.class, args);
    }
}
