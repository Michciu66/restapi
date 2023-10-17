package com.mchudzik.restapi;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mchudzik.restapi.assemblers.UserModelAssembler;
import com.mchudzik.restapi.controllers.UserController;
import com.mchudzik.restapi.enums.Status;
import com.mchudzik.restapi.exceptions.UserNotFoundException;
import com.mchudzik.restapi.models.Task;
import com.mchudzik.restapi.models.User;
import com.mchudzik.restapi.repositories.TaskRepository;
import com.mchudzik.restapi.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerUnitTests {
	@Autowired
	private UserRepository repo;
	@Autowired
	private UserModelAssembler assembler;
	@Autowired
	private UserController controller;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	private ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();

	@TestConfiguration
	static class TestDatabase {
		private static final Logger log = LoggerFactory.getLogger(TestDatabase.class);

		@Bean
		CommandLineRunner initDatabase(TaskRepository taskRepo, UserRepository userRepo) {

			return args -> {
				log.info("Preloading " + taskRepo.save(
						new Task("hakowanie", "hakowanie hakowanie", Status.IN_PROGRESS, LocalDate.ofEpochDay(1))));
				log.info("Preloading "
						+ taskRepo.save(new Task("naprawianie hakow", "bol", Status.NEW, LocalDate.ofEpochDay(2))));
				log.info("Preloading " + taskRepo.save(
						new Task("pisanie zadan rekrutacyjnych", "opis", Status.IN_PROGRESS, LocalDate.ofEpochDay(3))));

				log.info("Preloading " + userRepo.save(new User("jan", "kowalski", "jkowalski@gmail.com")));
				log.info("Preloading " + userRepo.save(new User("john", "doe", "jdoe@domain.com")));
				log.info("Preloading " + userRepo.save(new User("jan", "kochanowski", "jkochanowski@wp.com")));
			};
		}
	}

	@Test
	void contextLoads() throws Exception {
		assertThat(controller).isNotNull();
	}

	@Test
	void testGetAllUsers() throws Exception {
		mockMvc.perform(get("/users"))
				.andExpect(jsonPath("$", hasSize(3)));
	}

	@Test
	void testAddUser() throws Exception {

		User user = new User("michal", "chudzik", "mchudzik@gmail.com");

		String requestJson = ow.writeValueAsString(user);

		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isCreated());

		repo.findById(user.getId()).orElseThrow(() -> new UserNotFoundException(user.getId()));
	}
}
