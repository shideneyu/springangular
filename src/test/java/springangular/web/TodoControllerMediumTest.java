package springangular.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import springangular.domain.Todo;
import springangular.repository.TodoRepository;
import springangular.web.dto.TodoDTO;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.*;
import static springangular.web.exception.ErrorCode.NO_ENTITY_DELETION;


/**
 * Medium tests (or integration tests)
 * Pushes an API call, checks that <b>response</b> has expected datas (http status, body ...)
 * 
 * DB check is done in {@link springangular.web.TodoControllerBigTest}
 **/
public class TodoControllerMediumTest extends WebAppTest{

    @Autowired
    private TodoRepository todoRepository;
    
    private Todo savedTodo;
    
    @Before
    public void setUp() {
        savedTodo = new Todo.Builder().withTitle("Test").withDescription("Description Test").build();
        Todo secondTodoTest = new Todo.Builder().withTitle("Secondtest").withDescription("Second description test").build();

        todoRepository.save(savedTodo);
        todoRepository.save(secondTodoTest);
    }

    @After
    public void tearDown() {
        todoRepository.deleteAll();
    }

    @Test
    public void should_Get_AllTodos_WithTwoTestTodoInResult() {
        given()
            .log().all()
        .when()
            .get("/todo")
        .then()
            .log().all()
            .statusCode(OK.value())
            .body("[0].id", is(savedTodo.getId().intValue()))
            .body("[0].title", is(savedTodo.getTitle()))
            .body("[0].description", is(savedTodo.getDescription()));
    }

    @Test
    public void should_Get_OneTodoById_WithOneTestTodoInResult() {
        given()
            .log().all()
        .when()
            .get("/todo/{id}", savedTodo.getId())
        .then()
            .log().all()
            .statusCode(OK.value())
            .body("todo.id", is(savedTodo.getId().intValue()))
            .body("todo.title", is(savedTodo.getTitle()))
            .body("todo.description", is(savedTodo.getDescription()));
    }

    @Test
    public void should_Create_OneTodo_Nominal_Medium() {
        final String todoTitle = "NewTest";
        final String todoDescription = "NewDesc";
        Todo todoToCreate = new Todo.Builder().withTitle(todoTitle).withDescription(todoDescription).build();
        TodoDTO todoDTO = new TodoDTO(todoToCreate);
        
        given()
            .header("Content-Type", "application/json")
            .body(todoDTO)
            .log().all()
        .when()
            .put("/todo")
        .then()
            .log().all()
            .statusCode(OK.value())
            .body("todo.id", notNullValue())
            .body("todo.title", is(todoTitle))
            .body("todo.description", is(todoDescription));
    }
    
    @Test
    public void should_Update_Todo_Nominal() {
        final String updatedTitle = "NewTitle of todo";
        final String updatedDescription = "NewDescription of todo";
        savedTodo.setTitle(updatedTitle);
        savedTodo.setDescription(updatedDescription);

        TodoDTO todoDTO = new TodoDTO(savedTodo);

        given()
            .header("Content-Type", "application/json")
            .body(todoDTO)
            .log().all()
        .when()
            .put("/todo")
        .then()
            .log().all()
            .statusCode(OK.value())
            .body("todo.id", is(savedTodo.getId().intValue()))
            .body("todo.title", is(updatedTitle))
            .body("todo.description", is(updatedDescription));
    }
    
    @Test
    public void should_Delete_OneTodo_Nominal() {
        given()
            .log().all()
        .when()
            .delete("/todo/{id}", savedTodo.getId())
        .then()
            .log().all()
            .statusCode(NO_CONTENT.value());
    }
    
    @Test
    public void shouldNot_Delete_OneTodo_WhenNotFound() {
        given()
            .log().all()
        .when()
            .delete("/todo/{id}", 100)
        .then()
            .log().all()
            .statusCode(NOT_FOUND.value())
            .body("url", is("/todo/100"))
            .body("errorCode", is(NO_ENTITY_DELETION.getCode()))
            .body("reasonCause", is(NO_ENTITY_DELETION.getDescription()));
    }
}