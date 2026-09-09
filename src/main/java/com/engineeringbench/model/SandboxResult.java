package com.engineeringbench.model;

public record SandboxResult(
        int exitCode,
        String stdout,
        String stderr,
        String buildSystem,
        String testCommand
) {

    public boolean successful() {
        return exitCode == 0;
    }
}