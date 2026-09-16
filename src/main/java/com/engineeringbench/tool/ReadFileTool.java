package com.engineeringbench.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.service.SandboxService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class ReadFileTool implements EngineeringTool {

    private final SandboxService sandboxService;
    private final ObjectMapper objectMapper;

    public ReadFileTool(
            @Qualifier("fargateSandboxService")
            SandboxService sandboxService,
            ObjectMapper objectMapper) {

        this.sandboxService = sandboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "read_file";
    }

    @Override
    public SandboxResult execute(
            String repository,
            String command) {

        return execute(
                repository,
                null,
                command
        );
    }

    @Override
    public SandboxResult execute(
            String repository,
            String revision,
            String command) {

        try {

            JsonNode request =
                    objectMapper.readTree(command);

            String file =
                    request.get("file").asText();

            validatePath(file);

            String encodedFile =
                    Base64.getEncoder()
                            .encodeToString(
                                    file.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            String script = """
                    set -e

                    FILE=$(echo "%s" | base64 -d)

                    echo "Reading file: $FILE"

                    test -f "$FILE"

                    cat -- "$FILE"
                    """.formatted(encodedFile);

            return sandboxService.execute(
                    repository,
                    revision,
                    List.of(script)
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid read_file command.",
                    e
            );
        }
    }

    private void validatePath(String file) {

        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException(
                    "File path is required."
            );
        }

        if (file.startsWith("/")
                || file.contains("..")) {

            throw new IllegalArgumentException(
                    "File path must be repository-relative."
            );
        }
    }
}