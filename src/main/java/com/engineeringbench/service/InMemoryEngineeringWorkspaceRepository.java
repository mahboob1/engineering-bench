package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringWorkspace;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryEngineeringWorkspaceRepository
        implements EngineeringWorkspaceRepository {

    private final Map<String, EngineeringWorkspace> workspaces =
            new ConcurrentHashMap<>();

    @Override
    public EngineeringWorkspace save(
            EngineeringWorkspace workspace) {

        workspaces.put(workspace.id(), workspace);
        return workspace;
    }

    @Override
    public Optional<EngineeringWorkspace> findById(
            String id) {

        return Optional.ofNullable(workspaces.get(id));
    }

    @Override
    public List<EngineeringWorkspace> findAll() {
        return List.copyOf(workspaces.values());
    }

    @Override
    public void deleteById(String id) {
        workspaces.remove(id);
    }

    @Override
    public long count() {
        return workspaces.size();
    }
}