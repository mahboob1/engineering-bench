package com.engineeringbench.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.engineeringbench.model.SandboxResult;
import com.engineeringbench.model.SandboxRuntime;
import com.engineeringbench.service.SandboxService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class ApplyPatchTool implements EngineeringTool {

    private final SandboxService sandboxService;
    private final ObjectMapper objectMapper;

    public ApplyPatchTool(
            @Qualifier("fargateSandboxService")
            SandboxService sandboxService,
            ObjectMapper objectMapper) {

        this.sandboxService = sandboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "apply_patch";
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

            String script =
                    buildPatchScript(command);

            return sandboxService.execute(
                    repository,
                    revision,
                    List.of(script)
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid apply_patch command.",
                    e
            );
        }
    }

    @Override
    public SandboxResult execute(
            SandboxRuntime runtime,
            String command) {

        try {

            String script =
                    buildPatchScript(command);

            return sandboxService.execute(
                    runtime,
                    script
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid apply_patch command.",
                    e
            );
        }
    }

    private String buildPatchScript(
            String command) throws Exception {

        JsonNode edit =
                objectMapper.readTree(command);

        if (!edit.isObject()) {
            throw new IllegalArgumentException(
                    "apply_patch command must be a JSON object, not an array."
            );
        }

        if (!edit.hasNonNull("file")
                || !edit.hasNonNull("oldText")
                || !edit.hasNonNull("newText")) {

            throw new IllegalArgumentException(
                    "apply_patch command must contain file, oldText, and newText."
            );
        }

        String file =
                edit.get("file").asText();

        validatePath(file);

        String oldText =
                edit.get("oldText").asText();

        String newText =
                edit.get("newText").asText();

        String encodedOldText =
                Base64.getEncoder()
                        .encodeToString(
                                oldText.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        String encodedNewText =
                Base64.getEncoder()
                        .encodeToString(
                                newText.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        String pythonScript = """
                from pathlib import Path
                import base64
                import sys

                path = Path(sys.argv[1])

                old_text = base64.b64decode(
                    sys.argv[2]
                ).decode("utf-8")

                new_text = base64.b64decode(
                    sys.argv[3]
                ).decode("utf-8")

                content = path.read_text()

                if old_text not in content:
                    raise SystemExit(
                        "Original text was not found in target file."
                    )

                occurrences = content.count(old_text)

                if occurrences != 1:
                    raise SystemExit(
                        f"Original text occurs {occurrences} times; "
                        "refusing to apply ambiguous change."
                    )

                updated = content.replace(
                    old_text,
                    new_text,
                    1
                )

                path.write_text(updated)

                print(
                    "Source change applied successfully."
                )
                """;

        String encodedScript =
                Base64.getEncoder()
                        .encodeToString(
                                pythonScript.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        return """
                set -e

                echo "Applying source change..."

                echo "%s" | base64 -d > /tmp/apply_edit.py

                python3 /tmp/apply_edit.py "%s" "%s" "%s"

                echo "Running verification..."

                ./gradlew test

                echo "Source change verification completed successfully."
                """.formatted(
                encodedScript,
                file,
                encodedOldText,
                encodedNewText
        );
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