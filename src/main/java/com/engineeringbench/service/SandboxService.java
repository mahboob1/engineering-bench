package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;

import java.util.List;

public interface SandboxService {

    SandboxResult execute(
            String repository,
            List<String> commands
    );

    default SandboxResult execute(
            String repository,
            String revision,
            List<String> commands
    ) {
        return execute(repository, commands);
    }
}