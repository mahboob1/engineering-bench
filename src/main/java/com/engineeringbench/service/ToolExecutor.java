package com.engineeringbench.service;

import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.tool.EngineeringTool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolExecutor {

    private final List<EngineeringTool> tools;

    public ToolExecutor(
            List<EngineeringTool> tools) {

        this.tools = tools;
    }

    public SandboxResult execute(
            String toolName,
            String repository,
            String command) {

        EngineeringTool tool =
                findTool(toolName);

        return tool.execute(
                repository,
                command
        );
    }

    public SandboxResult execute(
            String toolName,
            String repository,
            String revision,
            String command) {

        EngineeringTool tool =
                findTool(toolName);

        return tool.execute(
                repository,
                revision,
                command
        );
    }

    private EngineeringTool findTool(
            String toolName) {

        return tools.stream()
                .filter(t -> t.name().equals(toolName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown engineering tool: "
                                        + toolName));
    }
}