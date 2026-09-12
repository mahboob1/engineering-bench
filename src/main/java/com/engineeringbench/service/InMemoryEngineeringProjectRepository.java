package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryEngineeringProjectRepository
        implements EngineeringProjectRepository {

    private final Map<String, EngineeringProject> projects =
            new ConcurrentHashMap<>();

    @Override
    public EngineeringProject save(
            EngineeringProject project) {

        projects.put(project.id(), project);
        return project;
    }

    @Override
    public Optional<EngineeringProject> findById(
            String id) {

        return Optional.ofNullable(projects.get(id));
    }

    @Override
    public List<EngineeringProject> findAll() {
        return List.copyOf(projects.values());
    }

    @Override
    public void deleteById(String id) {
        projects.remove(id);
    }
}