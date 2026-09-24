package com.engineeringbench.tool;

import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.model.SandboxRuntime;

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

    default SandboxResult execute(
            SandboxRuntime runtime,
            String command
    ) {
        throw new UnsupportedOperationException(
                "Persistent sandbox runtime is not supported by this tool."
        );
    }
}