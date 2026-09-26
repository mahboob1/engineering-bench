package com.engineeringbench.controller;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.model.ProjectCapability;
import com.engineeringbench.model.ProjectTechnology;
import com.engineeringbench.model.RepositoryReference;
import com.engineeringbench.service.EngineeringProjectProvisioningService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EngineeringProjectProvisioningControllerTest {

    @Test
    void shouldProvisionProject() throws Exception {

        EngineeringProjectProvisioningService provisioningService =
                mock(EngineeringProjectProvisioningService.class);

        EngineeringProjectProvisioningController controller =
                new EngineeringProjectProvisioningController(
                        provisioningService
                );

        EngineeringProject expectedProject =
                new EngineeringProject(
                        "petclinic",
                        "Petclinic",
                        new RepositoryReference(
                                "https://github.com/spring-projects/spring-petclinic.git",
                                "main"
                        ),
                        new RepositoryReference(
                                "https://github.com/mahboob1/engineering-bench-petclinic.git",
                                "main"
                        ),
                        "engineering_docs",
                        new ProjectTechnology(
                                "Java",
                                "Spring Boot",
                                "Gradle"
                        ),
                        List.of(
                                new ProjectCapability(
                                        "repository",
                                        "source-analysis"
                                )
                        )
                );

        when(provisioningService.provision(
                "petclinic",
                "Petclinic",
                "https://github.com/spring-projects/spring-petclinic.git",
                "main",
                "engineering-bench-petclinic",
                "Working repository created by Engineering Bench",
                "engineering_docs",
                expectedProject.technology(),
                expectedProject.capabilities()
        )).thenReturn(expectedProject);

        EngineeringProjectProvisioningController.ProvisionProjectRequest request =
                new EngineeringProjectProvisioningController.ProvisionProjectRequest(
                        "petclinic",
                        "Petclinic",
                        "https://github.com/spring-projects/spring-petclinic.git",
                        "main",
                        "engineering-bench-petclinic",
                        "Working repository created by Engineering Bench",
                        "engineering_docs",
                        expectedProject.technology(),
                        expectedProject.capabilities()
                );

        EngineeringProject result =
                controller.provision(request);

        assertNotNull(result);
        assertEquals("petclinic", result.id());
        assertEquals(
                "https://github.com/mahboob1/engineering-bench-petclinic.git",
                result.workingRepository().url()
        );

        verify(provisioningService).provision(
                "petclinic",
                "Petclinic",
                "https://github.com/spring-projects/spring-petclinic.git",
                "main",
                "engineering-bench-petclinic",
                "Working repository created by Engineering Bench",
                "engineering_docs",
                expectedProject.technology(),
                expectedProject.capabilities()
        );
    }
}