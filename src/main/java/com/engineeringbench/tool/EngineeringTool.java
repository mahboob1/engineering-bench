package com.engineeringbench.tool;

import com.engineeringbench.model.SandboxResult;

public interface EngineeringTool {

    String name();

    SandboxResult execute(
            String repository,
            String command
    );

    default SandboxResult execute(
            String repository,
            String revision,
            String command
    ) {
        return execute(repository, command);
    }
}