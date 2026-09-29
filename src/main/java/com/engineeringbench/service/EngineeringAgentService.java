package com.engineeringbench.service;

import com.engineeringbench.agent.EngineeringAgent;
import com.engineeringbench.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dev.langchain4j.service.output.OutputParsingException;
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

            int maxIterations = 8;

            String requiredReadFileAfterPatchFailure = null;

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
                                    limitAgentContext(context, 30000),
                                    limitAgentContext(executionHistory.toString(), 30000)
                );

                AgentDecision decision;

                decision = decideWithParsingRecovery(agentInput);

                if (requiredReadFileAfterPatchFailure != null) {

                    decision = new AgentDecision(
                            "CONTINUE",
                            "read_file",
                            JsonNodeFactory.instance.objectNode()
                                    .put("file", requiredReadFileAfterPatchFailure),
                            "Mandatory recovery after failed apply_patch."
                    );

                    requiredReadFileAfterPatchFailure = null;
                }


                System.out.println(
                        "\n=== AGENT DECISION ==="
                                + "\nTool: " + decision.toolName()
                                + "\nAction: " + decision.action()
                                + "\nCommand: " + decision.command()
                                + "\nReasoning: " + decision.reasoning()
                );

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

                if ("none".equalsIgnoreCase(decision.toolName())) {
                    response.append("""
                            Agent Result
                            ------------
                            Agent requested CONTINUE without selecting a tool.
                            Execution stopped for safety.
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

                if ("apply_patch".equalsIgnoreCase(decision.toolName())
                        && result.exitCode() != 0
                        && !result.stdout().contains(
                        "Source change applied successfully.")) {

                    JsonNode commandNode = decision.command();

                    if (commandNode != null
                            && commandNode.isObject()
                            && commandNode.has("file")) {

                        requiredReadFileAfterPatchFailure =
                                commandNode.get("file").asText();
                    }
                }

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

        Command:
        %s

        Exit Code:
        %d

        Successful:
        %s

        Execution Observation:
        Successful: %s
        Tests Executed:
        %s
        Diagnosis Required:
        %s
        Summary:
        %s

        Diagnosis:
        Required:
        %s
        Summary:
        %s
        Evidence:
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

    private String limitAgentContext(String value, int maxCharacters) {
        if (value == null || value.length() <= maxCharacters) {
            return value == null ? "" : value;
        }

        return "[Earlier execution history truncated.]\n\n"
                + value.substring(value.length() - maxCharacters);
    }

    private AgentDecision decideWithParsingRecovery(String agentInput) {
        String currentInput = agentInput;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return engineeringAgent.decide(currentInput);
            } catch (OutputParsingException e) {
                currentInput = agentInput + """

                    IMPORTANT:
                    Your previous response was invalid JSON and could not be parsed.

                    Return ONLY one valid JSON object.
                    Do not use markdown or code fences.
                    Do not use Java string concatenation.
                    Do not use '+' anywhere in the JSON.
                    Do not include literal tab characters inside JSON strings.
                    Represent tabs as the two characters \\t.
                    Represent newlines as the two characters \\n.
                    Escape every double quote inside a JSON string as \\".
                    The command field must be a JSON object.
                    The command object must contain file, oldText, and newText.
                    oldText and newText must each be valid JSON string values.
                    """;
            }
        }

        throw new IllegalStateException(
                "Agent failed to produce valid AgentDecision JSON after 3 attempts."
        );
    }
}