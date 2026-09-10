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
    public String execute(
            String repository,
            String command) {

        SandboxResult result =
                sandboxService.execute(
                        repository,
                        List.of(command)
                );

        return """
                Tool: %s

                Command: %s

                Exit Code: %d
                Successful: %s

                STDOUT:
                %s

                STDERR:
                %s
                """.formatted(
                name(),
                command,
                result.exitCode(),
                result.successful(),
                result.stdout(),
                result.stderr()
        );
    }
}