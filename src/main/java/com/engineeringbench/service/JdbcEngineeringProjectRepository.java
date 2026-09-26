package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;
import com.engineeringbench.model.ProjectCapability;
import com.engineeringbench.model.ProjectTechnology;
import com.engineeringbench.model.RepositoryReference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEngineeringProjectRepository
        implements EngineeringProjectRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcEngineeringProjectRepository(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public EngineeringProject save(EngineeringProject project) {

        String capabilitiesJson;

        try {
            capabilitiesJson =
                    objectMapper.writeValueAsString(project.capabilities());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize project capabilities", e);
        }

        jdbcTemplate.update("""
            INSERT INTO engineering_project (
                id,
                name,
                source_repository_url,
                source_repository_revision,
                working_repository_url,
                working_repository_revision,
                collection,
                technology_language,
                technology_framework,
                technology_build_tool,
                capabilities
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
            ON CONFLICT (id)
            DO UPDATE SET
                name = EXCLUDED.name,
                source_repository_url = EXCLUDED.source_repository_url,
                source_repository_revision = EXCLUDED.source_repository_revision,
                working_repository_url = EXCLUDED.working_repository_url,
                working_repository_revision = EXCLUDED.working_repository_revision,
                collection = EXCLUDED.collection,
                technology_language = EXCLUDED.technology_language,
                technology_framework = EXCLUDED.technology_framework,
                technology_build_tool = EXCLUDED.technology_build_tool,
                capabilities = EXCLUDED.capabilities
            """,
                project.id(),
                project.name(),
                project.sourceRepository().url(),
                project.sourceRepository().revision(),
                project.workingRepository().url(),
                project.workingRepository().revision(),
                project.collection(),
                project.technology().language(),
                project.technology().framework(),
                project.technology().buildTool(),
                capabilitiesJson
        );

        return project;
    }

    @Override
    public Optional<EngineeringProject> findById(String id) {

        List<EngineeringProject> projects = jdbcTemplate.query(
                """
                SELECT
                    id,
                    name,
                    source_repository_url,
                    source_repository_revision,
                    working_repository_url,
                    working_repository_revision,
                    collection,
                    technology_language,
                    technology_framework,
                    technology_build_tool,
                    capabilities
                FROM engineering_project
                WHERE id = ?
                """,
                (rs, rowNum) -> mapRow(rs),
                id
        );

        return projects.stream().findFirst();
    }

    @Override
    public List<EngineeringProject> findAll() {

        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    name,
                    source_repository_url,
                    source_repository_revision,
                    working_repository_url,
                    working_repository_revision,
                    collection,
                    technology_language,
                    technology_framework,
                    technology_build_tool,
                    capabilities
                FROM engineering_project
                """,
                (rs, rowNum) -> mapRow(rs)
        );
    }

    @Override
    public void deleteById(String id) {

        jdbcTemplate.update(
                "DELETE FROM engineering_project WHERE id = ?",
                id
        );
    }

    private EngineeringProject mapRow(
            java.sql.ResultSet rs) throws java.sql.SQLException {

        try {
            List<ProjectCapability> capabilities =
                    objectMapper.readValue(
                            rs.getString("capabilities"),
                            new TypeReference<>() {}
                    );

            ProjectTechnology technology =
                    new ProjectTechnology(
                            rs.getString("technology_language"),
                            rs.getString("technology_framework"),
                            rs.getString("technology_build_tool")
                    );

            return new EngineeringProject(
                    rs.getString("id"),
                    rs.getString("name"),
                    new RepositoryReference(
                            rs.getString("source_repository_url"),
                            rs.getString("source_repository_revision")
                    ),
                    new RepositoryReference(
                            rs.getString("working_repository_url"),
                            rs.getString("working_repository_revision")
                    ),
                    rs.getString("collection"),
                    technology,
                    capabilities
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to deserialize engineering project",
                    e
            );
        }
    }
}