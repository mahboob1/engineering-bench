package com.engineeringbench.service;

import com.engineeringbench.agent.EngineeringAgent;
import com.engineeringbench.model.AgentDecision;
import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.model.SandboxResult;
import org.springframework.stereotype.Service;

@Service
public class EngineeringAgentService {

    private final ToolExecutor toolExecutor;
    private final EngineeringAgent engineeringAgent;

    public EngineeringAgentService(
            ToolExecutor toolExecutor,
            EngineeringAgent engineeringAgent) {

        this.toolExecutor = toolExecutor;
        this.engineeringAgent = engineeringAgent;
    }

    public String execute(EngineeringTask task) {

        AgentDecision decision =
                engineeringAgent.decide(task.task());

        SandboxResult result =
                toolExecutor.execute(
                        decision.toolName(),
                        task.repository(),
                        decision.command()
                );

        String observation =
                observe(result);

        return """
                Engineering Task
                -----------------
                Repository: %s
                Task: %s

                Agent Decision
                --------------
                Tool: %s
                Command: %s
                Reasoning: %s

                Tool Execution
                --------------
                Exit Code: %d
                Successful: %s

                Agent Observation
                -----------------
                %s

                STDOUT
                ------
                %s

                STDERR
                ------
                %s
                """.formatted(
                task.repository(),
                task.task(),
                decision.toolName(),
                decision.command(),
                decision.reasoning(),
                result.exitCode(),
                result.successful(),
                observation,
                result.stdout(),
                result.stderr()
        );
    }

    private String observe(
            SandboxResult result) {

        if (result.exitCode() == 0) {

            if (result.stdout().contains(
                    "NO-SOURCE")) {

                return """
                        Execution completed successfully,
                        but no test source files were found.
                        The command succeeded, but this does not
                        prove that tests actually executed.
                        """;
            }

            return """
                    Execution completed successfully.
                    The selected command returned exit code 0.
                    """;
        }

        return """
                Execution failed.
                The selected command returned a non-zero exit code.
                Further diagnosis is required.
                """;
    }
}