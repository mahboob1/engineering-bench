package com.engineeringbench.service;

import com.engineeringbench.model.WorkspaceTask;
import com.engineeringbench.model.WorkspaceTaskStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JdbcWorkspaceTaskRepository
        implements WorkspaceTaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWorkspaceTaskRepository(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public WorkspaceTask save(
            WorkspaceTask task) {

        jdbcTemplate.update("""
            INSERT INTO workspace_task (
                id,
                workspace_id,
                task,
                status
            )
            VALUES (?, ?, ?, ?)
            ON CONFLICT (id)
            DO UPDATE SET
                workspace_id = EXCLUDED.workspace_id,
                task = EXCLUDED.task,
                status = EXCLUDED.status
            """,
                task.id(),
                task.workspaceId(),
                task.task(),
                task.status().name()
        );

        return task;
    }

    @Override
    public Optional<WorkspaceTask> findById(
            String id) {

        List<WorkspaceTask> tasks =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            workspace_id,
                            task,
                            status
                        FROM workspace_task
                        WHERE id = ?
                        """,
                        (rs, rowNum) ->
                                new WorkspaceTask(
                                        rs.getString("id"),
                                        rs.getString("workspace_id"),
                                        rs.getString("task"),
                                        WorkspaceTaskStatus.valueOf(
                                                rs.getString("status")
                                        )
                                ),
                        id
                );

        return tasks.stream().findFirst();
    }

    @Override
    public List<WorkspaceTask> findAll() {

        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    workspace_id,
                    task,
                    status
                FROM workspace_task
                """,
                (rs, rowNum) ->
                        new WorkspaceTask(
                                rs.getString("id"),
                                rs.getString("workspace_id"),
                                rs.getString("task"),
                                WorkspaceTaskStatus.valueOf(
                                        rs.getString("status")
                                )
                        )
        );
    }

    @Override
    public List<WorkspaceTask> findByWorkspaceId(
            String workspaceId) {

        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    workspace_id,
                    task,
                    status
                FROM workspace_task
                WHERE workspace_id = ?
                """,
                (rs, rowNum) ->
                        new WorkspaceTask(
                                rs.getString("id"),
                                rs.getString("workspace_id"),
                                rs.getString("task"),
                                WorkspaceTaskStatus.valueOf(
                                        rs.getString("status")
                                )
                        ),
                workspaceId
        );
    }

    @Override
    public void deleteById(String id) {

        jdbcTemplate.update(
                "DELETE FROM workspace_task WHERE id = ?",
                id
        );
    }

    @Override
    public long count() {

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workspace_task",
                Long.class
        );

        return count != null ? count : 0L;
    }
}