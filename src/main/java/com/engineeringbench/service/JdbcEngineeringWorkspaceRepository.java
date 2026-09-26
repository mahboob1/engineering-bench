package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringWorkspace;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JdbcEngineeringWorkspaceRepository
        implements EngineeringWorkspaceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEngineeringWorkspaceRepository(
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public EngineeringWorkspace save(
            EngineeringWorkspace workspace) {

        jdbcTemplate.update("""
            INSERT INTO engineering_workspace (
                id,
                project_id,
                revision
            )
            VALUES (?, ?, ?)
            ON CONFLICT (id)
            DO UPDATE SET
                project_id = EXCLUDED.project_id,
                revision = EXCLUDED.revision
            """,
                workspace.id(),
                workspace.projectId(),
                workspace.revision()
        );

        return workspace;
    }

    @Override
    public Optional<EngineeringWorkspace> findById(
            String id) {

        List<EngineeringWorkspace> workspaces =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            project_id,
                            revision
                        FROM engineering_workspace
                        WHERE id = ?
                        """,
                        (rs, rowNum) ->
                                new EngineeringWorkspace(
                                        rs.getString("id"),
                                        rs.getString("project_id"),
                                        rs.getString("revision")
                                ),
                        id
                );

        return workspaces.stream().findFirst();
    }

    @Override
    public List<EngineeringWorkspace> findAll() {

        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    project_id,
                    revision
                FROM engineering_workspace
                """,
                (rs, rowNum) ->
                        new EngineeringWorkspace(
                                rs.getString("id"),
                                rs.getString("project_id"),
                                rs.getString("revision")
                        )
        );
    }

    @Override
    public void deleteById(String id) {

        jdbcTemplate.update(
                "DELETE FROM engineering_workspace WHERE id = ?",
                id
        );
    }

    @Override
    public long count() {

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM engineering_workspace",
                Long.class
        );

        return count != null ? count : 0L;
    }
}