package com.engineeringbench;

import com.engineeringbench.config.AppProperties;
import com.engineeringbench.config.QdrantProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, QdrantProperties.class})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}