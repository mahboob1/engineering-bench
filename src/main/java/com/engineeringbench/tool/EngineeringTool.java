package com.engineeringbench.tool;

public interface EngineeringTool {

    String name();

    String execute(
            String repository,
            String command
    );
}