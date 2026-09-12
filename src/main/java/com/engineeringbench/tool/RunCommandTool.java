package com.engineeringbench.tool;

import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.service.SandboxService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RunCommandTool implements EngineeringTool {

    private final SandboxService sandboxService;

    public RunCommandTool(
            @Qualifier("fargateSandboxService")
            SandboxService sandboxService) {

        this.sandboxService = sandboxService;
    }

    @Override
    public String name() {
        return "run_command";
    }

    @Override
    public SandboxResult execute(
            String repository,
            String command) {

        return sandboxService.execute(
                repository,
                List.of(command)
        );
    }

    @Override
    public SandboxResult execute(
            String repository,
            String revision,
            String command) {

        return sandboxService.execute(
                repository,
                revision,
                List.of(command)
        );
    }
}