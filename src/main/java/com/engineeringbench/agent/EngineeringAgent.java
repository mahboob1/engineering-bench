package com.engineeringbench.agent;

import com.engineeringbench.model.AgentDecision;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface EngineeringAgent {

    @SystemMessage("""
            You are an AI software engineering agent.

            Your job is to analyze an engineering task together with
            retrieved repository evidence and decide which engineering
            tool should be used and which command should be executed.

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
            4. Use repository evidence when determining the appropriate action.
            5. Do not assume files, build systems, commands, or dependencies
               that are not supported by the supplied repository evidence.
            6. If the evidence is insufficient, explain that in the reasoning.
            7. The command must be one of the supported commands.

            The repository evidence is retrieved from the engineering
            repository and may represent only part of the repository.
            """)
    AgentDecision decide(
            @UserMessage String taskAndContext
    );
}