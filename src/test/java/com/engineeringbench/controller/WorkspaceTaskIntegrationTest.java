package com.engineeringbench.controller;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.service.EngineeringAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WorkspaceTaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EngineeringAgentService engineeringAgentService;

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

    @Test
    void shouldExecuteWorkspaceTaskThroughHttp() throws Exception {

        when(engineeringAgentService.execute(any(EngineeringTask.class)))
                .thenReturn("execution-result");

        when(engineeringAgentService.execute(
                any(EngineeringTask.class),
                anyString()
        )).thenReturn("execution-result");

        // 1. Create project
        mockMvc.perform(
                        post("/api/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "project-execution-001",
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
                .andExpect(status().isOk());

        // 2. Create workspace
        mockMvc.perform(
                        post("/api/workspaces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "workspace-execution-001",
                                      "projectId": "project-execution-001",
                                      "revision": "feature/customer-search"
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        // 3. Create workspace task
        mockMvc.perform(
                        post("/api/workspace-tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "task-001",
                                      "workspaceId": "workspace-execution-001",
                                      "task": "Add customer search by name and write tests."
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        // 4. Execute task through HTTP
        mockMvc.perform(
                        post("/api/workspace-tasks/task-001/execute")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("execution-result"));
    }

    @Test
    void shouldRetrieveWorkspaceTaskResultThroughHttp() throws Exception {

        when(engineeringAgentService.execute(any(EngineeringTask.class)))
                .thenReturn("execution-result");

        when(engineeringAgentService.execute(
                any(EngineeringTask.class),
                anyString()
        )).thenReturn("execution-result");

        // Create project
        mockMvc.perform(
                        post("/api/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "project-result-001",
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
                .andExpect(status().isOk());

        // Create workspace
        mockMvc.perform(
                        post("/api/workspaces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "workspace-result-001",
                                      "projectId": "project-result-001",
                                      "revision": "feature/customer-search"
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        // Create task
        mockMvc.perform(
                        post("/api/workspace-tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "task-result-001",
                                      "workspaceId": "workspace-result-001",
                                      "task": "Add customer search."
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        // Execute task
        mockMvc.perform(
                        post("/api/workspace-tasks/task-result-001/execute")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("execution-result"));

        // Retrieve stored result
        mockMvc.perform(
                        get("/api/workspace-tasks/task-result-001/result")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId")
                        .value("task-result-001"))
                .andExpect(jsonPath("$.output")
                        .value("execution-result"));
    }

    @Test
    void shouldShowCompletedTaskAfterExecution() throws Exception {

        when(engineeringAgentService.execute(any(EngineeringTask.class)))
                .thenReturn("execution-result");

        when(engineeringAgentService.execute(
                any(EngineeringTask.class),
                anyString()
        )).thenReturn("execution-result");

        // Create project
        mockMvc.perform(
                        post("/api/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "project-lifecycle-001",
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
                .andExpect(status().isOk());

        // Create workspace
        mockMvc.perform(
                        post("/api/workspaces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "workspace-lifecycle-001",
                                      "projectId": "project-lifecycle-001",
                                      "revision": "feature/customer-search"
                                    }
                                    """)
                )
                .andExpect(status().isOk());

        // Create task
        mockMvc.perform(
                        post("/api/workspace-tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "id": "task-lifecycle-001",
                                      "workspaceId": "workspace-lifecycle-001",
                                      "task": "Add customer search."
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CREATED"));

        // Execute task
        mockMvc.perform(
                        post("/api/workspace-tasks/task-lifecycle-001/execute")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("execution-result"));

        // Verify task lifecycle
        var response = mockMvc.perform(
                        get("/api/workspace-tasks/task-lifecycle-001")
                )
                .andReturn();

        System.out.println(
                "TASK STATUS: "
                        + response.getResponse().getStatus()
        );

        System.out.println(
                "TASK RESPONSE: "
                        + response.getResponse().getContentAsString()
        );
    }
}