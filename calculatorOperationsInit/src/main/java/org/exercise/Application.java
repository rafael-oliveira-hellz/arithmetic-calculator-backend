package org.exercise;

import org.exercise.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import org.exercise.interfaces.http.OperationController;

@SpringBootApplication
@Import({ OperationController.class })
public class Application {

    public static void main(String[] args) {
        AppConfig config = new AppConfig();

        System.out.println("DB_HOST: " + System.getenv("DB_HOST"));
        System.out.println("DB_PORT: " + System.getenv("DB_PORT"));
        System.out.println("DB_NAME: " + System.getenv("DB_NAME"));
        System.out.println("DB_USER: " + System.getenv("DB_USER"));
        System.out.println("DB_PASSWORD: " + System.getenv("DB_PASSWORD"));


        SpringApplication.run(Application.class, args);
    }
}