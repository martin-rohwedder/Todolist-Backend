package dk.martinrohwedder.todolist_backend;

import org.springframework.boot.SpringApplication;

public class TestTodolistBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(TodolistBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
