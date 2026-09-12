package com.engineeringbench.service;

import com.engineeringbench.model.EngineeringProject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EngineeringProjectService {

    private final EngineeringProjectRepository projectRepository;

    public EngineeringProjectService(
            EngineeringProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public EngineeringProject create(
            EngineeringProject project) {

        if (projectRepository.findById(project.id()).isPresent()) {
            throw new IllegalArgumentException(
                    "Project already exists: " + project.id());
        }

        return projectRepository.save(project);
    }

    public EngineeringProject findById(
            String id) {

        return projectRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project not found: " + id));
    }

    public List<EngineeringProject> findAll() {
        return projectRepository.findAll();
    }

    public boolean exists(String id) {
        return projectRepository.findById(id).isPresent();
    }

    public void delete(String id) {

        if (projectRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Project not found: " + id);
        }

        projectRepository.deleteById(id);
    }
}