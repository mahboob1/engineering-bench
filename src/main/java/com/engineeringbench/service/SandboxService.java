package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;

import java.util.List;

public interface SandboxService {

    SandboxResult execute(
            String repository,
            List<String> commands
    );
}