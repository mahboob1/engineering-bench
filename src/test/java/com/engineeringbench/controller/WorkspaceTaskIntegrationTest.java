package com.engineeringbench.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WorkspaceTaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateProjectWorkspaceAndTaskThroughRealApplication() throws Exception {

        // 1. Create project
        mockMvc.perform(
                        post("/api/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "project-integration-001",
                                          "name": "Customer Service",
                                          "repository": {
                                            "url": "https://github.com/example/customer-service.git",
                                            "revision": "main"
                                          },
                                          "technology": {
                                            "language": "Java",
                                            "framework": "Spring Boot",
                                            "buildTool": "Gradle"
                                          },
                                          "capabilities": []
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("project-integration-001"));

        // 2. Create workspace
        mockMvc.perform(
                        post("/api/workspaces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "workspace-integration-001",
                                          "projectId": "project-integration-001",
                                          "revision": "feature/customer-search"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("workspace-integration-001"))
                .andExpect(jsonPath("$.projectId")
                        .value("project-integration-001"))
                .andExpect(jsonPath("$.revision")
                        .value("feature/customer-search"));

        // 3. Create workspace task
        mockMvc.perform(
                        post("/api/workspace-tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "task-integration-001",
                                          "workspaceId": "workspace-integration-001",
                                          "task": "Add customer search by name and write tests."
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("task-integration-001"))
                .andExpect(jsonPath("$.workspaceId")
                        .value("workspace-integration-001"))
                .andExpect(jsonPath("$.task")
                        .value(
                                "Add customer search by name and write tests."
                        ));

        // 4. Retrieve task through the real API
        mockMvc.perform(
                        get("/api/workspace-tasks/task-integration-001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("task-integration-001"))
                .andExpect(jsonPath("$.workspaceId")
                        .value("workspace-integration-001"))
                .andExpect(jsonPath("$.task")
                        .value(
                                "Add customer search by name and write tests."
                        ));
    }
}