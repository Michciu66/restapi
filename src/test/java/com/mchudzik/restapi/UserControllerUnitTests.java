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
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	private static final String USERS_PATH = "/users";

	@TestConfiguration
	static class TestDatabase {
		private static final Logger log = LoggerFactory.getLogger(TestDatabase.class);

		@Bean
		CommandLineRunner initTestDatabase(TaskRepository taskRepo, UserRepository userRepo) {

			return args -> {
				log.info("Preloading " + taskRepo.save(
						new Task("hakowanie", "hakowanie hakowanie", Status.IN_PROGRESS, LocalDate.ofEpochDay(1))));
				log.info("Preloading "
						+ taskRepo.save(new Task("naprawianie hakow", "bol", Status.NEW, LocalDate.ofEpochDay(2))));
				log.info("Preloading " + taskRepo.save(
						new Task("pisanie zadan rekrutacyjnych", "opis", Status.IN_PROGRESS, LocalDate.ofEpochDay(3))));
			};
		}
	}

	private void prepareUserRepo()
	{
		repo.save(new User("jan", "jowalski", "jjowalski@gmail.com"));
		repo.save(new User("john", "doe", "jdoe@domain.com"));
		repo.save(new User("dan", "jochanowski", "djochanowski@wp.com"));
	}

	@Test
	void testGetAllUsers() throws Exception {
		//given
		prepareUserRepo();
		
		//when
		mockMvc.perform(get(USERS_PATH))
				//then
				.andExpect(jsonPath("$._embedded.userList", hasSize((int)repo.count())));

		repo.deleteAll();
	}

	@Test
	void testAddUser() throws Exception {

		//given
		prepareUserRepo();
		User user = new User("michal", "chudzik", "mchudzik@gmail.com");
		String requestJson = objectMapper.writeValueAsString(user);

		//when
		MvcResult result = mockMvc.perform(post(USERS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isCreated())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		User createdUser = objectMapper.readValue(json, User.class);

		//then
		User repoUser = repo.findById(createdUser.getId()).orElseThrow(() -> new UserNotFoundException(user.getId()));
		assertEquals("michal", repoUser.getName());
		assertEquals("chudzik", repoUser.getSurname());
		assertEquals("mchudzik@gmail.com", repoUser.getEmail());
		repo.deleteAll();
	}

	@Test
	void testAddUserWithNullFields() throws Exception{
		//given
		prepareUserRepo();
		User user = new User("michal",null,null);
		String requestJson = objectMapper.writeValueAsString(user);

		//when
		MvcResult result = mockMvc.perform(post(USERS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isCreated())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		User createdUser = objectMapper.readValue(json, User.class);

		//then
		User repoUser = repo.findById(createdUser.getId()).orElseThrow(() -> new UserNotFoundException(user.getId()));
		assertEquals("michal", repoUser.getName());
		assertNull(repoUser.getSurname());
		assertNull(repoUser.getEmail());
		repo.deleteAll();
	}

	@Test
	void testAddNull() throws Exception{
		//given
		prepareUserRepo();
		User user = null;
		String requestJson = objectMapper.writeValueAsString(user);

		//when
		mockMvc.perform(post(USERS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
		//then
				.andExpect(status().isBadRequest());
		
		repo.deleteAll();

	}

	@Test
	void testDeleteUser() throws Exception{
		//given
		prepareUserRepo();
		Long id = Long.valueOf(1);

		//when
		mockMvc.perform(delete(USERS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNoContent());

		User user = repo.findById(id).orElse(null);
		assertNull(user);
		repo.deleteAll();
	}

	@Test
	void testDeleteNonexistentUser() throws Exception{
		//given
		prepareUserRepo();
		Long id = Long.valueOf(repo.count()+1);

		//when
		mockMvc.perform(delete(USERS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNoContent());

		User user = repo.findById(id).orElse(null);
		assertNull(user);
		repo.deleteAll();
	}

	@Test
	void testFindUser() throws Exception
	{
		//given
		prepareUserRepo();
		Long id = Long.valueOf(2);

		//when
		MvcResult result = mockMvc.perform(get(USERS_PATH + "/{id}",id))
		.andReturn();

		String json = result.getResponse().getContentAsString();
		User foundUser = objectMapper.readValue(json, User.class);

		//then
		assertEquals(repo.findById(id).get(), foundUser);
		repo.deleteAll();
	}

	@Test
	void testFindNonexistentUser() throws Exception{
		//given
		prepareUserRepo();
		Long id = Long.MAX_VALUE;

		//when
		mockMvc.perform(get(USERS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNotFound());

		repo.deleteAll();
	}

	@Test
	void testFindUserByName() throws Exception{
		//given
		prepareUserRepo();
		String input = "j";

		//when
		MvcResult result = mockMvc.perform(get(USERS_PATH + "/byName").param(input))
		.andReturn();
		String json = result.getResponse().getContentAsString();
		List<User> foundUsers = objectMapper.readValue(json, new TypeReference<List<User>>(){});

		//then
		assertEquals(3, foundUsers.size());
		assertNull(foundUsers.stream().filter(user -> "john".equals(user.getName())).findAny().orElse(null));
		assertNull(foundUsers.stream().filter(user -> "jan".equals(user.getName())).findAny().orElse(null));
		assertNull(foundUsers.stream().filter(user -> "dan".equals(user.getName())).findAny().orElse(null));
		repo.deleteAll();

	}

	// TO DO: Test edit user
}
