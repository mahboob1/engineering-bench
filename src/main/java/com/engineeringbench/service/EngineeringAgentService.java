package com.engineeringbench.service;

import com.engineeringbench.agent.EngineeringAgent;
import com.engineeringbench.model.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EngineeringAgentService {

    private final ToolExecutor toolExecutor;
    private final EngineeringAgent engineeringAgent;
    private final RepositoryContextService repositoryContextService;
    private final ExecutionObservationService observationService;
    private final DiagnosisService diagnosisService;
    private final ExecutionEventService eventService;
    private final SandboxService sandboxService;

    public EngineeringAgentService(
            ToolExecutor toolExecutor,
            EngineeringAgent engineeringAgent,
            RepositoryContextService repositoryContextService,
            ExecutionObservationService observationService,
            DiagnosisService diagnosisService,
            ExecutionEventService eventService,
            @Qualifier("fargateSandboxService")
            SandboxService sandboxService
            ) {

        this.toolExecutor = toolExecutor;
        this.engineeringAgent = engineeringAgent;
        this.repositoryContextService =
                repositoryContextService;
        this.observationService =
                observationService;
        this.diagnosisService = diagnosisService;
        this.eventService = eventService;
        this.sandboxService = sandboxService;
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
             * The agent decided that no further action is required.
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
             * Validate the action before executing anything.
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
             * Execute the selected engineering tool.
             *
             */
            String command =
                    decision.command().isTextual()
                            ? decision.command().asText()
                            : decision.command().toString();
            SandboxResult result =
                    toolExecutor.execute(
                            decision.toolName(),
                            task.repository(),
                            task.revision(),
                            command
                    );

            /*
             * Convert raw execution output into structured
             * execution facts.
             */
            ExecutionObservation observation =
                    observationService.observe(result);
            Diagnosis diagnosis =
                    diagnosisService.diagnose(result);

            response.append("""
                    Tool Execution #%d
                    -----------------
                    Exit Code: %d
                    Successful: %s

                    Execution Observation
                    ---------------------
                    Successful: %s
                    Tests Executed: %s
                    Diagnosis Required: %s
                    Summary: %s
                    
                    Diagnosis
                    ---------
                    Required: %s
                    Summary: %s
                    Evidence: %s

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
                    observation.successful(),
                    observation.testsExecuted(),
                    observation.diagnosisRequired(),
                    observation.summary(),
                    diagnosis.required(),
                    diagnosis.summary(),
                    diagnosis.evidence(),
                    result.stdout(),
                    result.stderr()
            ));

            /*
             * Store both the raw execution result and the structured
             * observation so the agent can use them during its
             * next decision.
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

                    Execution Observation:
                    Successful:
                    %s
                    Tests Executed:
                    %s
                    Diagnosis Required:
                    %s
                    Summary:
                    %s
                    
                    Diagnosis:
                    Required: %s
                    Summary: %s
                    Evidence: %s

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
                    observation.successful(),
                    observation.testsExecuted(),
                    observation.diagnosisRequired(),
                    observation.summary(),
                    diagnosis.required(),
                    diagnosis.summary(),
                    diagnosis.evidence(),
                    result.stdout(),
                    result.stderr()
            ));
        }

        return response.toString();
    }

    public String execute(EngineeringTask task, String workspaceTaskId) {

        String context =
                repositoryContextService.retrieve(
                        task.repository(),
                        workspaceTaskId
                );

        SandboxRuntime runtime =
                sandboxService.start(
                        task.repository(),
                        task.revision()
                );

        eventService.add(
                workspaceTaskId,
                0,
                "TASK_STARTED",
                "Persistent engineering sandbox started."
        );

        try {
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

            int maxIterations = 5;

            for (int iteration = 1; iteration <= maxIterations; iteration++) {

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

                eventService.add(
                        workspaceTaskId,
                        iteration,
                        "AGENT_DECISION",
                        "Agent selected "
                                + decision.toolName()
                                + " with action "
                                + decision.action()
                                + "."
                );

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
                 * The agent decided that no further action is required.
                 */
                if ("STOP".equalsIgnoreCase(
                        decision.action())) {

                    response.append("""
                            Agent Result
                            ------------
                            Agent decided that no further action is required.
                            """);

                    return response.toString();
                }

                /*
                 * Validate the action before executing anything.
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

                eventService.add(
                        workspaceTaskId,
                        iteration,
                        "TOOL_STARTED",
                        "Executing tool "
                                + decision.toolName()
                                + "."
                );

                /*
                 * Execute the selected engineering tool.
                 */
                String command =
                        decision.command().isTextual()
                                ? decision.command().asText()
                                : decision.command().toString();
                SandboxResult result =
                        toolExecutor.execute(
                                decision.toolName(),
                                runtime,
                                command
                        );

                eventService.add(
                        workspaceTaskId,
                        iteration,
                        "TOOL_COMPLETED",
                        decision.toolName()
                                + " completed with exit code "
                                + result.exitCode()
                                + "."
                );

                /*
                 * Convert raw execution output into structured
                 * execution facts.
                 */
                ExecutionObservation observation =
                        observationService.observe(result);
                Diagnosis diagnosis =
                        diagnosisService.diagnose(result);

                eventService.add(
                        workspaceTaskId,
                        iteration,
                        "DIAGNOSIS",
                        diagnosis.summary()
                );

                response.append("""
                        Tool Execution #%d
                        -----------------
                        Exit Code: %d
                        Successful: %s
                        
                        Execution Observation
                        ---------------------
                        Successful: %s
                        Tests Executed: %s
                        Diagnosis Required: %s
                        Summary: %s
                        
                        Diagnosis
                        ---------
                        Required: %s
                        Summary: %s
                        Evidence: %s
                        
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
                        observation.successful(),
                        observation.testsExecuted(),
                        observation.diagnosisRequired(),
                        observation.summary(),
                        diagnosis.required(),
                        diagnosis.summary(),
                        diagnosis.evidence(),
                        result.stdout(),
                        result.stderr()
                ));

                /*
                 * Store both the raw execution result and the structured
                 * observation so the agent can use them during its
                 * next decision.
                 */
                executionHistory.append("""
        Execution #%d

        Tool:
        %s

        Exit Code:
        %d

        Successful:
        %s

        Execution Observation:
        Successful: %s
        Tests Executed: %s
        Diagnosis Required: %s
        Summary: %s

        Diagnosis:
        Required: %s
        Summary: %s

        """.formatted(
                        iteration,
                        decision.toolName(),
                        result.exitCode(),
                        result.successful(),
                        observation.successful(),
                        observation.testsExecuted(),
                        observation.diagnosisRequired(),
                        observation.summary(),
                        diagnosis.required(),
                        diagnosis.summary()
                ));
            }

            /*
             * Reaching this point means the agent did NOT
             * successfully complete the task.
             */
            throw new IllegalStateException(
                    "Agent reached maximum iterations without completing the task."
            );
        } finally {

            sandboxService.stop(runtime);
        }
    }
}