package dnss.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration
@Configuration
@ComponentScan
public class Main {
    public static void main(String[] args) throws Exception {
        Simulator.init();
        SpringApplication.run(Main.class);
    }
}