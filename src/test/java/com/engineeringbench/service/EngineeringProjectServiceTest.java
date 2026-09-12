package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.model.ProjectCapability;
import com.engineeringbench.model.ProjectTechnology;
import com.engineeringbench.model.RepositoryReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EngineeringProjectServiceTest {

    @Test
    void shouldCreateAndRetrieveProject() {

        EngineeringProjectRepository repository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService service =
                new EngineeringProjectService(repository);

        EngineeringProject project =
                new EngineeringProject(
                        "customer-service",
                        "Customer Service",
                        new RepositoryReference(
                                "https://github.com/example/customer-service.git",
                                "main"
                        ),
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of(
                                new ProjectCapability(
                                        "DATABASE",
                                        "PostgreSQL"
                                )
                        )
                );

        service.create(project);

        EngineeringProject retrieved =
                service.findById("customer-service");

        assertEquals(project, retrieved);
        assertTrue(service.exists("customer-service"));
    }

    @Test
    void shouldRejectDuplicateProject() {

        EngineeringProjectRepository repository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService service =
                new EngineeringProjectService(repository);

        EngineeringProject project =
                new EngineeringProject(
                        "customer-service",
                        "Customer Service",
                        new RepositoryReference(
                                "https://github.com/example/customer-service.git",
                                "main"
                        ),
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of()
                );

        service.create(project);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(project)
        );
    }

    @Test
    void shouldDeleteProject() {

        EngineeringProjectRepository repository =
                new InMemoryEngineeringProjectRepository();

        EngineeringProjectService service =
                new EngineeringProjectService(repository);

        EngineeringProject project =
                new EngineeringProject(
                        "customer-service",
                        "Customer Service",
                        new RepositoryReference(
                                "https://github.com/example/customer-service.git",
                                "main"
                        ),
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of()
                );

        service.create(project);

        assertTrue(service.exists("customer-service"));

        service.delete("customer-service");

        assertFalse(service.exists("customer-service"));
    }
}