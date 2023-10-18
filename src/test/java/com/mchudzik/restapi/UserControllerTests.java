package com.mchudzik.restapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertNull;


import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mchudzik.restapi.exceptions.UserNotFoundException;
import com.mchudzik.restapi.models.User;
import com.mchudzik.restapi.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {
	@Autowired
	private UserRepository repo;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	private static final String USERS_PATH = "/users";

	@BeforeEach 
	public void resetRepo()
	{
		repo.deleteAll();
		repo.flush();

		prepareUserRepo();
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
		
		//when
		mockMvc.perform(get(USERS_PATH))
				//then
				.andExpect(jsonPath("$._embedded.userList", hasSize((int)repo.count())));
	}

	@Test
	void testAddUser() throws Exception {

		//given
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
	}

	@Test
	void testAddUserWithNullFields() throws Exception{
		//given
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
	}

	@Test
	void testAddNull() throws Exception{
		//given
		User user = null;
		String requestJson = objectMapper.writeValueAsString(user);

		//when
		mockMvc.perform(post(USERS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
		//then
				.andExpect(status().isBadRequest());
		
	}

	@Test
	void testDeleteUser() throws Exception{
		//given
		Long id = repo.findAll().get(1).getId();

		//when
		mockMvc.perform(delete(USERS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNoContent());

		User user = repo.findById(id).orElse(null);
		assertNull(user);
	}

	@Test
	void testDeleteNonexistentUser() throws Exception{
		//given
		Long id = Long.MAX_VALUE;

		//when
		mockMvc.perform(delete(USERS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNoContent());

		User user = repo.findById(id).orElse(null);
		assertNull(user);
	}

	@Test
	void testFindUser() throws Exception
	{
		//given
		Long id = repo.findAll().get(1).getId();

		//when
		MvcResult result = mockMvc.perform(get(USERS_PATH + "/{id}",id))
		.andReturn();

		String json = result.getResponse().getContentAsString();
		User foundUser = objectMapper.readValue(json, User.class);

		//then
		assertEquals(repo.findById(id).get(), foundUser);
	}

	@Test
	void testFindNonexistentUser() throws Exception{
		//given
		Long id = Long.MAX_VALUE;

		//when
		mockMvc.perform(get(USERS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNotFound());

	}

	@Test
	void testFindUserByName() throws Exception{
		//given
		String input = "j";

		//when
		MvcResult result = mockMvc.perform(get(USERS_PATH + "/byName").param("name",input))
		.andReturn();
		String json = result.getResponse().getContentAsString();
		ArrayNode node = (ArrayNode) objectMapper.readTree(json).get("_embedded").get("userList");
		List<User> foundUsers = objectMapper.readerFor(new TypeReference<List<User>>() {}).readValue(node);

		//then
		assertEquals(3, foundUsers.size());
	}

	@Test
	void testEditUser() throws Exception{
		//given
		User user = new User("michal", "chudzik", "mchudzik@gmail.com");
		Long id = repo.findAll().get(1).getId();
		String requestJson = objectMapper.writeValueAsString(user);

		//when
		MvcResult result = mockMvc.perform(put(USERS_PATH + "/{id}",id)
		.contentType(MediaType.APPLICATION_JSON)
		.content(requestJson))
		.andExpect(status().isCreated())
		.andReturn();

		String json = result.getResponse().getContentAsString();
		User createdUser = objectMapper.readValue(json, User.class);
		User editedUser = repo.findAll().get(1);

		//then
		assertEquals(editedUser, createdUser);
		assertEquals(user.getName(), createdUser.getName());
		assertEquals(user.getSurname(), createdUser.getSurname());
		assertEquals(user.getEmail(), createdUser.getEmail());
		}

	@Test
	void testUnsupportedRequest() throws Exception{
		//when
		mockMvc.perform(get(USERS_PATH + "/Teapot"))
		//then
		.andExpect(status().isBadRequest());
	}
}
