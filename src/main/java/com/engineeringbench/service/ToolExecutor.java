package com.engineeringbench.service;

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

    public String execute(
            String toolName,
            String repository,
            String command) {

        EngineeringTool tool =
                tools.stream()
                        .filter(t -> t.name().equals(toolName))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Unknown engineering tool: "
                                                + toolName));

        return tool.execute(
                repository,
                command
        );
    }
}