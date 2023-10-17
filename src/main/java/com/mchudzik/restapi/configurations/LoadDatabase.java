package com.mchudzik.restapi.configurations;

import com.mchudzik.restapi.enums.Status;
import com.mchudzik.restapi.models.*;
import com.mchudzik.restapi.repositories.*;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
 class LoadDatabase {
    
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(TaskRepository taskRepo, UserRepository userRepo )
    {
        
        return args -> {
            log.info("Preloading " + taskRepo.save(new Task("hakowanie", "hakowanie hakowanie", Status.IN_PROGRESS, LocalDate.ofEpochDay(1))));
            log.info("Preloading " + taskRepo.save(new Task("naprawianie hakow", "bol", Status.NEW, LocalDate.ofEpochDay(2))));
            log.info("Preloading " + userRepo.save(new User("jan", "kowalski", "jkowalski@gmail.com")));
				log.info("Preloading " + userRepo.save(new User("john", "doe", "jdoe@domain.com")));
				log.info("Preloading " + userRepo.save(new User("jan", "kochanowski", "jkochanowski@wp.com")));
        };
    }
}
