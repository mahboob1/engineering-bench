package com.engineeringbench.controller;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.WorkspaceTask;
import com.engineeringbench.service.WorkspaceTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceTaskController.class)
class WorkspaceTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceTaskService taskService;

    private WorkspaceTask task;

    @BeforeEach
    void setUp() {
        task =
                new WorkspaceTask(
                        "task-001",
                        "workspace-001",
                        "Add customer search by name and write tests."
                );
    }

    @Test
    void shouldCreateWorkspaceTask() throws Exception {
        when(taskService.create(any(WorkspaceTask.class)))
                .thenReturn(task);

        mockMvc.perform(
                        post("/api/workspace-tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "task-001",
                                          "workspaceId": "workspace-001",
                                          "task": "Add customer search by name and write tests."
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("task-001"))
                .andExpect(jsonPath("$.workspaceId")
                        .value("workspace-001"))
                .andExpect(jsonPath("$.task")
                        .value("Add customer search by name and write tests."))
                .andExpect(jsonPath("$.status")
                .value("CREATED"));

        verify(taskService).create(any(WorkspaceTask.class));
    }

    @Test
    void shouldFindWorkspaceTaskById() throws Exception {
        when(taskService.findById("task-001"))
                .thenReturn(task);

        mockMvc.perform(
                        get("/api/workspace-tasks/task-001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value("task-001"))
                .andExpect(jsonPath("$.workspaceId")
                        .value("workspace-001"))
                .andExpect(jsonPath("$.status")
                .value("CREATED"));

        verify(taskService).findById("task-001");
    }

    @Test
    void shouldFindAllWorkspaceTasks() throws Exception {
        when(taskService.findAll())
                .thenReturn(List.of(task));

        mockMvc.perform(
                        get("/api/workspace-tasks")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value("task-001"))
                .andExpect(jsonPath("$[0].workspaceId")
                        .value("workspace-001"));

        verify(taskService).findAll();
    }

    @Test
    void shouldCheckWorkspaceTaskExists() throws Exception {
        when(taskService.exists("task-001"))
                .thenReturn(true);

        mockMvc.perform(
                        get("/api/workspace-tasks/task-001/exists")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(taskService).exists("task-001");
    }

    @Test
    void shouldDeleteWorkspaceTask() throws Exception {
        doNothing()
                .when(taskService)
                .delete("task-001");

        mockMvc.perform(
                        delete("/api/workspace-tasks/task-001")
                )
                .andExpect(status().isOk());

        verify(taskService).delete("task-001");
    }

    @Test
    void shouldConvertWorkspaceTaskToEngineeringTask()
            throws Exception {

        EngineeringTask engineeringTask =
                new EngineeringTask(
                        "https://github.com/example/customer-service.git",
                        "feature/customer-search",
                        "Add customer search by name and write tests."
                );

        when(taskService.toEngineeringTask("task-001"))
                .thenReturn(engineeringTask);

        mockMvc.perform(
                        get("/api/workspace-tasks/task-001/engineering-task")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repository")
                        .value(
                                "https://github.com/example/customer-service.git"
                        ))
                .andExpect(jsonPath("$.revision")
                        .value("feature/customer-search"))
                .andExpect(jsonPath("$.task")
                        .value(
                                "Add customer search by name and write tests."
                        ));

        verify(taskService)
                .toEngineeringTask("task-001");
    }

    @Test
    void shouldReturnWorkspaceTaskCount() throws Exception {
        when(taskService.count())
                .thenReturn(3L);

        mockMvc.perform(
                        get("/api/workspace-tasks/count")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(taskService).count();
    }

    @Test
    void shouldFindWorkspaceTasksByWorkspaceId() throws Exception {

        WorkspaceTask secondTask =
                new WorkspaceTask(
                        "task-002",
                        "workspace-001",
                        "Add pagination."
                );

        when(taskService.findByWorkspaceId("workspace-001"))
                .thenReturn(List.of(task, secondTask));

        mockMvc.perform(
                        get("/api/workspace-tasks/workspace/workspace-001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].id")
                        .value("task-001"))
                .andExpect(jsonPath("$[0].workspaceId")
                        .value("workspace-001"))
                .andExpect(jsonPath("$[1].id")
                        .value("task-002"))
                .andExpect(jsonPath("$[1].workspaceId")
                        .value("workspace-001"))
                .andExpect(jsonPath("$[0].status")
                        .value("CREATED"))
                .andExpect(jsonPath("$[1].status")
                        .value("CREATED"));

        verify(taskService)
                .findByWorkspaceId("workspace-001");
    }
}