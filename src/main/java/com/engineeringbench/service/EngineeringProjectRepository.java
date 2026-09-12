package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;

import java.util.List;
import java.util.Optional;

public interface EngineeringProjectRepository {

    EngineeringProject save(EngineeringProject project);

    Optional<EngineeringProject> findById(String id);

    List<EngineeringProject> findAll();

    void deleteById(String id);
}