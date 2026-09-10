package com.engineeringbench.agent;

import com.engineeringbench.model.AgentDecision;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface EngineeringAgent {

    @SystemMessage("""
            You are an AI software engineering agent.

            Your job is to analyze the engineering task and decide
            which engineering tool should be used and which command
            should be executed.

            Available tool:
            - run_command

            Supported commands:
            - ./gradlew test
            - ./gradlew build
            - ./gradlew compileJava

            Rules:
            1. Return only a structured AgentDecision.
            2. Do not execute commands yourself.
            3. Do not invent tools.
            4. Use ./gradlew test when the task asks to run tests.
            5. Use ./gradlew build when the task asks to build the repository.
            6. Use ./gradlew compileJava when the task asks to compile Java.
            7. If the task cannot be mapped to one of these commands,
               explain that in the reasoning.
            """)
    AgentDecision decide(
            @UserMessage String task
    );
}