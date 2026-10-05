package dk.martinrohwedder.todolist_backend.repositories;

import dk.martinrohwedder.todolist_backend.TestcontainersConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Import(TestcontainersConfiguration.class)
@DataJpaTest
@Testcontainers
public abstract class AbstractRepositoryTest {
}
