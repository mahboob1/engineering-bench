package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalSandboxService implements SandboxService {

    @Override
    public SandboxResult execute(
            String repository,
            List<String> commands) {

        return new SandboxResult(
                0,
                "Sandbox execution placeholder\n"
                        + "Repository: " + repository
                        + "\nCommands: " + commands,
                "",
                "Build System",
                "Test Command"
        );
    }
}