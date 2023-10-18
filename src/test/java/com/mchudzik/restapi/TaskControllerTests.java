package com.mchudzik.restapi;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mchudzik.restapi.enums.Status;
import com.mchudzik.restapi.exceptions.TaskNotFoundException;
import com.mchudzik.restapi.models.Task;
import com.mchudzik.restapi.models.User;
import com.mchudzik.restapi.repositories.TaskRepository;
import com.mchudzik.restapi.repositories.UserRepository;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertNull;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTests {
    
    @Autowired
	private TaskRepository taskRepo;
    @Autowired
    private UserRepository userRepo;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	private static final String TASKS_PATH = "/tasks";

    @BeforeEach 
	public void resetTaskRepo()
	{
		taskRepo.deleteAll();
		taskRepo.flush();
        userRepo.deleteAll();;
        userRepo.flush();

		prepareTaskRepo();
	}

	private void prepareTaskRepo()
	{
		taskRepo.save(new Task("hakowanie", "hakowanie hakowanie", Status.IN_PROGRESS, LocalDate.ofEpochDay(1)));
		taskRepo.save(new Task("naprawianie hakow", "bol", Status.NEW, LocalDate.ofEpochDay(2)));
		taskRepo.save(new Task("pisanie zadan rekrutacyjnych", "hakowanie", Status.IN_PROGRESS, LocalDate.ofEpochDay(3)));
	}
    
    private void prepareUserRepo()
	{
		userRepo.save(new User("jan", "jowalski", "jjowalski@gmail.com"));
		userRepo.save(new User("john", "doe", "jdoe@domain.com"));
		userRepo.save(new User("dan", "jochanowski", "djochanowski@wp.com"));
	}

    @Test
    void testGetAllTasks() throws Exception{
        //given

        //when
		mockMvc.perform(get(TASKS_PATH))
        //then
        .andExpect(jsonPath("$._embedded.taskList", hasSize((int)taskRepo.count())));
    }

    @Test
    void testAddTask() throws Exception{
        //given
		Task task = new Task("granie w gre", "tomb rajder", Status.NEW, LocalDate.ofEpochDay((3)));
		String requestJson = objectMapper.writeValueAsString(task);

		//when
		MvcResult result = mockMvc.perform(post(TASKS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isCreated())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		Task createdTask = objectMapper.readValue(json, Task.class);

		//then
		Task repoTask = taskRepo.findById(createdTask.getId()).orElseThrow(() -> new TaskNotFoundException(task.getId()));
		assertEquals("granie w gre", repoTask.getName());
		assertEquals("tomb rajder", repoTask.getDesc());
		assertEquals(Status.NEW,repoTask.getStatus());
        assertEquals(LocalDate.ofEpochDay(3), repoTask.getFinishDate());
    }

    @Test
    void testAddTaskWithNullFields() throws Exception{
        //given
		Task task = new Task("granie w gre", null, null, null);
		String requestJson = objectMapper.writeValueAsString(task);

		//when
		MvcResult result = mockMvc.perform(post(TASKS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isCreated())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		Task createdTask = objectMapper.readValue(json, Task.class);

		//then
		Task repoTask = taskRepo.findById(createdTask.getId()).orElseThrow(() -> new TaskNotFoundException(task.getId()));
		assertEquals("granie w gre", repoTask.getName());
		assertNull(repoTask.getDesc());
		assertNull(repoTask.getStatus());
        assertNull(repoTask.getFinishDate());
    }

    @Test
	void testDeleteTask() throws Exception{
		//given
		Long id = taskRepo.findAll().get(1).getId();

		//when
		mockMvc.perform(delete(TASKS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNoContent());

		Task task = taskRepo.findById(id).orElse(null);
		assertNull(task);
	}

    @Test
	void testDeleteNonexistentTask() throws Exception{
		//given
		Long id = Long.MAX_VALUE;

		//when
		mockMvc.perform(delete(TASKS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNoContent());

		Task task = taskRepo.findById(id).orElse(null);
		assertNull(task);
	}

    @Test
	void testFindTask() throws Exception
	{
		//given
		Long id = taskRepo.findAll().get(1).getId();

		//when
		MvcResult result = mockMvc.perform(get(TASKS_PATH + "/{id}",id))
		.andReturn();

		String json = result.getResponse().getContentAsString();
		Task foundTask = objectMapper.readValue(json, Task.class);

		//then
		assertEquals(taskRepo.findById(id).get(), foundTask);
	}

	@Test
	void testFindNonexistentTask() throws Exception{
		//given
		Long id = Long.MAX_VALUE;

		//when
		mockMvc.perform(get(TASKS_PATH + "/{id}",id))
		//then
		.andExpect(status().isNotFound());

	}

    @Test
	void testFindTaskByName() throws Exception{
		//given
		String input = "hakow";

		//when
		MvcResult result = mockMvc.perform(get(TASKS_PATH + "/byName").param("name",input))
		.andReturn();
		String json = result.getResponse().getContentAsString();
		ArrayNode node = (ArrayNode) objectMapper.readTree(json).get("_embedded").get("userList");
		List<Task> foundTasks = objectMapper.readerFor(new TypeReference<List<Task>>() {}).readValue(node);

		//then
		assertEquals(3, foundTasks.size());
	}

    @Test
	void testFindTaskByStatus() throws Exception{
		//given
		Status input = Status.IN_PROGRESS;

		//when
		MvcResult result = mockMvc.perform(get(TASKS_PATH + "/byStatus").param("status",input.toString()))
		.andReturn();
		String json = result.getResponse().getContentAsString();
		ArrayNode node = (ArrayNode) objectMapper.readTree(json).get("_embedded").get("userList");
		List<Task> foundTasks = objectMapper.readerFor(new TypeReference<List<Task>>() {}).readValue(node);

		//then
		assertEquals(2, foundTasks.size());
        assertEquals(Status.IN_PROGRESS, foundTasks.get(0).getStatus());
        assertEquals(Status.IN_PROGRESS, foundTasks.get(1).getStatus());
	}

    @Test
	void testFindTaskByIncorrectStatus() throws Exception{
		//given
		String input = "Teapot";

		//when
		mockMvc.perform(get(TASKS_PATH + "/byStatus").param("status",input))
        //then
		.andExpect(status().isNotAcceptable());
	}

    @Test
    void testFindTaskBeforeDate() throws Exception{
        //given
        String inputDate = "1970-01-03";

        //when
        MvcResult result = mockMvc.perform(get(TASKS_PATH + "/byStatus")
        .param("startDate",inputDate))
        .andReturn();

        String json = result.getResponse().getContentAsString();
		ArrayNode node = (ArrayNode) objectMapper.readTree(json).get("_embedded").get("taskList");
		List<Task> foundTasks = objectMapper.readerFor(new TypeReference<List<Task>>() {}).readValue(node);

        //then
        assertEquals(3,foundTasks.size());
    }

    @Test
    void testFindTaskBetweenDates() throws Exception{
        //given
        String inputStartDate = "1970-01-02";
        String inputFinishDate = "1970-01-03";


        //when
        MvcResult result = mockMvc.perform(get(TASKS_PATH + "/byStatus")
        .param("startDate",inputStartDate)
        .param("finishDate",inputFinishDate))
        .andReturn();

        String json = result.getResponse().getContentAsString();
		ArrayNode node = (ArrayNode) objectMapper.readTree(json).get("_embedded").get("taskList");
		List<Task> foundTasks = objectMapper.readerFor(new TypeReference<List<Task>>() {}).readValue(node);

        //then
        assertEquals(2,foundTasks.size());
    }

    @Test
	void testEditTask() throws Exception{
		//given
		Task task = new Task("granie w gre", "tomb rajder", Status.NEW, LocalDate.ofEpochDay((3)));
		Long id = taskRepo.findAll().get(1).getId();
		String requestJson = objectMapper.writeValueAsString(task);

		//when
		MvcResult result = mockMvc.perform(put(TASKS_PATH + "/{id}",id)
		.contentType(MediaType.APPLICATION_JSON)
		.content(requestJson))
		.andExpect(status().isCreated())
		.andReturn();

		String json = result.getResponse().getContentAsString();
		Task createdTask = objectMapper.readValue(json, Task.class);
		Task editedTask = taskRepo.findAll().get(1);

		//then
		assertEquals(editedTask, createdTask);
		assertEquals(task.getName(), createdTask.getName());
		assertEquals(task.getDesc(), createdTask.getDesc());
		assertEquals(task.getStatus(), createdTask.getStatus());
        assertEquals(task.getFinishDate(), createdTask.getFinishDate());
		}

    @Test
    void testEditStatus() throws Exception{
        //given
        Long id = taskRepo.findAll().get(1).getId();
        String newStatus = Status.COMPLETED.toString();

        //when
        MvcResult result = mockMvc.perform(put(TASKS_PATH + "/status/{id}",id)
        .contentType(MediaType.TEXT_PLAIN)
        .content(newStatus))
        //then
        .andExpect(status().isCreated())
        .andReturn();

        String json = result.getResponse().getContentAsString();
        Task createdTask = objectMapper.readValue(json, Task.class);
        Task editedTask = taskRepo.findAll().get(1);

        assertEquals(editedTask, createdTask);
		assertEquals(Status.COMPLETED, createdTask.getStatus());
    }

    @Test
    void testEditStatusOfNullTask() throws Exception{
        //given
        Long id = Long.MAX_VALUE;
        String newStatus = Status.COMPLETED.toString();

        //when
        mockMvc.perform(put(TASKS_PATH + "/status/{id}",id)
        .contentType(MediaType.TEXT_PLAIN)
        .content(newStatus))
        //then
        .andExpect(status().isNotFound());
    }

    @Test
    void testChangeTaskStatusToIncorrectValue() throws Exception{
        //given
        Long id = taskRepo.findAll().get(1).getId();
        String newStatus = "Teapot";

        //when
        mockMvc.perform(put(TASKS_PATH + "/status/{id}",id)
        .contentType(MediaType.TEXT_PLAIN)
        .content(newStatus))
        //then
        .andExpect(status().isNotFound());
    }

    @Test
    void testAssignUser() throws Exception{
        //given
        prepareUserRepo();
        Long taskId = taskRepo.findAll().get(1).getId();
        Long userId = userRepo.findAll().get(1).getId();

        //when
        mockMvc.perform(put(TASKS_PATH + "{taskId}/{userId}",taskId,userId))
        //then
        .andExpect(status().isCreated());

        assertEquals(userId,taskRepo.findAll().get(1).getAssignedUsers().get(0));
    }

    @Test
    void testAssignUserToNonexistentTask() throws Exception{
        //given
        prepareUserRepo();
        Long taskId = Long.MAX_VALUE;
        Long userId = userRepo.findAll().get(1).getId();

        //when
        mockMvc.perform(put(TASKS_PATH + "{taskId}/{userId}",taskId,userId))
        //then
        .andExpect(status().isNotFound());
    }

    @Test
    void testAssignNonexistentUser() throws Exception{
        //given
        prepareUserRepo();
        Long taskId = taskRepo.findAll().get(1).getId();
        Long userId = Long.MAX_VALUE;

        //when
        mockMvc.perform(put(TASKS_PATH + "{taskId}/{userId}",taskId,userId))
        //then
        .andExpect(status().isNotFound());
    }

    @Test
    void testUnassignUser() throws Exception{
        //given
        prepareUserRepo();
        Long taskId = taskRepo.findAll().get(1).getId();
        Long userId = userRepo.findAll().get(1).getId();
        taskRepo.findAll().get(1).addUser(userId);

        //when
        mockMvc.perform(delete(TASKS_PATH + "{taskId}/{userId}",taskId,userId))
        //then
        .andExpect(status().isNoContent());
    }

    @Test
    void testUnassignNonexistentUser() throws Exception{
        //given
        prepareUserRepo();
        Long taskId = taskRepo.findAll().get(1).getId();
        Long userId = Long.MAX_VALUE;

        //when
        mockMvc.perform(delete(TASKS_PATH + "{taskId}/{userId}",taskId,userId))
        //then
        .andExpect(status().isNoContent());

    }



}
