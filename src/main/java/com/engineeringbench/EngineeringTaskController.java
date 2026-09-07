package com.engineeringbench;

import com.engineeringbench.model.EngineeringTask;
import com.engineeringbench.service.EngineeringAgentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/engineering-task")
public class EngineeringTaskController {

    private final EngineeringAgentService agentService;

    public EngineeringTaskController(
            EngineeringAgentService agentService) {

        this.agentService = agentService;
    }

    @PostMapping
    public String execute(
            @RequestBody EngineeringTask task) {

        return agentService.execute(task);
    }
}