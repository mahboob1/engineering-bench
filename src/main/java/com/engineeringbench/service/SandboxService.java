package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.model.SandboxRuntime;

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

    default SandboxRuntime start(
            String repository,
            String revision
    ) {
        throw new UnsupportedOperationException(
                "Persistent sandbox runtime is not supported by this sandbox implementation."
        );
    }

    default SandboxResult execute(
            SandboxRuntime runtime,
            String command
    ) {
        throw new UnsupportedOperationException(
                "Persistent sandbox runtime is not supported by this sandbox implementation."
        );
    }

    default void stop(
            SandboxRuntime runtime
    ) {
        throw new UnsupportedOperationException(
                "Persistent sandbox runtime is not supported by this sandbox implementation."
        );
    }
}