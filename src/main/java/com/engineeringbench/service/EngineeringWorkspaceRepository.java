package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringWorkspace;

import java.util.List;
import java.util.Optional;

public interface EngineeringWorkspaceRepository {

    EngineeringWorkspace save(EngineeringWorkspace workspace);

    Optional<EngineeringWorkspace> findById(String id);

    List<EngineeringWorkspace> findAll();

    void deleteById(String id);

    long count();
}