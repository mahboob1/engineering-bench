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
    private final RepositoryContextService repositoryContextService;

    public EngineeringAgentService(
            ToolExecutor toolExecutor,
            EngineeringAgent engineeringAgent,
            RepositoryContextService repositoryContextService) {

        this.toolExecutor = toolExecutor;
        this.engineeringAgent = engineeringAgent;
        this.repositoryContextService =
                repositoryContextService;
    }

    public String execute(EngineeringTask task) {

        String context =
                repositoryContextService.retrieve(
                        task.repository(),
                        task.task()
                );

        StringBuilder executionHistory =
                new StringBuilder();

        StringBuilder response =
                new StringBuilder();

        response.append("""
                Engineering Task
                -----------------
                Repository: %s
                Task: %s

                Retrieved Repository Context
                -----------------------------
                %s

                """.formatted(
                task.repository(),
                task.task(),
                context
        ));

        /*
         * Stage 13:
         *
         * The agent may decide to CONTINUE or STOP.
         *
         * CONTINUE:
         *     execute the selected tool
         *     observe the result
         *     ask the agent what to do next
         *
         * STOP:
         *     finish execution
         */

        int maxIterations = 5;

        for (int iteration = 1;
             iteration <= maxIterations;
             iteration++) {

            String agentInput = """
                    Engineering Task:
                    %s

                    Retrieved Repository Evidence:
                    %s

                    Previous Execution History:
                    %s

                    Decide the next action.
                    """.formatted(
                    task.task(),
                    context,
                    executionHistory
            );

            AgentDecision decision =
                    engineeringAgent.decide(agentInput);

            response.append("""
                    
                    Agent Decision #%d
                    -----------------
                    Action: %s
                    Tool: %s
                    Command: %s
                    Reasoning: %s

                    """.formatted(
                    iteration,
                    decision.action(),
                    decision.toolName(),
                    decision.command(),
                    decision.reasoning()
            ));

            /*
             * Agent decided that the task is complete.
             */
            if ("STOP".equalsIgnoreCase(
                    decision.action())) {

                response.append("""
                        Agent Result
                        ------------
                        Agent decided that no further action is required.
                        """);

                break;
            }

            /*
             * Agent decided that another command should be executed.
             */
            if (!"CONTINUE".equalsIgnoreCase(
                    decision.action())) {

                response.append("""
                        Agent Result
                        ------------
                        Invalid agent action. Execution stopped for safety.
                        """);

                break;
            }

            /*
             * Execute the command through the Tool Executor.
             */
            SandboxResult result =
                    toolExecutor.execute(
                            decision.toolName(),
                            task.repository(),
                            decision.command()
                    );

            String observation =
                    observe(result);

            response.append("""
                    Tool Execution #%d
                    -----------------
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
                    iteration,
                    result.exitCode(),
                    result.successful(),
                    observation,
                    result.stdout(),
                    result.stderr()
            ));

            /*
             * Give the execution result back to the agent
             * on the next iteration.
             */
            executionHistory.append("""
                    Execution #%d

                    Tool:
                    %s

                    Command:
                    %s

                    Exit Code:
                    %d

                    Successful:
                    %s

                    Observation:
                    %s

                    STDOUT:
                    %s

                    STDERR:
                    %s

                    """.formatted(
                    iteration,
                    decision.toolName(),
                    decision.command(),
                    result.exitCode(),
                    result.successful(),
                    observation,
                    result.stdout(),
                    result.stderr()
            ));
        }

        return response.toString();
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